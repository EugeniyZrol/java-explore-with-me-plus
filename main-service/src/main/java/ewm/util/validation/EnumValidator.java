package ewm.util.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private List<String> acceptedValues;
    private boolean ignoreCase;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.ignoreCase = constraintAnnotation.ignoreCase();

        Enum<?>[] enumConstants = constraintAnnotation.enumClass().getEnumConstants();
        this.acceptedValues = Arrays.stream(enumConstants)
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (ignoreCase) {
            return acceptedValues.stream()
                    .anyMatch(enumValue -> enumValue.equalsIgnoreCase(value));
        } else {
            return acceptedValues.contains(value);
        }
    }
}
