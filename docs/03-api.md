# 3. Contrato da API REST — Casa em Dia

> Contrato entre o app Android e o back-end. Com o back-end rodando, a versão interativa fica em `http://localhost:8080/swagger-ui.html`.

## 3.1 Convenções

| Item | Padrão |
|---|---|
| URL base | `http://localhost:8080/api` (no emulador: `http://10.0.2.2:8080/api`) |
| Formato | JSON, UTF-8 (`Content-Type: application/json`) |
| Autenticação | Header `Authorization: Bearer <token>` em todas as rotas, **exceto** `/api/auth/**` |
| Datas | ISO-8601 `yyyy-MM-dd` (ex.: `"2026-11-28"`); data e hora em `yyyy-MM-dd'T'HH:mm:ss` |
| Valores monetários | Número decimal com 2 casas (ex.: `99.90`) |
| Enums | Texto em maiúsculas (ex.: `"CONTA"`, `"A_CADA_N_DIAS"`) |
| Ids | Números inteiros (`Long`) |

## 3.2 Códigos de status

| Código | Quando |
|---|---|
| 200 OK | Leitura ou alteração bem-sucedida, com corpo |
| 201 Created | Recurso criado (com header `Location`) |
| 204 No Content | Exclusão bem-sucedida |
| 400 Bad Request | JSON malformado, falha de validação dos campos ou parâmetro em formato inválido (ex.: `mes=novembro`) |
| 401 Unauthorized | Token ausente, inválido ou expirado; credenciais erradas no login |
| 404 Not Found | Recurso inexistente **ou de outro usuário** |
| 409 Conflict | E-mail já cadastrado |
| 422 Unprocessable Entity | Regra de negócio violada (ex.: concluir duas vezes, reabrir um lembrete pendente, `CONTA` sem valor) |
| 500 Internal Server Error | Erro inesperado |

## 3.3 Formato de erro

Todas as respostas de erro seguem este formato:

```json
{
  "timestamp": "2026-11-26T15:30:00",
  "status": 400,
  "erro": "Bad Request",
  "mensagem": "Dados inválidos",
  "caminho": "/api/lembretes",
  "campos": [
    { "campo": "titulo", "mensagem": "não deve estar em branco" }
  ]
}
```

O campo `campos` só aparece em erros de validação (400).

---

## 3.4 Autenticação

### `POST /api/auth/cadastro`
Cria o usuário e já devolve o token, então o app entra direto, sem login separado.

**Requisição**
```json
{
  "nome": "Gustavo",
  "nomeCasa": "Casa da Família Souza",
  "email": "gustavo@email.com",
  "senha": "segredo123"
}
```
| Campo | Regras |
|---|---|
| `nome` | obrigatório, até 100 caracteres |
| `nomeCasa` | obrigatório, até 100 caracteres |
| `email` | obrigatório, e-mail válido, até 150 caracteres |
| `senha` | obrigatória, de 6 a 72 caracteres |

**Resposta `201`**: mesmo formato do login (abaixo). **Erros:** 400, 409.

### `POST /api/auth/login`

**Requisição**
```json
{ "email": "gustavo@email.com", "senha": "segredo123" }
```

