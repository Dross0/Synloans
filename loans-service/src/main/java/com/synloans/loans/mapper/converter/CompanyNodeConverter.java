package com.synloans.loans.mapper.converter;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.node.CompanyNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CompanyNodeConverter implements Converter<Company, NodeUserInfo> {


    @Override
    public NodeUserInfo convert(Company source) {
        if (source.getNodes().isEmpty()){
            return null;
        }

        CompanyNode companyNode = source.getNodes().get(0);

        return new NodeUserInfo(
                companyNode.getAddress(),
                companyNode.getUser(),
                companyNode.getPassword()
        );
    }
}
