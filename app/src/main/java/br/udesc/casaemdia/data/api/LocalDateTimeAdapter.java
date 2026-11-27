package br.udesc.casaemdia.data.api;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;

/** Converte {@link LocalDateTime} de/para o formato ISO "yyyy-MM-ddTHH:mm:ss" usado pela API. */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    @Override
    public void write(JsonWriter out, LocalDateTime dataHora) throws IOException {
        if (dataHora == null) {
            out.nullValue();
        } else {
            out.value(dataHora.toString());
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return LocalDateTime.parse(in.nextString());
    }
}
