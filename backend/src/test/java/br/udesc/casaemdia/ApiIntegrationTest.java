package br.udesc.casaemdia;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

/**
 * Percorre a API de ponta a ponta (HTTP → controller → service → H2 em memória),
 * incluindo segurança, validação e isolamento entre usuários.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final LocalDate HOJE = LocalDate.now(ZoneId.of("America/Sao_Paulo"));

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("Rotas protegidas sem token respondem 401 no formato padrão de erro")
    void semTokenRetorna401() throws Exception {
        mvc.perform(get("/api/lembretes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    @DisplayName("Cadastro, e-mail duplicado e login com senha errada")
    void fluxoDeAutenticacao() throws Exception {
        String email = emailUnico();
        cadastrar(email);

        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson(email)))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"senha-errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos"));

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email.toUpperCase() + "\",\"senha\":\"segredo123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.nomeCasa").value("Casa de Teste"));
    }

    @Test
    @DisplayName("Validação de campos (400) e regra de negócio (422)")
    void validacoes() throws Exception {
        String token = cadastrar(emailUnico());

        mvc.perform(post("/api/lembretes").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"\",\"categoria\":\"OUTRO\",\"dataVencimento\":\"" + HOJE
                        + "\",\"recorrencia\":\"NENHUMA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("titulo"));

        mvc.perform(post("/api/lembretes").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Água\",\"categoria\":\"CONTA\",\"dataVencimento\":\"" + HOJE
                        + "\",\"recorrencia\":\"MENSAL\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("Criar, concluir (gera a próxima) e isolamento entre usuários")
    void cicloDeVidaDoLembrete() throws Exception {
        String token = cadastrar(emailUnico());

        String criado = mvc.perform(post("/api/lembretes").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Conta de luz\",\"categoria\":\"CONTA\",\"dataVencimento\":\""
                        + HOJE.minusDays(1) + "\",\"valor\":184.50,\"recorrencia\":\"MENSAL\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.atrasado").value(true))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(criado, "$.id");

        mvc.perform(patch("/api/lembretes/{id}/concluir", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluido.concluido").value(true))
                .andExpect(jsonPath("$.proximo.dataVencimento").value(HOJE.minusDays(1).plusMonths(1).toString()));

        mvc.perform(patch("/api/lembretes/{id}/concluir", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnprocessableContent());

        mvc.perform(get("/api/lembretes").param("concluido", "true")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Desfazer a conclusão remove a ocorrência gerada e deixa só o original, pendente
        mvc.perform(patch("/api/lembretes/{id}/reabrir", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluido").value(false));
        mvc.perform(get("/api/lembretes").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(patch("/api/lembretes/{id}/concluir", id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        String outroUsuario = cadastrar(emailUnico());
        mvc.perform(get("/api/lembretes/{id}", id).header(HttpHeaders.AUTHORIZATION, bearer(outroUsuario)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Lista de compras e painel")
    void comprasEPainel() throws Exception {
        String token = cadastrar(emailUnico());

        String item = mvc.perform(post("/api/compras").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Café\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantidade").value(1))
                .andReturn().getResponse().getContentAsString();
        Integer cafeId = JsonPath.read(item, "$.id");

        mvc.perform(post("/api/compras").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Detergente\",\"quantidade\":2}"))
                .andExpect(status().isCreated());

        mvc.perform(patch("/api/compras/{id}/alternar", cafeId).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$.comprado").value(true));

        mvc.perform(get("/api/painel").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCasa").value("Casa de Teste"))
                .andExpect(jsonPath("$.itensPendentesCompra").value(1));

        mvc.perform(delete("/api/compras/comprados").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/compras").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Detergente"));
    }

    @Test
    @DisplayName("Análise de gastos do mês, com histórico e parâmetro de mês inválido")
    void analiseDeGastos() throws Exception {
        String token = cadastrar(emailUnico());
        String mesAtual = HOJE.toString().substring(0, 7);

        mvc.perform(post("/api/lembretes").header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Aluguel\",\"categoria\":\"CONTA\",\"dataVencimento\":\"" + HOJE
                        + "\",\"valor\":1200.00,\"recorrencia\":\"MENSAL\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/analises/gastos").param("mes", mesAtual).param("meses", "3")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mes").value(mesAtual))
                .andExpect(jsonPath("$.total").value(1200.0))
                .andExpect(jsonPath("$.pendente").value(1200.0))
                .andExpect(jsonPath("$.historico", hasSize(3)))
                .andExpect(jsonPath("$.porCategoria[0].categoria").value("CONTA"))
                .andExpect(jsonPath("$.porCategoria[0].percentual").value(100))
                .andExpect(jsonPath("$.maioresGastos[0].titulo").value("Aluguel"));

        mvc.perform(get("/api/analises/gastos").param("mes", "novembro")
                .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content(cadastroJson(email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.token");
    }

    private static String cadastroJson(String email) {
        return "{\"nome\":\"Teste\",\"nomeCasa\":\"Casa de Teste\",\"email\":\"" + email
                + "\",\"senha\":\"segredo123\"}";
    }

    private static String emailUnico() {
        return "teste-" + UUID.randomUUID() + "@email.com";
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
