package com.synloans.loans.model.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthenticationRequest {
    @Email(message = "Невалидная электронная почта")
    private String email;

    @Size(min = 8, message = "Пароль должен быть не меньше 8 символов")
    private String password;
}
