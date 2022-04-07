package com.synloans.loans.controller.loan;

import com.synloans.loans.mapper.Mapper;
import com.synloans.loans.model.dto.BankParticipantInfo;
import com.synloans.loans.model.dto.CompanyDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestInfo;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import com.synloans.loans.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/loans/requests")
@Slf4j
public class LoanRequestController {

    private final LoanRequestService loanRequestService;
    private final UserService userService;
    private final SyndicateParticipantService syndicateParticipantService;

    private final Converter<SyndicateParticipant, BankParticipantInfo>  syndicateParticipantConverter;
    private final Converter<LoanRequest, LoanRequestInfo> loanRequestConverter;
    private final Mapper<Company, CompanyDto> companyMapper;

    @PostMapping(value = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoanRequestResponse createLoanRequest(
            @RequestBody @Valid LoanRequestDto loanRequestDto,
            Authentication authentication
    ){
        User user = userService.getCurrentUser(authentication);
        LoanRequest loanRequest = loanRequestService.createRequest(
                loanRequestDto,
                user.getCompany()
        );
        return buildResponse(loanRequest);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<LoanRequestResponse> getCompanyRequests(Authentication authentication){
        User user = userService.getCurrentUser(authentication);
        return buildCollectionResponse(user.getCompany().getLoanRequests());
    }


    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LoanRequestResponse getRequestById(@PathVariable("id") Long id, Authentication authentication){
        User user = userService.getCurrentUser(authentication);
        if (user.hasRole(UserRole.ROLE_BANK)){
            return buildResponse(
                    loanRequestService.getById(id)
                            .orElseThrow(LoanRequestNotFoundException::new)
            );
        }
        return buildResponse(getOwnedLoanRequestById(id, authentication));
    }

    @GetMapping(value = "/{id}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<SyndicateParticipant> getSyndicateParticipantsForRequest(@PathVariable("id") Long id){
        return syndicateParticipantService.getSyndicateParticipantsByRequestId(id);
    }

    @Secured(UserRole.ROLE_BANK)
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<LoanRequestResponse> getAllRequests(){
        return buildCollectionResponse(loanRequestService.getAll());
    }

    @DeleteMapping(value = "/{id}")
    public void deleteRequest(@PathVariable("id") Long id,  Authentication authentication){
        getOwnedLoanRequestById(id, authentication);
        loanRequestService.deleteById(id);
    }

    private LoanRequest getOwnedLoanRequestById(long id, Authentication authentication){
        User user = userService.getCurrentUser(authentication);
        return loanRequestService.getOwnedCompanyLoanRequestById(id, user.getCompany());
    }

    private List<LoanRequestResponse> buildCollectionResponse(Collection<LoanRequest> loanRequests){
        return loanRequests.stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    private LoanRequestResponse buildResponse(LoanRequest loanRequest){
        LoanRequestResponse response = new LoanRequestResponse();
        LoanRequestInfo info = loanRequestConverter.convert(loanRequest);
        response.setInfo(info);
        response.setBanks(Collections.emptyList());
        if (loanRequest.getSyndicate() != null){
            response.setBanks(
                    loanRequest.getSyndicate()
                            .getParticipants()
                            .stream()
                            .map(syndicateParticipantConverter::convert)
                            .collect(Collectors.toList())
            );
        }
        response.setBorrower(
                companyMapper.mapFrom(loanRequest.getCompany())
        );

        return response;
    }
}
