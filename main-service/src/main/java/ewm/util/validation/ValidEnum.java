package ewm.util.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    Class<? extends Enum<?>> enumClass();

    String[] values() default {};

    String message() default "Недопустимое значение. Допустимо указать: {accepted}";

    boolean ignoreCase() default true;
}
