package com.synloans.loans.controller.node;

import com.synloans.loans.controller.BaseControllerTest;
import com.synloans.loans.mapper.converter.CompanyNodeConverter;
import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.node.CompanyNode;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.node.NodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import utils.JsonHelper;
import utils.NoConvertersFilter;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.blankString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = NodeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM, classes = NoConvertersFilter.class)
)
class NodeControllerTest extends BaseControllerTest {

    public static final String BASE_PATH = "/nodes";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    NodeService nodeService;

    @MockBean
    CompanyNodeConverter nodeUserInfoConverter;

    @Captor
    ArgumentCaptor<Authentication> authenticationArgumentCaptor;

    @Test
    @DisplayName("Регистрация нового узла")
    @WithMockUser
    void registerNodeTest() throws Exception {
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, nodeUserInfo))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(blankString()));


        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());

        Authentication authentication = authenticationArgumentCaptor.getValue();
        assertThat(authentication.getName()).isEqualTo("user");

        verify(nodeService, times(1)).registerNode(company, nodeUserInfo);
    }

    @Test
    @DisplayName("Регистрация нового узла без авторизации")
    void registerNodeAccessDeniedTest() throws Exception {
        NodeUserInfo nodeUserInfo = new NodeUserInfo(
                "address-test",
                "user-test",
                "password-test"
        );

        mockMvc.perform(
                post(BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonHelper.asJsonString(objectMapper, nodeUserInfo))
        )
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));
    }

    @Test
    @DisplayName("Получение списка узлов компании")
    @WithMockUser
    void getNodesTest() throws Exception {
        NodeUserInfo nodeUserInfo1 = new NodeUserInfo(
                "address-test1",
                "user-test1",
                "password-test1"
        );
        NodeUserInfo nodeUserInfo2 = new NodeUserInfo(
                "address-test2",
                "user-test2",
                "password-test2"
        );

        CompanyNode companyNode1 = new CompanyNode();
        CompanyNode companyNode2 = new CompanyNode();

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(nodeService.getCompanyNodes(company)).thenReturn(List.of(companyNode1, companyNode2));

        when(nodeUserInfoConverter.convert(companyNode1)).thenReturn(nodeUserInfo1);
        when(nodeUserInfoConverter.convert(companyNode2)).thenReturn(nodeUserInfo2);

        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].address", is(nodeUserInfo1.getAddress())),
                        jsonPath("$[0].user", is(nodeUserInfo1.getUser())),
                        jsonPath("$[0].password", is(nodeUserInfo1.getPassword())),
                        jsonPath("$[1].address", is(nodeUserInfo2.getAddress())),
                        jsonPath("$[1].user", is(nodeUserInfo2.getUser())),
                        jsonPath("$[1].password", is(nodeUserInfo2.getPassword()))
                );

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());

        Authentication authentication = authenticationArgumentCaptor.getValue();
        assertThat(authentication.getName()).isEqualTo("user");

        verify(nodeService, times(1)).getCompanyNodes(company);
        verify(nodeUserInfoConverter, times(1)).convert(companyNode1);
        verify(nodeUserInfoConverter, times(1)).convert(companyNode2);
    }

    @Test
    @DisplayName("Получение пустого списка узлов компании")
    @WithMockUser
    void getNodesEmptyListTest() throws Exception {

        User user = new User();
        Company company = new Company();
        user.setCompany(company);

        when(nodeService.getCompanyNodes(company)).thenReturn(Collections.emptyList());
        when(userService.getCurrentUser(any(Authentication.class))).thenReturn(user);

        mockMvc.perform(get(BASE_PATH ))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).getCurrentUser(authenticationArgumentCaptor.capture());

        Authentication authentication = authenticationArgumentCaptor.getValue();
        assertThat(authentication.getName()).isEqualTo("user");

        verify(nodeService, times(1)).getCompanyNodes(company);
        verify(nodeUserInfoConverter, never()).convert(any());
    }

    @Test
    @DisplayName("Получение узлов компании без авторизации")
    void getCompanyNodesAccessDeniedTest() throws Exception {

        mockMvc.perform(get(BASE_PATH))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(content().string(blankOrNullString()));
    }
}
