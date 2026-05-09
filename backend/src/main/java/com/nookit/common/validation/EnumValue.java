package com.nookit.common.validation;

import com.nookit.common.enums.BaseEnum;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Objects;

/**
 * 校验字段值是否落在指定枚举的 code 集合内。
 *
 * <pre>
 * &#64;EnumValue(enumClass = StatusEnum.class, message = "状态非法")
 * private String status;
 * </pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EnumValue.Validator.class)
public @interface EnumValue {

    Class<? extends BaseEnum<?>> enumClass();

    String message() default "枚举值不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<EnumValue, Object> {

        private Class<? extends BaseEnum<?>> enumClass;

        @Override
        public void initialize(EnumValue constraintAnnotation) {
            this.enumClass = constraintAnnotation.enumClass();
        }

        @Override
        public boolean isValid(Object value, ConstraintValidatorContext context) {
            if (value == null) {
                return true;
            }
            BaseEnum<?>[] constants = enumClass.getEnumConstants();
            if (constants == null) {
                return false;
            }
            return Arrays.stream(constants)
                    .map(BaseEnum::getCode)
                    .anyMatch(code -> Objects.equals(code, value) || code.toString().equals(value.toString()));
        }
    }
}
