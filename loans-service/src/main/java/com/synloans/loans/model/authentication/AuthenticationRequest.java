package com.synloans.loans.model.authentication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@EqualsAndHashCode
public class AuthenticationRequest {

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Невалидная электронная почта")
    private final String email;

    @NotNull(message = "Пароль должен присутсвовать")
    @Size(min = 8, message = "Пароль должен быть не меньше 8 символов")
    private final String password;

    @JsonCreator
    public AuthenticationRequest(
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "password", required = true) String password
    ){
        this.email = email;
        this.password = password;
    }
}
