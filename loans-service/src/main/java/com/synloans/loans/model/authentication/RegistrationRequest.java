package com.synloans.loans.model.authentication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegistrationRequest {
    @NotBlank(message = "Полное название организации не может быть пустым")
    private String fullName;

    @NotBlank(message = "Короткое название организации не может быть пустым")
    private String shortName;

    @Pattern(regexp = "\\d{10}", message = "ИНН должен состоять из 10 цифр")
    private String inn;

    @Pattern(regexp = "\\d{9}", message = "КПП должен состоять из 9 цифр")
    private String kpp;

    @NotBlank(message = "Юридический адрес организации не может быть пустым")
    private String legalAddress;

    @NotBlank(message = "Фактический адрес организации не может быть пустым")
    private String actualAddress;

    private boolean creditOrganisation;

    @Email(message = "Невалидная электронная почта")
    private String email;

    @Size(min = 8, message = "Пароль должен быть не меньше 8 символов")
    private String password;
}
