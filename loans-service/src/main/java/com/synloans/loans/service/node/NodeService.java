package com.synloans.loans.service.node;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.node.CompanyNode;

import java.util.List;

public interface NodeService {

    void registerNode(Company company, NodeUserInfo nodeRequest);

    List<CompanyNode> getCompanyNodes(Company company);
}
