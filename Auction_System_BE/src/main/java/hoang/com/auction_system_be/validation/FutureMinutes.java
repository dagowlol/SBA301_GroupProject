package hoang.com.auction_system_be.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that a LocalDateTime field is at least {@code minMinutes} minutes
 * in the future from now. Use instead of plain @Future for APIs requiring a
 * scheduling buffer (e.g. the Scheduler needs time to load session config).
 */
@Documented
@Constraint(validatedBy = FutureMinutesValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureMinutes {
    /** Minimum number of minutes from now */
    int minMinutes() default 5;

    String message() default "Start time must be at least {minMinutes} minutes from now";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
