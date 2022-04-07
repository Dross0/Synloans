package com.synloans.loans.service.node;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;

public interface NodeService {

    void registerNode(Company company, NodeUserInfo nodeRequest);

}
