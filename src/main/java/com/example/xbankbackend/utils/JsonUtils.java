package com.example.xbankbackend.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

public class JsonUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T fromReader(Reader reader, Class<T> clazz) {
        try {
            return reader != null ? OBJECT_MAPPER.readValue(reader, clazz) : null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid request payload", e);
        }
    }

    public static <T> void writeValue(PrintWriter writer, T val) {
        try {
            OBJECT_MAPPER.writeValue(writer, val);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid response payload", e);
        }
    }
}
