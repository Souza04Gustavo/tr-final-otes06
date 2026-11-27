package br.udesc.casaemdia.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import br.udesc.casaemdia.BuildConfig;
import br.udesc.casaemdia.data.session.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Ponto único de configuração do Retrofit. Inicializado uma vez em {@code CasaEmDiaApp}.
 */
public final class ApiClient {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private static Retrofit retrofit;

    private ApiClient() {
    }

    public static synchronized void inicializar(SessionManager sessionManager) {
        if (retrofit != null) {
            return;
        }

        // BASIC registra método, URL, status e tempo, sem expor senhas ou tokens no log
        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BASIC : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient cliente = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(sessionManager))
                .addInterceptor(log)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build();
    }

    public static Gson gson() {
        return GSON;
    }

    public static AuthApi auth() {
        return obter().create(AuthApi.class);
    }

    public static LembreteApi lembretes() {
        return obter().create(LembreteApi.class);
    }

    public static CompraApi compras() {
        return obter().create(CompraApi.class);
    }

    public static PainelApi painel() {
        return obter().create(PainelApi.class);
    }

    public static AnaliseApi analises() {
        return obter().create(AnaliseApi.class);
    }

    private static Retrofit obter() {
        if (retrofit == null) {
            throw new IllegalStateException("ApiClient não foi inicializado");
        }
        return retrofit;
    }
}
