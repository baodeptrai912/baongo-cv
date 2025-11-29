package com.example.baoNgoCv.service.validationService;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UniqueUsernameValidator.class)
public @interface ValidUniqueUsername {
    String message() default "Username is already taken, please choose another";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
