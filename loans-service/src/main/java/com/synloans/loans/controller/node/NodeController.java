package com.synloans.loans.controller.node;

import com.synloans.loans.configuration.api.Api;
import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.node.CompanyNode;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.node.NodeService;
import com.synloans.loans.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Контроллер блокчейн узлов", description = "Служит для обслуживание блокчейн узлов компаний")
@RestController
@RequestMapping(Api.V1 + Api.NODE)
@RequiredArgsConstructor
@Slf4j
public class NodeController { //TODO добавить удаление и изменений узла (идентификатор - адрес)

    private final NodeService nodeService;

    private final UserService userService;

    private final Converter<CompanyNode, NodeUserInfo> nodeConverter;

    @Operation(summary = "Регистрация нового блокчейн узла текущего пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Узел успешно сохранен"
    )

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerNode(
            @Parameter(name = "Информация о блокчейн узле", required = true)
            @RequestBody NodeUserInfo nodeUserInfo
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getCurrentUser(authentication);
        log.info("Register node with address='{}' to user='{}'", nodeUserInfo.getAddress(), currentUser.getUsername());
        nodeService.registerNode(currentUser.getCompany(), nodeUserInfo);
    }


    @Operation(summary = "Получение всех блокчейн узлов текущего пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Узлы успешно получены",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = NodeUserInfo.class))
            )
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NodeUserInfo> getNodes(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getCurrentUser(authentication);
        return nodeService.getCompanyNodes(currentUser.getCompany())
                .stream()
                .map(nodeConverter::convert)
                .collect(Collectors.toList());
    }





}
