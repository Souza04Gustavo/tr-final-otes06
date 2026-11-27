# 6. Roteiro do vídeo — Casa em Dia

> Plano da gravação da demonstração (entrega em 27/11). Duração-alvo: **cerca de 8 minutos**; a seção 6.5 traz uma versão de 5 minutos. Cada bloco indica qual requisito do trabalho ele comprova.

## 6.1 Antes de gravar (preparação, ≈ 15 min)

**Ambiente**
- [ ] Back-end rodando: `cd backend` → `.\mvnw.cmd spring-boot:run` (no PowerShell, depois do passo 2 do README), com o terminal visível e o log limpo.
- [ ] Emulador ligado e o app instalado (Run ▶ no Android Studio), com as **duas permissões já concedidas** (rede local e notificações).
- [ ] Logado na conta demo (`demo@casaemdia.com` / `segredo123`) e **depois deslogado**, para começar pela tela de login.
- [ ] Navegador com duas abas abertas: o **README no GitHub** e o **Swagger** (`http://localhost:8080/swagger-ui.html`).
- [ ] Android Studio aberto no projeto, com a árvore de pacotes recolhida (para expandir durante o vídeo).
- [ ] Testes já rodados uma vez, para mostrar o resultado sem esperar: `.\mvnw.cmd test` (30 testes) e `.\gradlew.bat testDebugUnitTest` (16).

**Dados da conta demo**
- [ ] Conferir se o Painel mostra **atrasados**, contas do mês e próximos 7 dias. Se os dados ficaram bagunçados por testes (ex.: o lembrete "Farmacia – R$ 5.000"), restaure a conta demo antes.
- [ ] Lembre que o vídeo será gravado em **27/11**: as datas "andam" um dia. "Levar o lixo" (26/11) passa a aparecer como atrasado e a "Conta de luz" fica "Atrasado há 2 dias". Isso é esperado e até ajuda a mostrar o destaque de atraso.

**Gravação**
- [ ] Ferramenta: **OBS Studio** (captura de tela inteira) ou a **Ferramenta de Captura do Windows** (`Win + Shift + R`, grava uma região com áudio).
- [ ] Ativar o "Não incomodar" do Windows, fechar abas e janelas que não serão usadas, aumentar a fonte do terminal e do Android Studio (zoom ~125%).
- [ ] Testar o microfone com 10 segundos de gravação antes do vídeo valendo.

## 6.2 Roteiro

