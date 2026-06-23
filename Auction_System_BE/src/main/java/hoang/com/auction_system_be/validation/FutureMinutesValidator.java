package hoang.com.auction_system_be.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class FutureMinutesValidator implements ConstraintValidator<FutureMinutes, LocalDateTime> {

    private int minMinutes;

    @Override
    public void initialize(FutureMinutes annotation) {
        this.minMinutes = annotation.minMinutes();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext ctx) {
        if (value == null) {
            // @NotNull handles null separately
            return true;
        }
        LocalDateTime threshold = LocalDateTime.now().plusMinutes(minMinutes);
        return value.isAfter(threshold);
    }
}
