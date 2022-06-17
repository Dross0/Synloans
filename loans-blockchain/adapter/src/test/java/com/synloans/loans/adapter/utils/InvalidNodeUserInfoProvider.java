package com.synloans.loans.adapter.utils;

import com.synloans.loans.adapter.dto.NodeUserInfo;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class InvalidNodeUserInfoProvider {

    private InvalidNodeUserInfoProvider(){}

    public static List<NodeUserInfo> invalidNodes(){
        return Stream.of(
                new NodeUserInfo(
                        null,
                        "u",
                        "p"
                ),
                new NodeUserInfo(
                        "",
                        "u",
                        "p"
                ),
                new NodeUserInfo(
                        "a",
                        null,
                        "p"
                ),
                new NodeUserInfo(
                        "a",
                        "",
                        "p"
                ),
                new NodeUserInfo(
                        "a",
                        "u",
                        null
                ),
                new NodeUserInfo(
                        "a",
                        "u",
                        null
                )
        ).collect(Collectors.toList());
    }

}
