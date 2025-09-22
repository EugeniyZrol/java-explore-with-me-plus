package ewm.util.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {EventDateValidator.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEventDate {
    String message() default "Дата и время на которые намечено событие не может быть раньше, чем через {hours} " +
            "часа(ов) от текущего момента";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long hours() default 2;
}
