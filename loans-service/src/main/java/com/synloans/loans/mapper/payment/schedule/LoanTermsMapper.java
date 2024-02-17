package com.synloans.loans.mapper.payment.schedule;

import com.synloans.loans.configuration.Constants;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.schedule.LoanTerms;
import org.javamoney.moneta.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
@Component
public abstract class LoanTermsMapper {

    @Mapping(source = "registrationDate", target = "issueDate")
    @Mapping(source = "request.term", target = "months")
    @Mapping(source = "rate", target = "rate", qualifiedByName = "convertRate")
    @Mapping(source = ".", target = "loanSum", qualifiedByName = "buildLoanSum")
    public abstract LoanTerms convert(Loan loan);

    @Named("convertRate")
    protected BigDecimal convertRate(Double rate) {
        if (rate == null) {
            return null;
        }
        return BigDecimal.valueOf(rate / 100);
    }

    @Named("buildLoanSum")
    protected MonetaryAmount buildLoanSum(Loan loan) {
        return Money.of(loan.getSum(), Constants.CURRENCY_CODE);
    }
}
