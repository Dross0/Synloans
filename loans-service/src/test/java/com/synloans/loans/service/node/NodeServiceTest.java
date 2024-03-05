package com.synloans.loans.service.node;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.node.CompanyNode;
import com.synloans.loans.repository.company.CompanyRepository;
import com.synloans.loans.service.node.impl.NodeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NodeServiceImpl.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NodeServiceTest {

    @Autowired
    NodeService nodeService;

    @MockBean
    CompanyRepository companyRepository;

    @Test
    @DisplayName("Тест регистрации узла для компании")
    void registerNodeTest(){
        Company company = new Company();
        company.setNodes(new ArrayList<>());
        NodeUserInfo nodeUserInfo = new NodeUserInfo("address", "admin", "password");

        when(companyRepository.save(company)).thenReturn(company);

        nodeService.registerNode(company, nodeUserInfo);

        assertThat(company.getNodes()).hasSize(1);
        CompanyNode companyNode = company.getNodes().get(0);
        assertThat(companyNode.getCompany()).isEqualTo(company);
        assertThat(companyNode.getAddress()).isEqualTo(nodeUserInfo.getAddress());
        assertThat(companyNode.getUser()).isEqualTo(nodeUserInfo.getUser());
        assertThat(companyNode.getPassword()).isEqualTo(nodeUserInfo.getPassword());

        verify(companyRepository, times(1)).save(company);
    }

    @Test
    @DisplayName("Тест на получение блокчейн узлов компании")
    void getCompanyNodesTest(){
        Company company = new Company();
        List<CompanyNode> nodes = List.of(new CompanyNode(), new CompanyNode());
        company.setNodes(nodes);

        when(companyRepository.save(company)).thenReturn(company);

        List<CompanyNode> companyNodes = nodeService.getCompanyNodes(company);

        assertThat(companyNodes).isEqualTo(nodes);

        verify(companyRepository, times(1)).save(company);
    }

}
