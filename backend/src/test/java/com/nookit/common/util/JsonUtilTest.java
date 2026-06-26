package com.nookit.common.util;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JsonUtilTest {

    @Test
    void serializesAndDeserializesMap() {
        Map<String, Object> source = Map.of("name", "nook", "count", 3);
        String json = JsonUtil.toJson(source);

        Map<String, Object> parsed = JsonUtil.fromJson(json, new TypeReference<>() {
        });
        assertEquals("nook", parsed.get("name"));
        assertEquals(3, parsed.get("count"));
    }

    @Test
    void deserializesToClass() {
        String json = JsonUtil.toJson(List.of("a", "b"));
        List<?> parsed = JsonUtil.fromJson(json, List.class);
        assertEquals(List.of("a", "b"), parsed);
    }

    @Test
    void nullJsonReturnsNull() {
        assertNull(JsonUtil.fromJson(null, Map.class));
        assertNull(JsonUtil.fromJson(null, new TypeReference<Map<String, Object>>() {
        }));
    }

    @Test
    void invalidJsonReturnsNullInsteadOfThrowing() {
        assertNull(JsonUtil.fromJson("{not valid json", Map.class));
    }
}
