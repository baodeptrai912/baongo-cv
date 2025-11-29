package com.example.baoNgoCv.service.validationService;

import com.example.baoNgoCv.model.dto.common.PasswordConfirmation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordConfirmation> {

    @Override
    public boolean isValid(PasswordConfirmation request, ConstraintValidatorContext context) {
        if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            // Tắt thông báo lỗi mặc định
            context.disableDefaultConstraintViolation();

            // Xây dựng thông báo lỗi mới và gắn nó vào trường "confirmPassword"
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword") // Gắn lỗi vào trường này
                    .addConstraintViolation();

            return false;
        }
        return true;
    }
}