package utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonHelper {

    private JsonHelper(){

    }

    public static String asJsonString(final ObjectMapper objectMapper, final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
