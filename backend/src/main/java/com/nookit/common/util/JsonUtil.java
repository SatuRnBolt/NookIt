package com.nookit.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * Jackson JSON 工具。Spring 容器外（如线程局部、缓存反序列化）使用本类，
 * 容器内可直接注入 {@link ObjectMapper}。
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonUtil() {
    }

    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("toJson failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return json == null ? null : MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("fromJson failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return json == null ? null : MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.error("fromJson failed: {}", e.getMessage(), e);
            return null;
        }
    }

    public static ObjectMapper mapper() {
        return MAPPER;
    }
}
