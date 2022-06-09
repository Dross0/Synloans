package com.synloans.loans.controller.node;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.node.CompanyNode;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.node.NodeService;
import com.synloans.loans.service.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NodeController.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NodeControllerTest {

    @Autowired
    NodeController nodeController;

    @MockBean
    NodeService nodeService;

    @MockBean
    UserService userService;

    @MockBean
    Converter<CompanyNode, NodeUserInfo> nodeConverter;

    @Test
    @DisplayName("Тест на регистрацию блокчейн узла")
    void registerNodeTest(){
        Authentication authentication = Mockito.mock(Authentication.class);
        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        NodeUserInfo nodeUserInfo = new NodeUserInfo("address", "admin", "password");

        when(userService.getCurrentUser(authentication)).thenReturn(user);

        nodeController.registerNode(nodeUserInfo, authentication);

        verify(userService, times(1)).getCurrentUser(authentication);
        verify(nodeService, times(1)).registerNode(company, nodeUserInfo);
    }

    @Test
    @DisplayName("Тест на получение блокчейн узлов компании")
    void getNodesTest(){
        Authentication authentication = Mockito.mock(Authentication.class);
        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        CompanyNode node1 = new CompanyNode();
        CompanyNode node2 = new CompanyNode();

        NodeUserInfo convertedNode1 = new NodeUserInfo("a1", "b1", "c1");
        NodeUserInfo convertedNode2 = new NodeUserInfo("a2", "b2", "c2");

        when(nodeService.getCompanyNodes(company)).thenReturn(List.of(node1, node2));
        when(nodeConverter.convert(node1)).thenReturn(convertedNode1);
        when(nodeConverter.convert(node2)).thenReturn(convertedNode2);

        when(userService.getCurrentUser(authentication)).thenReturn(user);

        List<NodeUserInfo> nodes = nodeController.getNodes(authentication);

        assertThat(nodes).containsExactly(convertedNode1, convertedNode2);

        verify(userService, times(1)).getCurrentUser(authentication);
        verify(nodeService, times(1)).getCompanyNodes(company);
        verify(nodeConverter, times(1)).convert(node1);
        verify(nodeConverter, times(1)).convert(node2);
    }
}
