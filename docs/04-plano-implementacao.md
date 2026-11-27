# 4. Plano de Implementação — Casa em Dia

> Ordem de construção, estimativas, critérios de corte, situação atual e checklist de entrega. Prazo: **27/11, à noite**. Equipe: 1 pessoa.

## 4.1 Estratégia

1. **Back-end primeiro, testado pelo Swagger.** Quando o Android começar, a API já está pronta e estável.
2. **Uma fatia vertical por vez no Android**: login → painel → lembretes → compras → notificações. Ao fim de cada fatia, o app funciona de ponta a ponta.
3. **Commit a cada etapa concluída**, para ter sempre uma versão entregável no GitHub.
4. Se o tempo apertar, os itens são cortados na ordem da seção 4.4, nunca o núcleo.

## 4.2 Etapas

### Fase 1: Back-end (≈ 5 h)

| # | Etapa | Est. | Pronto quando |
|---|---|---|---|
| B1 | Gerar o projeto (Spring Boot, Java 21, Maven); dependências web, data-jpa, validation, security, oauth2-resource-server, h2, springdoc | 20 min | `./mvnw spring-boot:run` sobe na porta 8080 |
| B2 | `common/exception` + `GlobalExceptionHandler` + `ClockConfig` + `OpenApiConfig` | 30 min | Swagger abre em `/swagger-ui.html` |
| B3 | `Usuario` + `SecurityConfig` + `TokenService` + `AuthService` + `AuthController` + `/usuarios/me` | 1 h 15 | Cadastro e login devolvem o token; `/me` com token responde 200 e sem token, 401 |
| B4 | `CalculadoraRecorrencia` + **testes unitários** | 30 min | `CalculadoraRecorrenciaTest` passa |
| B5 | Lembretes: entidade, repositório, service (RN03 a RN07), controller e DTOs + `LembreteServiceTest` | 1 h 30 | CRUD e concluir funcionando pelo Swagger |
| B6 | Compras: entidade, repositório, service, controller e DTOs | 40 min | Endpoints 10 a 15 funcionando |
| B7 | Painel: service + controller + `PainelServiceTest` | 30 min | `/api/painel` com os dados corretos |

### Fase 2: Android (≈ 7 h)

| # | Etapa | Est. | Pronto quando |
|---|---|---|---|
| A1 | Projeto *Empty Views Activity* em Java, minSdk 26; dependências (Retrofit, Gson, OkHttp logging, Material, Lifecycle, Navigation, WorkManager); `network_security_config`; `BuildConfig.BASE_URL` | 40 min | App compila e abre no emulador |
| A2 | `ApiClient`, `AuthInterceptor`, `LocalDateAdapter`, `SessionManager`, `Resource` | 40 min | — |
| A3 | `LoginActivity`, `CadastroActivity` e `AuthViewModel` | 1 h | Login e cadastro reais contra a API; sessão persiste entre aberturas |
| A4 | `MainActivity` com navegação inferior + menu Sair | 30 min | Troca entre as abas; Sair volta ao Login |
| A5 | Painel: cartões + lista dos próximos 7 dias + puxar para atualizar | 1 h | Dados reais do `/api/painel` |
| A6 | Lembretes: lista com filtros, destaque de atrasado e botão concluir + formulário de criar, editar e excluir | 2 h | RF04 a RF10 funcionando |
| A7 | Compras: adicionar, marcar, excluir, limpar comprados (e editar, se der tempo) | 1 h | RF11 a RF14 funcionando |
| A8 | Notificações: canal, permissão, `LembreteWorker`, agendamento + "Verificar agora" | 45 min | A notificação aparece no emulador |

### Fase 3: Entrega (≈ 2 h)

| # | Etapa | Est. |
|---|---|---|
| E1 | Revisar o README (build e execução testados do zero) e incluir prints das telas | 40 min |
| E2 | Popular dados de demonstração e gravar o vídeo ([roteiro](06-roteiro-video.md)) | 1 h |
| E3 | Subir o vídeo, colocar o link no README, fazer o push final e conferir o repositório no GitHub | 20 min |

**Total estimado: cerca de 14 horas.**

## 4.3 Cronograma sugerido

| Quando | O quê |
|---|---|
| 26/11, tarde e noite | Fase 1 inteira (B1 a B7) e, se sobrar fôlego, A1 e A2 |
| 27/11, manhã | A3 a A6 |
| 27/11, tarde | A7, A8 e E1 |
| 27/11, início da noite | E2, E3 e **entrega** |

## 4.4 Ordem de corte (se faltar tempo)

Corte de cima para baixo. O núcleo (autenticação, lembretes com recorrência, compras básicas e painel) **não é cortado**.

1. RF16: editar item de compra (o usuário pode excluir e adicionar de novo).
2. Testes de `PainelServiceTest` (manter os de recorrência, que são o diferencial).
3. RF17 e RF18: notificações. Sem elas, o resto do app não é afetado.
4. Puxar para atualizar no painel (recarregar no `onResume` resolve).

## 4.5 Situação (26/11)

As fases 1 e 2 estão **concluídas e testadas no emulador**. Além do planejado, entraram estes extras:

| # | Extra | Requisitos |
|---|---|---|
| X1 | Busca de lembretes por texto | RF19 |
| X2 | "Desfazer" ao concluir um lembrete (rota `PATCH /api/lembretes/{id}/reabrir`) e ao excluir um item de compra | RF20, RF21 |
| X3 | Análise de gastos com gráficos: resumo do mês, por categoria, maiores gastos e histórico de 6 meses (`GET /api/analises/gastos`) | RF22, RF23 |
| X4 | Identidade visual própria em azul ([05-identidade-visual.md](05-identidade-visual.md)) | — |

Nenhum item da ordem de corte (4.4) precisou ser cortado. Os testes são 30 no back-end e 16 no app.

## 4.6 Roteiro do vídeo

O plano completo da gravação está em [06-roteiro-video.md](06-roteiro-video.md).

## 4.7 Checklist de entrega

- [x] `backend/` compila e roda com `./mvnw spring-boot:run` (testado com o JDK 21+ do Android Studio)
- [x] `./mvnw test` passa (30 testes) e `./gradlew testDebugUnitTest` passa (16 testes)
- [x] O app compila pelo Gradle (`./gradlew assembleDebug`) e roda no emulador contra o back-end
- [ ] Conferir abrindo a raiz do repositório no Android Studio e rodando com **Run ▶**
- [x] README com: atendimento aos requisitos, descrição funcional, como usar, stack, arquitetura e boas práticas, pré-requisitos, build, execução, testes e problemas comuns
- [x] Nenhum arquivo sensível ou gerado no repositório (`data/`, `build/`, `target/`, `local.properties`, `.idea/` estão no `.gitignore`)
- [ ] Vídeo gravado e publicado com link acessível sem login
- [ ] Link do vídeo no README
- [ ] Commit e push final no GitHub
- [ ] Link do GitHub enviado no Moodle
