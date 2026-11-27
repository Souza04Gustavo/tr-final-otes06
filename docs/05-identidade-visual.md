# 5. Identidade visual — Casa em Dia

Padrão visual do app: **azul-anil em vários tons, superfícies claras e frias, cantos levemente retos e tipografia sóbria**. Este guia vale para toda tela ou componente novo.

Arquivos de referência:
- Cores: [`app/src/main/res/values/colors.xml`](../app/src/main/res/values/colors.xml).
- Cantos: [`values/dimens.xml`](../app/src/main/res/values/dimens.xml).
- Tema e estilos: [`values/themes.xml`](../app/src/main/res/values/themes.xml).

## 5.1 Princípios

1. **Azul é a marca.** O app é predominantemente azul. Outras cores aparecem só com uma função clara: status (atrasado, pendente, queda de gasto) ou identificação de categoria.
2. **Cantos levemente retos.** Nada de pílulas nem cantos de 16 a 28 dp. Há só três tamanhos de canto (seção 5.3).
3. **Limpo.** Cartões brancos com borda fina de 1 dp e **sem sombra**. Fundo azul muito claro. Pouca decoração.
4. **Peso médio em vez de negrito.** Títulos, valores e botões usam `sans-serif-medium`. Os títulos de seção usam caixa alta pequena com espaçamento entre letras.
5. **A cor nunca é a única pista.** Todo status também tem texto ou ícone: "Atrasado há 3 dias", setas ▲/▼, nome da categoria escrito ao lado do filete colorido.
6. **Sem ícone em quadrado pastel.** A categoria de um lembrete é mostrada por um filete na borda e pelo nome escrito, não por um ícone dentro de um fundo colorido claro.

## 5.2 Cores (tokens por função)

Os layouts usam o **nome da função** (`@color/primaria`, `@color/texto`...), nunca o nome do tom. Para mudar a paleta, basta mudar o `colors.xml`.

**Escala de azuis**

| Token | Hex | Uso |
|---|---|---|
| `azul_50` | `#F4F7FC` | Fundo das telas (`fundo`) |
| `azul_100` | `#E4ECF8` | Containers suaves: aba ativa, chip e botão selecionados, cartão "Para hoje" (`primaria_container`) |
| `azul_300` | `#A9C4EC` | Ação sobre fundo escuro: "Desfazer" na Snackbar (`primaria_sobre_escuro`) |
| `azul_500` | `#3D78CC` | Terciária (realces raros) |
| `azul_600` | `#2A61B0` | Secundária; categoria **Conta** |
| `azul_700` | `#1D4A8C` | **Primária**: botões, aba ativa, valores pagos, mês em destaque nos gráficos |
| `azul_800` | `#163566` | Texto sobre os containers azuis (`primaria_escura`) |
| `azul_950` | `#0F1B2D` | Texto principal (`texto`) |

**Neutros, status e categorias**

| Token | Hex | Uso |
|---|---|---|
| `superficie` | `#FFFFFF` | Cartões, campos, barra inferior |
| `texto_secundario` | `#56637A` | Datas, legendas, textos de apoio |
| `texto_rotulo` | `#34425A` | Rótulos de campo e títulos de seção |
| `contorno_suave` | `#D7DEE8` | Borda dos cartões e dos botões contornados |
| `divisor` / `trilha_progresso` | `#E6EBF2` / `#E4EAF3` | Divisórias e trilho das barras |
| `erro` / `erro_container` | `#B42318` / `#FDEDEB` | Atrasado, excluir, aumento de gasto (▲) |
| `pendente` | `#9A5700` | Valor ainda não pago |
| `sucesso` | `#1F7A4D` | Queda de gasto em relação ao mês anterior (▼) |
| `conta` | `#2A61B0` | Categoria Conta (filete do item, gráficos) |
| `manutencao` | `#B25E09` | Categoria Manutenção |
| `outro` | `#00897B` | Categoria Outro |
| `grafico_contexto` | `#C9D4E3` | Colunas dos meses que não estão em destaque |

