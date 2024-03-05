package com.synloans.loans.service.node.impl;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.node.CompanyNode;
import com.synloans.loans.repository.company.CompanyRepository;
import com.synloans.loans.service.node.NodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NodeServiceImpl implements NodeService {

    private final CompanyRepository companyRepository;

    @Transactional
    @Override
    public void registerNode(Company company, NodeUserInfo nodeRequest) {
        Company persistedCompany = companyRepository.save(company);
        CompanyNode node = new CompanyNode();
        node.setAddress(nodeRequest.getAddress());
        node.setUser(nodeRequest.getUser());
        node.setPassword(nodeRequest.getPassword());

        persistedCompany.addNode(node);
    }

    @Transactional
    @Override
    public List<CompanyNode> getCompanyNodes(Company company) {
        company = companyRepository.save(company);

        return company.getNodes();
    }
}
