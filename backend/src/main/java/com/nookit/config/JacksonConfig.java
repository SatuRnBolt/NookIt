package com.nookit.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.nookit.common.constant.CommonConstants;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置：
 * <ul>
 *   <li>{@code Long / long} 序列化为字符串，避免前端 JS 精度丢失</li>
 *   <li>统一时间格式</li>
 *   <li>不输出 {@code null} 字段</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATETIME_PATTERN);
            DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern(CommonConstants.DEFAULT_DATE_PATTERN);
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, new ToStringSerializerNullSafe());
            module.addSerializer(Long.TYPE, new ToStringSerializerNullSafe());

            builder
                    .simpleDateFormat(CommonConstants.DEFAULT_DATETIME_PATTERN)
                    .serializers(new LocalDateTimeSerializer(dateTimeFmt))
                    .serializers(new LocalDateSerializer(dateFmt))
                    .serializers(new LocalTimeSerializer(timeFmt))
                    .modules(module);
        };
    }

    static class ToStringSerializerNullSafe extends JsonSerializer<Long> {
        @Override
        public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeString(value.toString());
            }
        }
    }

    static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        private final DateTimeFormatter formatter;

        LocalDateTimeSerializer(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(formatter));
        }
    }

    static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        private final DateTimeFormatter formatter;

        LocalDateSerializer(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(formatter));
        }
    }

    static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
        private final DateTimeFormatter formatter;

        LocalTimeSerializer(DateTimeFormatter formatter) {
            this.formatter = formatter;
        }

        @Override
        public Class<LocalTime> handledType() {
            return LocalTime.class;
        }

        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.format(formatter));
        }
    }
}
