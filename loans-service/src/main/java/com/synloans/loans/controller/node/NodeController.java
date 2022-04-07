package com.synloans.loans.controller.node;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.node.NodeService;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nodes")
@RequiredArgsConstructor
@Slf4j
public class NodeController {

    private final NodeService nodeService;

    private final UserService userService;

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerNode(
            @RequestBody NodeUserInfo nodeUserInfo,
            Authentication authentication
    ){
        User currentUser = userService.getCurrentUser(authentication);
        log.info("Register node with address='{}' to user='{}'", nodeUserInfo.getAddress(), currentUser.getUsername());
        nodeService.registerNode(currentUser.getCompany(), nodeUserInfo);
    }

}
