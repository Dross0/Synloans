package com.synloans.loans.controller.loan;

import com.synloans.loans.model.dto.collection.LoanRequestCollection;
import com.synloans.loans.model.dto.collection.LoanRequestCollectionResponse;
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
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

    private final Converter<LoanRequest, LoanRequestResponse> loanRequestConverter;
    private final Converter<LoanRequestCollection, LoanRequestCollectionResponse> requestCollectionConverter;

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
        return loanRequestConverter.convert(loanRequest);
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
            return loanRequestConverter.convert(
                    loanRequestService.getById(id)
                            .orElseThrow(LoanRequestNotFoundException::new)
            );
        }
        return loanRequestConverter.convert(getOwnedLoanRequestById(id, authentication));
    }

    @GetMapping(value = "/{id}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<SyndicateParticipant> getSyndicateParticipantsForRequest(@PathVariable("id") Long id){
        return syndicateParticipantService.getSyndicateParticipantsByRequestId(id);
    }

    @Secured(UserRole.ROLE_BANK)
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LoanRequestCollectionResponse getAllRequests(Authentication authentication){
        User user = userService.getCurrentUser(authentication);
        return requestCollectionConverter.convert(loanRequestService.getAll(user.getCompany()));
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
                .map(loanRequestConverter::convert)
                .collect(Collectors.toList());
    }
}
