package com.synloans.loans.model.dto.profile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateRequest {
    @Pattern(regexp = "^(?!\\s*$).+", message = "Короткое название организации не может быть пустым")
    private String shortName;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Юридический адрес организации не может быть пустым")
    private String legalAddress;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Фактический адрес организации не может быть пустым")
    private String actualAddress;

    @Email(message = "Невалидная электронная почта")
    private String email;
}