| # | Tempo | O que mostrar | O que falar (sugestão) | Comprova |
|---|---|---|---|---|
| 1 | 0:00–0:30 | README no GitHub (topo) | "Sou o Gustavo de Souza e este é o Casa em Dia, meu trabalho final de OTES06. É um app Android para organizar a casa: contas, manutenções, lembretes, lista de compras e gastos do mês." | Visão geral |
| 2 | 0:30–1:15 | README › seções "Atendimento aos requisitos" e "Arquitetura" (diagrama) | "O app é Android nativo em Java, com MVVM. Ele conversa por REST, com JSON e JWT, com uma API em Java 21 e Spring Boot, que guarda os dados num H2." | Android nativo, back-end Java, REST |
| 3 | 1:15–2:00 | Terminal com o back-end rodando → Swagger: rolar pelos grupos de endpoints (Autenticação, Usuários, Lembretes, Lista de compras, Painel, Análises) | "Esta é a API rodando. O Swagger documenta os 18 endpoints; todos, menos login e cadastro, exigem o token." | REST, documentação da API |
| 4 | 2:00–2:40 | App: tela de login → **Criar conta** → tocar em Cadastrar com campos vazios (mostra "Campo obrigatório") → voltar → entrar com a conta demo | "Tem cadastro e login com validação. Vou entrar numa conta que já tem dados para a demonstração." | Autenticação |
| 5 | 2:40–3:10 | **Painel**: cartões Atrasados e Para hoje, Contas do mês (pago e pendente), Próximos 7 dias | "O painel resume a casa: o que está atrasado, o que vence hoje, as contas do mês e o que vem nos próximos dias." | Funcionalidade |
| 6 | 3:10–4:40 | **Lembretes**: (a) filtros Contas e Manutenção; (b) buscar "agua" (acha "Água" e "Trocar filtro da água"); (c) **+** → categoria Conta sem valor → Salvar mostra "Contas precisam de um valor" → preencher (ex.: "Gás", R$ 120, todo mês) → Salvar; (d) concluir a **Conta de luz** → aviso "Próximo agendado para 25/12" → tocar em **Desfazer** → concluir de novo; (e) aba Concluídos | "Os lembretes têm categoria e recorrência. Ao concluir uma conta mensal, o sistema já cria a do mês seguinte; se concluí por engano, dá para desfazer. Manutenções podem repetir a cada N dias, contados a partir de quando o serviço foi feito." | Regras de negócio, ponta a ponta |
| 7 | 4:40–5:40 | **Gastos** (aba ou toque em "Contas do mês"): total, variação ▲/▼, pago/pendente, média; Por categoria; Maiores gastos; gráfico dos 6 meses → tocar na coluna de **outubro** (aparecem Contas + Manutenção) → seta para voltar a novembro | "A análise de gastos é calculada no back-end: total do mês, comparação com o mês anterior, divisão por categoria, ranking e histórico. Tocando numa coluna, abro aquele mês." | Funcionalidade, REST |
| 8 | 5:40–6:20 | **Compras**: adicionar "Café" com qtd. 2 → marcar um item como comprado → segurar outro item › Excluir → **Desfazer** → "Limpar comprados" | "A lista de compras separa pendentes e comprados, e a exclusão também pode ser desfeita." | Funcionalidade |
| 9 | 6:20–6:45 | Menu ⋮ › **Verificar lembretes agora** → puxar a barra de notificações | "Todo dia o app verifica os lembretes e avisa no celular; aqui eu disparo a verificação na hora." | Notificação local |
| 10 | 6:45–7:35 | **Código**: Android Studio › `ui/` (uma pasta por tela), `data/` (api, repository, model) → abrir `LembretesViewModel`; depois `backend/` › pacotes por funcionalidade → `LembreteService.concluir` e `GlobalExceptionHandler`; resultado dos testes (30 + 16 passando) | "O código está em camadas: no app, tela, ViewModel e repository; no back-end, controller, service e repository, com DTOs, validação e erros padronizados. São 46 testes automatizados." | Organização do código e boas práticas |
| 11 | 7:35–8:00 | README › "Como executar" e lista da pasta `docs/` | "O README explica como rodar o back-end e o app, e a pasta docs tem a especificação, a arquitetura, o contrato da API e a identidade visual. Obrigado!" | Documentação de build e execução |

## 6.3 Durante a gravação

- Fale devagar e **pause dois segundos** depois de cada ação importante (concluir, desfazer, trocar de mês). Isso ajuda quem assiste e facilita cortar na edição.
- Os avisos com "Desfazer" duram uns 3 segundos: toque logo depois de aparecer.
- Se algo travar (ex.: "System UI isn't responding" no emulador), toque em **Wait** e continue; dá para cortar depois.
- Se o app disser "Não foi possível conectar ao servidor", confira se o back-end ainda está rodando no terminal.

## 6.4 Depois de gravar

- [ ] Assistir uma vez inteira para conferir áudio, cortes e se todos os blocos aparecem.
- [ ] Publicar: **YouTube como "Não listado"** ou **Google Drive** com acesso "Qualquer pessoa com o link".
- [ ] Testar o link numa janela anônima (precisa abrir sem login).
- [ ] Colocar o link no topo do README (linha "Vídeo de demonstração").
- [ ] Commit e push final; conferir o repositório no GitHub.
- [ ] Enviar no Moodle o link do GitHub (e do vídeo, se pedido).

## 6.5 Versão curta (≈ 5 min)

Se precisar encurtar, mantenha os blocos **1, 2, 5, 6, 7 e 10** e corte assim:
- **Bloco 3 (Swagger):** só a lista de endpoints, sem o terminal (20 s).
- **Bloco 4 (cadastro):** entre direto com a conta demo (10 s).
- **Bloco 6 (lembretes):** sem a criação do lembrete novo; mostre só busca, concluir e desfazer (1 min).
- **Blocos 8 e 9 (compras e notificação):** juntos, em 40 s.
- **Bloco 11:** vira uma frase no fim do bloco 10.