**Resposta `200`**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEm": "2026-12-03T15:30:00",
  "usuario": {
    "id": 1,
    "nome": "Gustavo",
    "nomeCasa": "Casa da Família Souza",
    "email": "gustavo@email.com"
  }
}
```
**Erros:** 400; 401 com a mensagem `"E-mail ou senha inválidos"`.

### `GET /api/usuarios/me`
Devolve o usuário autenticado, no mesmo formato do objeto `usuario` acima. **Erros:** 401.

---

## 3.5 Lembretes

### Objeto `Lembrete` (resposta)
```json
{
  "id": 7,
  "titulo": "Conta de luz",
  "categoria": "CONTA",
  "dataVencimento": "2026-11-28",
  "valor": 184.50,
  "recorrencia": "MENSAL",
  "intervaloDias": null,
  "observacao": "Celesc - débito não automático",
  "concluido": false,
  "dataConclusao": null,
  "atrasado": false
}
```
O campo `atrasado` é calculado (RN07) e **não** é enviado nas requisições.

### Corpo de requisição (criar e editar)
```json
{
  "titulo": "Trocar filtro da água",
  "categoria": "MANUTENCAO",
  "dataVencimento": "2026-12-01",
  "valor": null,
  "recorrencia": "A_CADA_N_DIAS",
  "intervaloDias": 180,
  "observacao": "Filtro modelo X"
}
```
| Campo | Regras |
|---|---|
| `titulo` | obrigatório, até 100 caracteres |
| `categoria` | obrigatório: `CONTA`, `MANUTENCAO` ou `OUTRO` |
| `dataVencimento` | obrigatório |
| `valor` | opcional e > 0; **obrigatório se `categoria = CONTA`** (422) |
| `recorrencia` | obrigatório: `NENHUMA`, `SEMANAL`, `MENSAL`, `ANUAL` ou `A_CADA_N_DIAS` |
| `intervaloDias` | obrigatório e entre 1 e 3650 **se `recorrencia = A_CADA_N_DIAS`** (422); nos demais casos é ignorado |
| `observacao` | opcional, até 500 caracteres |

### Endpoints

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/lembretes` | Lista os lembretes do usuário, ordenados por `dataVencimento` crescente | 200 `Lembrete[]` | 401 |
| GET | `/api/lembretes/{id}` | Detalha um lembrete | 200 `Lembrete` | 401, 404 |
| POST | `/api/lembretes` | Cria um lembrete pendente | 201 `Lembrete` | 400, 401, 422 |
| PUT | `/api/lembretes/{id}` | Substitui os dados editáveis (não altera `concluido`) | 200 `Lembrete` | 400, 401, 404, 422 |
| DELETE | `/api/lembretes/{id}` | Exclui um lembrete | 204 | 401, 404 |
| PATCH | `/api/lembretes/{id}/concluir` | Conclui e, se for recorrente, cria a próxima ocorrência | 200 `Conclusao` | 401, 404, 422 |
| PATCH | `/api/lembretes/{id}/reabrir` | Desfaz a conclusão: volta a pendente e remove a ocorrência gerada, se ainda pendente (RN18) | 200 `Lembrete` | 401, 404, 422 |

**Parâmetros de consulta de `GET /api/lembretes`**

| Parâmetro | Valores | Padrão |
|---|---|---|
| `categoria` | `CONTA`, `MANUTENCAO`, `OUTRO` | todas |
| `concluido` | `true` ou `false` | `false` (pendentes) |

Exemplo: `GET /api/lembretes?categoria=CONTA&concluido=false`

**Resposta de `PATCH /api/lembretes/{id}/concluir`**
```json
{
  "concluido": { "id": 7, "titulo": "Conta de luz", "concluido": true, "dataConclusao": "2026-11-26", "...": "..." },
  "proximo":   { "id": 12, "titulo": "Conta de luz", "dataVencimento": "2026-12-28", "concluido": false, "...": "..." }
}
```
Quando `recorrencia = NENHUMA`, `proximo` vem como `null`.

**`PATCH /api/lembretes/{id}/reabrir`** é usado pelo botão "Desfazer" do app. Ao concluir, a nova ocorrência guarda o id do lembrete de origem; ao reabrir, essa ocorrência é removida (se ainda estiver pendente) e o original volta a `concluido = false`, `dataConclusao = null`. Reabrir um lembrete pendente responde 422.

---

## 3.6 Lista de compras

### Objeto `ItemCompra`
```json
{ "id": 3, "nome": "Detergente", "quantidade": 2, "comprado": false, "criadoEm": "2026-11-26T10:12:00" }
```

### Corpo de requisição (criar e editar)
```json
{ "nome": "Detergente", "quantidade": 2 }
```
| Campo | Regras |
|---|---|
| `nome` | obrigatório, até 80 caracteres |
| `quantidade` | opcional, inteiro ≥ 1 (padrão 1) |

### Endpoints

| Método | Rota | Descrição | Sucesso | Erros |
|---|---|---|---|---|
| GET | `/api/compras` | Lista os itens: não comprados primeiro, depois por `criadoEm` | 200 `ItemCompra[]` | 401 |
| POST | `/api/compras` | Adiciona um item | 201 `ItemCompra` | 400, 401 |
| PUT | `/api/compras/{id}` | Edita nome e quantidade | 200 `ItemCompra` | 400, 401, 404 |
| PATCH | `/api/compras/{id}/alternar` | Inverte o estado `comprado` | 200 `ItemCompra` | 401, 404 |
| DELETE | `/api/compras/{id}` | Exclui um item | 204 | 401, 404 |
| DELETE | `/api/compras/comprados` | Exclui todos os itens já comprados | 204 | 401 |

