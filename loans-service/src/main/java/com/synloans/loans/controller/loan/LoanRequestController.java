package com.synloans.loans.controller.loan;

import com.synloans.loans.model.dto.BankParticipantInfo;
import com.synloans.loans.model.dto.collection.LoanRequestCollection;
import com.synloans.loans.model.dto.collection.LoanRequestCollectionResponse;
import com.synloans.loans.model.dto.loanrequest.LoanRequestDto;
import com.synloans.loans.model.dto.loanrequest.LoanRequestResponse;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.security.UserRole;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.loan.LoanRequestService;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import com.synloans.loans.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "???????????????????? ???????????? ???? ????????????", description = "?????????????????????????? ???????????????? ???? ???????????? ?? ???????????????? ???? ????????????")
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
    private final Converter<SyndicateParticipant, BankParticipantInfo> syndicateParticipantConverter;

    @Operation(summary = "???????????????? ???????????? ???? ????????????")
    @ApiResponse(
            responseCode = "200",
            description = "???????????? ?????????????? ??????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoanRequestResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "???????????? ?????????????????? ???????? ??????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoanRequestResponse createLoanRequest(
            @RequestBody @Valid LoanRequestDto loanRequestDto
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getCurrentUser(authentication);
        LoanRequest loanRequest = loanRequestService.createRequest(
                loanRequestDto,
                user.getCompany()
        );
        return loanRequestConverter.convert(loanRequest);
    }

    @Operation(summary = "?????????????????? ???????? ???????????? ???????????????? ???????????????? ????????????????????????")
    @ApiResponse(
            responseCode = "200",
            description = "???????????????? ?????????????????? ???????? ????????????",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = LoanRequestResponse.class))
            )
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<LoanRequestResponse> getCompanyRequests(Authentication authentication){
        User user = userService.getCurrentUser(authentication);
        return buildCollectionResponse(user.getCompany().getLoanRequests());
    }


    @Operation(
            summary = "?????????????????? ???????????? ???? id",
            description = "???????? ???????????? ?????????????????????? ???????????????? ???????????????? ????????????????????????, ???? ???? ???? ??????????????. " +
                    "???????? ?????????? ???????????????? ?????????? ????????????"
    )
    @ApiResponse(
            responseCode = "200",
            description = "???????????? ?????????????? ????????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoanRequestResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "???????????? ?? ?????????? id ???? ??????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "???????????? ???? ?????????????????????? ???????????????? ???????????????? ????????????????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LoanRequestResponse getRequestById(
            @Parameter(name = "id ???????????? ???? ????????????")
            @PathVariable("id") Long id
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getCurrentUser(authentication);
        if (user.hasRole(UserRole.ROLE_BANK)){
            return loanRequestConverter.convert(
                    loanRequestService.getById(id)
                            .orElseThrow(LoanRequestNotFoundException::new)
            );
        }
        return loanRequestConverter.convert(getOwnedLoanRequestById(id, authentication));
    }


    @Operation(summary = "?????????????????? ???????????????????? ?????????????????? ???? ????????????")
    @ApiResponse(
            responseCode = "200",
            description = "?????????????? ???????????????? ?????????????????? ??????????????????",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = BankParticipantInfo.class))
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "???????????? ?? ?????????? id ???? ??????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(value = "/{id}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Collection<BankParticipantInfo> getSyndicateParticipantsForRequest(
            @Parameter(name = "id ???????????? ???? ????????????")
            @PathVariable("id") Long id
    ){
        return syndicateParticipantService.getSyndicateParticipantsByRequestId(id)
                .stream()
                .map(syndicateParticipantConverter::convert)
                .collect(Collectors.toList());
    }

    @Operation(summary = "?????????????????? ???????????? ???????? ????????????")
    @ApiResponse(
            responseCode = "200",
            description = "?????????????? ???????????????? ?????? ????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoanRequestCollectionResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "???????????????? ???????????????? ???????????????????????? ???? ???????????????? ????????????"
    )
    @Secured(UserRole.ROLE_BANK)
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public LoanRequestCollectionResponse getAllRequests(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getCurrentUser(authentication);
        return requestCollectionConverter.convert(loanRequestService.getAll(user.getCompany()));
    }

    @Operation(summary = "???????????????? ???????????? ???? id")
    @ApiResponse(
            responseCode = "200",
            description = "???????????? ?????????????? ??????????????"
    )
    @ApiResponse(
            responseCode = "404",
            description = "???????????? ?? ?????????? id ???? ??????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "???????????? ???? ?????????????????????? ???????????????? ???????????????? ????????????????????????",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @DeleteMapping(value = "/{id}")
    public void deleteRequest(
            @Parameter(name = "id ???????????? ?????? ????????????????")
            @PathVariable("id") Long id
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
