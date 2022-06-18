package com.synloans.loans.model.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class NodeUserInfo {

    private final String address;

    private final String user;

    private final String password;

    @JsonCreator
    public NodeUserInfo(
            @JsonProperty(value = "address", required = true) String address,
            @JsonProperty(value = "user", required = true) String user,
            @JsonProperty(value = "password", required = true) String password
    ) {
        this.address = address;
        this.user = user;
        this.password = password;
    }
}
