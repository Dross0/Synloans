package com.synloans.loans.model.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@Builder
public class Profile {
    @Pattern(regexp = "^(?!\\s*$).+", message = "Полное название организации не может быть пустым")
    private String fullName;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Короткое название организации не может быть пустым")
    private String shortName;

    @Pattern(regexp = "\\d{10}", message = "ИНН должен состоять из 10 цифр")
    private String inn;

    @Pattern(regexp = "\\d{9}", message = "КПП должен состоять из 9 цифр")
    private String kpp;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Юридический адрес организации не может быть пустым")
    private String legalAddress;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Фактический адрес организации не может быть пустым")
    private String actualAddress;

    @Email(message = "Невалидная электронная почта")
    private String email;

    private boolean creditOrganisation;
}
