package com.synloans.loans.adapter.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class NodeUserInfo {

    @NotBlank(message = "Node address cant be blank")
    private final String address;

    @NotBlank(message = "Node user cant be blank")
    private final String user;

    @NotBlank(message = "Node password cant be blank")
    private final String password;

}
