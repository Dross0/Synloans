package com.synloans.loans.model.authentication;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class RegistrationRequest {
    @NotBlank(message = "Полное название организации не может быть пустым")
    private String fullName;

    @NotBlank(message = "Короткое название организации не может быть пустым")
    private String shortName;

    @NotNull(message = "ИНН должен присутствовать")
    @Pattern(regexp = "\\d{10}", message = "ИНН должен состоять из 10 цифр")
    private String inn;

    @NotNull(message = "КПП должен присутствовать")
    @Pattern(regexp = "\\d{9}", message = "КПП должен состоять из 9 цифр")
    private String kpp;

    @NotBlank(message = "Юридический адрес организации не может быть пустым")
    private String legalAddress;

    @NotBlank(message = "Фактический адрес организации не может быть пустым")
    private String actualAddress;

    private boolean creditOrganisation;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Невалидная электронная почта")
    private String email;

    @NotNull(message = "Пароль должен присутсвовать")
    @Size(min = 8, message = "Пароль должен быть не меньше 8 символов")
    private String password;
}
