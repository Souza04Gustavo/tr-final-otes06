package br.udesc.casaemdia.ui.gastos;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.udesc.casaemdia.R;

/**
 * Barra fina dividida em partes proporcionais (distribuição do mês por categoria).
 * <p>
 * Não tem texto: a lista logo abaixo traz o nome, o valor e o percentual de cada parte. As partes são
 * separadas por um vão de 2dp (a cor do cartão aparece entre elas) e só as pontas da barra têm cantos (2dp).
 */
public class BarraSegmentadaView extends View {

    /** Uma parte da barra. */
    public static final class Segmento {
        @ColorInt
        final int cor;
        final float valor;

        public Segmento(@ColorInt int cor, float valor) {
            this.cor = cor;
            this.valor = valor;
        }
    }

    private static final float ALTURA_DP = 10f;
    private static final float VAO_DP = 2f;

    private final float dp;
    /** Canto das pontas da barra (escala de cantos do app, values/dimens.xml). */
    private final float raioBarra;
    private final Paint pinta = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path caminho = new Path();
    private final RectF retangulo = new RectF();
    /** Raios dos cantos (reaproveitado a cada desenho para não alocar memória no onDraw). */
    private final float[] raios = new float[8];
    private List<Segmento> segmentos = Collections.emptyList();

    public BarraSegmentadaView(Context context, @Nullable AttributeSet atributos) {
        super(context, atributos);
        dp = getResources().getDisplayMetrics().density;
        raioBarra = getResources().getDimension(R.dimen.canto_pequeno);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO); // a lista ao lado já descreve os valores
    }

    public void setSegmentos(List<Segmento> novos) {
        List<Segmento> visiveis = new ArrayList<>();
        for (Segmento segmento : novos) {
            if (segmento.valor > 0f) {
                visiveis.add(segmento);
            }
        }
        segmentos = visiveis;
        invalidate();
    }

    @Override
    protected void onMeasure(int larguraSpec, int alturaSpec) {
        int largura = resolveSize(getSuggestedMinimumWidth(), larguraSpec);
        int altura = resolveSize((int) (ALTURA_DP * dp) + getPaddingTop() + getPaddingBottom(), alturaSpec);
        setMeasuredDimension(largura, altura);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (segmentos.isEmpty()) {
            return;
        }
        float total = 0f;
        for (Segmento segmento : segmentos) {
            total += segmento.valor;
        }

        float topo = getPaddingTop();
        float base = getHeight() - getPaddingBottom();
        float inicio = getPaddingLeft();
        float vao = VAO_DP * dp;
        float disponivel = getWidth() - getPaddingLeft() - getPaddingRight() - vao * (segmentos.size() - 1);
        float raio = raioBarra;

        float x = inicio;
        for (int i = 0; i < segmentos.size(); i++) {
            Segmento segmento = segmentos.get(i);
            float largura = segmento.valor / total * disponivel;
            float raioEsquerdo = i == 0 ? Math.min(raio, largura / 2f) : 0f;
            float raioDireito = i == segmentos.size() - 1 ? Math.min(raio, largura / 2f) : 0f;

            retangulo.set(x, topo, x + largura, base);
            caminho.reset();
            // Ordem: superior esquerdo, superior direito, inferior direito, inferior esquerdo (x, y de cada um)
            raios[0] = raioEsquerdo;
            raios[1] = raioEsquerdo;
            raios[2] = raioDireito;
            raios[3] = raioDireito;
            raios[4] = raioDireito;
            raios[5] = raioDireito;
            raios[6] = raioEsquerdo;
            raios[7] = raioEsquerdo;
            caminho.addRoundRect(retangulo, raios, Path.Direction.CW);
            pinta.setColor(segmento.cor);
            canvas.drawPath(caminho, pinta);
            x += largura + vao;
        }
    }
}
