package br.udesc.casaemdia.data.api;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;

/** Converte {@link LocalDate} de/para o formato ISO "yyyy-MM-dd" usado pela API. */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    @Override
    public void write(JsonWriter out, LocalDate data) throws IOException {
        if (data == null) {
            out.nullValue();
        } else {
            out.value(data.toString());
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return LocalDate.parse(in.nextString());
    }
}