---

## 3.7 Painel

### `GET /api/painel`

**Resposta `200`**
```json
{
  "nomeCasa": "Casa da Família Souza",
  "dataReferencia": "2026-11-26",
  "atrasados": [ { "...Lembrete...": "" } ],
  "hoje": [ { "...Lembrete...": "" } ],
  "proximos7Dias": [ { "...Lembrete...": "" } ],
  "itensPendentesCompra": 5,
  "resumoMes": {
    "mes": "2026-11",
    "totalContas": 820.00,
    "totalPago": 500.00,
    "totalPendente": 320.00
  }
}
```
As regras de cada lista estão em RN08 e RN09 ([01-especificacao.md](01-especificacao.md#15-regras-de-negócio)). As três listas trazem apenas lembretes pendentes, ordenados por data.

**Erros:** 401.

---

## 3.8 Análise de gastos

### `GET /api/analises/gastos`

| Parâmetro | Valores | Padrão |
|---|---|---|
| `mes` | mês analisado, `yyyy-MM` | mês atual |
| `meses` | tamanho do histórico, incluindo o mês analisado (limitado a 2..12) | `6` |

Exemplo: `GET /api/analises/gastos?mes=2026-11&meses=6`

**Resposta `200`**
```json
{
  "mes": "2026-11",
  "total": 1700.00,
  "pago": 1365.50,
  "pendente": 334.50,
  "quantidadeLancamentos": 5,
  "mediaMensal": 1450.00,
  "variacaoPercentual": 41.7,
  "historico": [
    { "mes": "2026-06", "total": 0, "pago": 0 },
    { "mes": "2026-10", "total": 1200.00, "pago": 1200.00 },
    { "mes": "2026-11", "total": 1700.00, "pago": 1365.50 }
  ],
  "porCategoria": [
    { "categoria": "CONTA", "valor": 1470.80, "percentual": 87 },
    { "categoria": "MANUTENCAO", "valor": 150.00, "percentual": 9 },
    { "categoria": "OUTRO", "valor": 79.20, "percentual": 5 }
  ],
  "maioresGastos": [
    { "titulo": "Aluguel", "valor": 1200.00, "percentual": 71, "agrupado": false },
    { "titulo": "Outros", "valor": 130.00, "percentual": 8, "agrupado": true }
  ]
}
```
- `historico` traz um item por mês, inclusive os meses sem gasto (total `0`), do mais antigo até o analisado.
- `variacaoPercentual` vem `null` quando o mês anterior não teve gasto.
- `porCategoria` segue sempre a ordem `CONTA`, `MANUTENCAO`, `OUTRO` e omite categorias sem gasto, para cada categoria manter a mesma cor no app.
- `agrupado = true` marca a linha "Outros", que soma o que ficou fora dos 5 maiores.
- As regras de cálculo estão em RN14 a RN17 ([01-especificacao.md](01-especificacao.md#15-regras-de-negócio)).

**Erros:** 400 (`mes` em formato inválido), 401.

---

## 3.9 Resumo dos endpoints

| # | Método | Rota | Autenticado |
|---|---|---|---|
| 1 | POST | `/api/auth/cadastro` | não |
| 2 | POST | `/api/auth/login` | não |
| 3 | GET | `/api/usuarios/me` | sim |
| 4 | GET | `/api/lembretes` | sim |
| 5 | GET | `/api/lembretes/{id}` | sim |
| 6 | POST | `/api/lembretes` | sim |
| 7 | PUT | `/api/lembretes/{id}` | sim |
| 8 | DELETE | `/api/lembretes/{id}` | sim |
| 9 | PATCH | `/api/lembretes/{id}/concluir` | sim |
| 10 | GET | `/api/compras` | sim |
| 11 | POST | `/api/compras` | sim |
| 12 | PUT | `/api/compras/{id}` | sim |
| 13 | PATCH | `/api/compras/{id}/alternar` | sim |
| 14 | DELETE | `/api/compras/{id}` | sim |
| 15 | DELETE | `/api/compras/comprados` | sim |
| 16 | GET | `/api/painel` | sim |
| 17 | PATCH | `/api/lembretes/{id}/reabrir` | sim |
| 18 | GET | `/api/analises/gastos` | sim |