**Validações feitas**
- **Contraste de texto (WCAG):** todos os pares de texto e fundo têm no mínimo 4,5:1. Por exemplo: `texto` sobre `fundo` dá 16,1:1; `primaria` sobre branco, 8,7:1; `pendente` sobre branco, 5,6:1.
- **Categorias:** as três cores passam na checagem de daltonismo (protanopia e deuteranopia, ΔE ≥ 8), na de distância em visão normal (ΔE ≥ 15) e na de contraste ≥ 3:1. O verde-azulado de "Outro" foi mantido porque um ciano mais próximo do azul da marca reprovaria na distância em visão normal.

## 5.3 Cantos

| Token | Valor | Onde |
|---|---|---|
| `canto_pequeno` | 2 dp | Barras de dados (gráficos, progresso), quantidade "× 2", amostra de cor da legenda |
| `canto_medio` | 4 dp | Botões, campos de texto, chips, caixa de concluir, indicador da aba ativa, Snackbar |
| `canto_grande` | 6 dp | Cartões, diálogos, botão flutuante "+", logo |

O tema mapeia a escala de formas do Material 3 (`shapeAppearanceCorner*`) para esses três tamanhos. Assim, componentes novos já nascem no padrão.

## 5.4 Tipografia

Fonte do sistema (Roboto), em dois pesos: regular (400) e médio (500).

| Estilo | Tamanho | Uso |
|---|---|---|
| `TextAppearance.Casaemdia.TituloTela` | 21 sp, médio | Título da barra superior |
| `TextAppearance.Casaemdia.Subtitulo` | 13 sp, regular | Subtítulo da barra superior |
| `TextAppearance.Casaemdia.Secao` | 12 sp, médio, CAIXA ALTA, espaçamento 0,08 | Títulos de seção ("PRÓXIMOS 7 DIAS", "POR CATEGORIA") |
| `TextAppearance.Casaemdia.Rotulo` | 13 sp, regular | Rótulos ("Total do mês") |
| `TextAppearance.Casaemdia.RotuloCategoria` | 11 sp, médio, CAIXA ALTA, espaçamento 0,08, cor `texto_rotulo` | Nome da categoria no item de lembrete ("CONTA") |
| Valores em destaque | 32–36 sp, médio | Total do mês, contadores do Painel |

## 5.5 Componentes

| Componente | Padrão |
|---|---|
| Cartão | Branco, borda de 1 dp em `contorno_suave`, sem sombra, 6 dp |
| Botão principal | Fundo `primaria`, texto branco médio de 15 sp, altura 48 dp, 4 dp |
| Botão contornado | Borda `contorno_suave`; selecionado (grupo de alternância) com fundo `primaria_container` |
| Botão de excluir | Contornado, texto e ícone em `erro`, borda `erro_contorno` |
| Chip de filtro | 4 dp; selecionado em `primaria_container` com ✓ |
| Item de lembrete | Cartão com **filete de 4 dp** na cor da categoria na borda esquerda; título; linha "CATEGORIA · data · recorrência"; status em texto ("Atrasado há N dias" em `erro`, "Vence hoje" em `primaria`); valor à direita com algarismos de largura fixa; borda em `erro_contorno` quando atrasado |
| Concluir lembrete | Caixa quadrada de 22 dp (4 dp), não círculo |
| Navegação inferior | Fundo branco; aba ativa com indicador retangular em `primaria_container` |
| Snackbar | 4 dp, fundo `superficie_inversa`, ação "Desfazer" em `primaria_sobre_escuro` |
| Gráficos | Mês em destaque em `primaria` e demais em `grafico_contexto`; ranking numa só cor (`primaria`); divisão por categoria com as cores das categorias e vão de 2 dp entre as partes |

## 5.6 O que evitar

- Cantos grandes, pílulas e círculos decorativos (botões, indicadores, caixas de seleção).
- Ícone dentro de quadrado pastel e selos coloridos ("chips" de status). Prefira filete + texto.
- Sombras e elevação em cartões.
- Gradientes, roxos e paletas "padrão" do Material.
- Negrito pesado em todo lugar. O peso médio basta.
- Cor nova sem token no `colors.xml`, ou cor usada só pelo tom (ex.: âmbar para algo que não seja "pendente" ou "manutenção").
