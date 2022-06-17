package com.synloans.loans.model.dto.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
@EqualsAndHashCode
public class ProfileUpdateRequest {
    @Pattern(regexp = "^(?!\\s*$).+", message = "Короткое название организации не может быть пустым")
    private final String shortName;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Юридический адрес организации не может быть пустым")
    private final String legalAddress;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Фактический адрес организации не может быть пустым")
    private final String actualAddress;

    @Pattern(regexp = "^(?!\\s*$).+", message = "Электронная почта не может быть пустой")
    @Email(message = "Невалидная электронная почта")
    private final String email;

    @JsonCreator
    public ProfileUpdateRequest(
            @JsonProperty("shortName") String shortName,
            @JsonProperty("legalAddress") String legalAddress,
            @JsonProperty("actualAddress") String actualAddress,
            @JsonProperty("email") String email
    ){
        this.shortName = shortName;
        this.actualAddress = actualAddress;
        this.legalAddress = legalAddress;
        this.email = email;
    }
}
