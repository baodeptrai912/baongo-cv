package com.example.baoNgoCv.service.validationService;

import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.service.domainService.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UniqueEmailValidator implements ConstraintValidator<ValidUniqueEmail, String> {

    @Autowired
    private UserService userService;

    @Override
    public void initialize(ValidUniqueEmail  constraintAnnotation) {

    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {

        Optional<User> existingUser = userService.findByEmail(email);
        return !existingUser.isPresent();
    }
}
