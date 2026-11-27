package br.udesc.casaemdia.ui.gastos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import br.udesc.casaemdia.R;

/**
 * Gráfico de colunas do histórico de gastos (um mês por coluna).
 * <p>
 * Usa a forma "destaque": o mês analisado fica na cor primária e os demais em azul acinzentado, e só ele mostra o valor
 * acima da coluna. Tocar em uma coluna avisa o {@link AoSelecionarMes}, que abre aquele mês. Os valores
 * de todos os meses ficam na descrição de acessibilidade, para leitores de tela.
 */
public class GraficoMensalView extends View {

    /** Recebe o índice da coluna tocada. */
    public interface AoSelecionarMes {
        void mesSelecionado(int indice);
    }

    /** Uma coluna: rótulo do eixo ("nov"), valor e textos já formatados. */
    public static final class Coluna {
        final String rotulo;
        final float valor;
        final String valorFormatado;
        final String descricao;

        public Coluna(String rotulo, float valor, String valorFormatado, String descricao) {
            this.rotulo = rotulo;
            this.valor = valor;
            this.valorFormatado = valorFormatado;
            this.descricao = descricao;
        }
    }

    private static final float ALTURA_PADRAO_DP = 180f;
    private static final float LARGURA_MAXIMA_BARRA_DP = 28f;

    private final float dp;
    /** Canto do topo das colunas (escala de cantos do app, values/dimens.xml). */
    private final float raioBarra;
    private final Paint pintaDestaque = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pintaContexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pintaBase = new Paint();
    private final Paint pintaRotulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pintaRotuloDestaque = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pintaValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path caminho = new Path();
    private final RectF retangulo = new RectF();
    /** Raios dos cantos (reaproveitado a cada desenho para não alocar memória no onDraw). */
    private final float[] raios = new float[8];

    private List<Coluna> colunas = Collections.emptyList();
    private int selecionada = -1;
    @Nullable
    private AoSelecionarMes ouvinte;

    public GraficoMensalView(Context context, @Nullable AttributeSet atributos) {
        super(context, atributos);
        dp = getResources().getDisplayMetrics().density;
        raioBarra = getResources().getDimension(R.dimen.canto_pequeno);
        float textoEixo = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
        float textoValor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 13, getResources().getDisplayMetrics());

        pintaDestaque.setColor(ContextCompat.getColor(context, R.color.primaria));
        pintaContexto.setColor(ContextCompat.getColor(context, R.color.grafico_contexto));
        pintaBase.setColor(ContextCompat.getColor(context, R.color.contorno_suave));
        pintaBase.setStrokeWidth(Math.max(1f, dp));

        pintaRotulo.setColor(ContextCompat.getColor(context, R.color.texto_secundario));
        pintaRotulo.setTextSize(textoEixo);
        pintaRotulo.setTextAlign(Paint.Align.CENTER);

        pintaRotuloDestaque.set(pintaRotulo);
        pintaRotuloDestaque.setColor(ContextCompat.getColor(context, R.color.texto));
        pintaRotuloDestaque.setTypeface(Typeface.DEFAULT_BOLD);

        pintaValor.setColor(ContextCompat.getColor(context, R.color.texto));
        pintaValor.setTextSize(textoValor);
        pintaValor.setTypeface(Typeface.DEFAULT_BOLD);
        pintaValor.setTextAlign(Paint.Align.CENTER);
    }

    public void setAoSelecionarMes(@Nullable AoSelecionarMes ouvinte) {
        this.ouvinte = ouvinte;
    }

    /** @param selecionada índice da coluna em destaque (o mês analisado) */
    public void setDados(List<Coluna> colunas, int selecionada) {
        this.colunas = colunas;
        this.selecionada = selecionada;
        StringBuilder descricao = new StringBuilder();
        for (Coluna coluna : colunas) {
            if (descricao.length() > 0) {
                descricao.append("; ");
            }
            descricao.append(coluna.descricao);
        }
        setContentDescription(descricao);
        invalidate();
    }

    @Override
    protected void onMeasure(int larguraSpec, int alturaSpec) {
        int largura = resolveSize(getSuggestedMinimumWidth(), larguraSpec);
        int altura = resolveSize((int) (ALTURA_PADRAO_DP * dp), alturaSpec);
        setMeasuredDimension(largura, altura);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int quantidade = colunas.size();
        if (quantidade == 0) {
            return;
        }

        float esquerda = getPaddingLeft();
        float direita = getWidth() - getPaddingRight();
        // Espaço reservado acima para o valor do mês em destaque e abaixo para os rótulos do eixo
        float topo = getPaddingTop() + pintaValor.getTextSize() + 10 * dp;
        float base = getHeight() - getPaddingBottom() - pintaRotulo.getTextSize() - 12 * dp;
        float alturaUtil = base - topo;
        float faixa = (direita - esquerda) / quantidade;
        float larguraBarra = Math.min(faixa * 0.56f, LARGURA_MAXIMA_BARRA_DP * dp);

        float maximo = 0f;
        for (Coluna coluna : colunas) {
            maximo = Math.max(maximo, coluna.valor);
        }
        if (maximo <= 0f) {
            maximo = 1f;
        }

        for (int i = 0; i < quantidade; i++) {
            Coluna coluna = colunas.get(i);
            float centro = esquerda + faixa * (i + 0.5f);
            float altura = coluna.valor / maximo * alturaUtil;
            if (coluna.valor > 0f) {
                altura = Math.max(altura, 2 * dp); // valores pequenos continuam visíveis
                float raio = Math.min(raioBarra, Math.min(larguraBarra / 2f, altura));
                retangulo.set(centro - larguraBarra / 2f, base - altura, centro + larguraBarra / 2f, base);
                caminho.reset();
                // Cantos arredondados só no topo; a base da coluna fica reta, apoiada no eixo
                Arrays.fill(raios, 0, 4, raio);
                Arrays.fill(raios, 4, 8, 0f);
                caminho.addRoundRect(retangulo, raios, Path.Direction.CW);
                canvas.drawPath(caminho, i == selecionada ? pintaDestaque : pintaContexto);
            }
            canvas.drawText(coluna.rotulo, centro, base + 8 * dp + pintaRotulo.getTextSize(),
                    i == selecionada ? pintaRotuloDestaque : pintaRotulo);
        }

        canvas.drawLine(esquerda, base, direita, base, pintaBase);

        // Rótulo de valor só no mês em destaque, numa faixa acima de todas as colunas (não colide com as
        // vizinhas mais altas) e sem sair das bordas do gráfico
        if (selecionada >= 0 && selecionada < quantidade) {
            String texto = colunas.get(selecionada).valorFormatado;
            float metade = pintaValor.measureText(texto) / 2f;
            float centro = esquerda + faixa * (selecionada + 0.5f);
            float x = Math.max(esquerda + metade, Math.min(direita - metade, centro));
            canvas.drawText(texto, x, topo - 6 * dp, pintaValor);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent evento) {
        if (colunas.isEmpty()) {
            return super.onTouchEvent(evento);
        }
        if (evento.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (evento.getAction() == MotionEvent.ACTION_UP) {
            // A área de toque é a faixa inteira da coluna (largura e altura), não só a barra
            float faixa = (getWidth() - getPaddingLeft() - getPaddingRight()) / (float) colunas.size();
            int indice = (int) ((evento.getX() - getPaddingLeft()) / faixa);
            indice = Math.max(0, Math.min(colunas.size() - 1, indice));
            if (ouvinte != null && indice != selecionada) {
                ouvinte.mesSelecionado(indice);
            }
            performClick();
            return true;
        }
        return super.onTouchEvent(evento);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
