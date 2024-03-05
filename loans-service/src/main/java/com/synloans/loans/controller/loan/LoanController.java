package com.synloans.loans.controller.loan;

import com.synloans.loans.configuration.api.Api;
import com.synloans.loans.model.dto.loan.LoanCreateResponse;
import com.synloans.loans.model.dto.loan.payments.ActualPaymentDto;
import com.synloans.loans.model.dto.loan.payments.PaymentRequest;
import com.synloans.loans.model.dto.loan.payments.PlannedPaymentDto;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.service.exception.advice.response.ErrorResponse;
import com.synloans.loans.service.loan.LoanService;
import com.synloans.loans.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Контроллер кредитов", description = "Обслуживание кредита")
@RestController
@RequestMapping(Api.V1 + Api.LOAN)
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    private final UserService userService;

    private final Converter<PlannedPayment, PlannedPaymentDto> plannedPaymentConverter;
    private final Converter<ActualPayment, ActualPaymentDto> actualPaymentConverter;

    @Operation(summary = "Страрт кредита по заявке")
    @ApiResponse(
            responseCode = "200",
            description = "Кредит успешно выдан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoanCreateResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Заявка с таким id не найдена или отсутвует блокчейн узел у участника кредита",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Кредит не может быть создан",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Кредит начат не создателем заявки",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Ошибка сохранения информации о кредите в блокчейн сеть",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping(value = "/{loanRequestId}/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public LoanCreateResponse startLoan(
            @Parameter(name = "id заявки на кредит")
            @PathVariable("loanRequestId") long loanRequestId
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getCurrentUser(authentication);
        Loan loan = loanService.startLoanByRequestId(loanRequestId, currentUser);
        return new LoanCreateResponse(loanRequestId, loan.getId());
    }


    @Operation(summary = "Получение плановых платежей по кредиту")
    @ApiResponse(
            responseCode = "200",
            description = "Успешно получены плановые платежи по кредиту",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = PlannedPaymentDto.class))
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Кредит или заявка не найдены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Получение планновых платежей не участником кредита",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(value = "/{loanRequestId}/payments/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PlannedPaymentDto> getPlannedPayments(
            @Parameter(name = "id кредитной заявки")
            @PathVariable("loanRequestId") long loanRequestId
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getCurrentUser(authentication);
        return loanService.getPlannedPaymentsByRequestId(loanRequestId, currentUser.getCompany())
                .stream()
                .map(plannedPaymentConverter::convert)
                .collect(Collectors.toList());

    }

    @Operation(summary = "Получение фактических платежей по кредиту")
    @ApiResponse(
            responseCode = "200",
            description = "Успешно получены фактические платежи по кредиту",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ActualPaymentDto.class))
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Кредит или заявка не найдены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Получение фактических платежей не участником кредита",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @GetMapping(value = "/{loanRequestId}/payments/actual", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActualPaymentDto> getActualPayments(
            @Parameter(name = "id кредитной заявки")
            @PathVariable("loanRequestId") long loanRequestId
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getCurrentUser(authentication);
        return loanService.getActualPaymentsByRequestId(loanRequestId, currentUser.getCompany())
                .stream()
                .map(actualPaymentConverter::convert)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Внесение платежа по кредиту")
    @ApiResponse(
            responseCode = "200",
            description = "Платеж успешно внесен",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ActualPaymentDto.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Кредит или заявка не найдены",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Отсутвует блокчейн узел у участника кредита",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации тела запроса",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Платеж вноситься не заемщиком",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Ошибка сохранения информации о платеже",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @PostMapping(value = "/{loanRequestId}/pay", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ActualPaymentDto acceptPayment(
            @Parameter(name = "id кредитной заявки")
            @PathVariable("loanRequestId") long loanRequestId,
            @RequestBody @Valid PaymentRequest paymentRequest
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.getCurrentUser(authentication);
        ActualPayment actualPayment = loanService.acceptPayment(loanRequestId, paymentRequest, currentUser);
        return actualPaymentConverter.convert(actualPayment);
    }

}
