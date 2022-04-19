package com.synloans.loans.mapper.converter;

import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.entity.node.CompanyNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CompanyNodeConverter implements Converter<CompanyNode, NodeUserInfo> {


    @Override
    public NodeUserInfo convert(CompanyNode source) {
        return new NodeUserInfo(
                source.getAddress(),
                source.getUser(),
                source.getPassword()
        );
    }
}
