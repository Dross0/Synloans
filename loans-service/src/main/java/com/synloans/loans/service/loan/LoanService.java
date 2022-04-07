package com.synloans.loans.service.loan;

import com.synloans.loans.AnnuityLoan;
import com.synloans.loans.factory.AnnuityLoanFactory;
import com.synloans.loans.info.LoanInfo;
import com.synloans.loans.model.blockchain.BankJoinRequest;
import com.synloans.loans.model.blockchain.LoanCreateRequest;
import com.synloans.loans.model.blockchain.LoanId;
import com.synloans.loans.model.blockchain.PaymentBlockchainRequest;
import com.synloans.loans.model.dto.NodeUserInfo;
import com.synloans.loans.model.dto.loan.payments.PaymentRequest;
import com.synloans.loans.model.entity.company.Bank;
import com.synloans.loans.model.entity.company.Company;
import com.synloans.loans.model.entity.loan.BlockchainLoanId;
import com.synloans.loans.model.entity.loan.Loan;
import com.synloans.loans.model.entity.loan.LoanRequest;
import com.synloans.loans.model.entity.loan.payment.ActualPayment;
import com.synloans.loans.model.entity.loan.payment.PlannedPayment;
import com.synloans.loans.model.entity.syndicate.SyndicateParticipant;
import com.synloans.loans.model.entity.user.User;
import com.synloans.loans.payment.LoanPayment;
import com.synloans.loans.repository.loan.LoanRepository;
import com.synloans.loans.service.blockchain.BlockchainService;
import com.synloans.loans.service.exception.AcceptPaymentException;
import com.synloans.loans.service.exception.ForbiddenResourceException;
import com.synloans.loans.service.exception.InvalidLoanRequestException;
import com.synloans.loans.service.exception.notfound.LoanNotFoundException;
import com.synloans.loans.service.exception.notfound.LoanRequestNotFoundException;
import com.synloans.loans.service.exception.notfound.NodeNotFoundException;
import com.synloans.loans.service.loan.payment.ActualPaymentService;
import com.synloans.loans.service.loan.payment.PlannedPaymentService;
import com.synloans.loans.service.syndicate.SyndicateParticipantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {
    private static final String CURRENCY_CODE = "RUR";

    private final LoanRepository loanRepository;
    private final LoanRequestService loanRequestService;
    private final SyndicateParticipantService participantService;
    private final PlannedPaymentService plannedPaymentService;
    private final ActualPaymentService actualPaymentService;
    private final BlockchainService blockchainService;

    private final Converter<Company, NodeUserInfo> nodeUserInfoConverter;

    @Transactional
    public Loan getLoanByRequestId(long loanRequestId){
        LoanRequest loanRequest = loanRequestService.getById(loanRequestId)
                .orElseThrow(LoanRequestNotFoundException::new);
        if (loanRequest.getLoan() == null){
            log.error("Кредит с заявкой с id='{}' не найден", loanRequest);
            throw new LoanNotFoundException("Кредит с заявкой с id=" + loanRequestId + " не найден");
        }
        return loanRequest.getLoan();
    }

    @Transactional
    public List<PlannedPayment> getPlannedPaymentsByRequestId(long loanRequestId){
        return getLoanByRequestId(loanRequestId).getPlannedPayments();
    }


    @Transactional
    public List<ActualPayment> getActualPaymentsByRequestId(long loanRequestId) {
        return getLoanByRequestId(loanRequestId).getActualPayments();
    }

    @Transactional
    public Loan startLoanByRequestId(long loanRequestId, User user) {
        LoanRequest loanRequest = loanRequestService.getById(loanRequestId)
                .orElseThrow(LoanRequestNotFoundException::new);
        validateBorrower(user, loanRequest);
        Loan loan = startLoan(loanRequest);
        persistToBlockchain(loan);
        return loan;
    }

    private void persistToBlockchain(Loan loan) {
        Company bankAgentCompany = loan.getBankAgent().getCompany();
        NodeUserInfo bankAgentNode = nodeUserInfoConverter.convert(bankAgentCompany);
        if (bankAgentNode == null){
            log.error("Bank agent={} at loan='{}' without nodes",
                    bankAgentCompany.getFullName(),
                    loan.getRequest().getId()
            );
            throw new NodeNotFoundException("Not found node of bank agent=" + bankAgentCompany.getFullName());
        }

        LoanCreateRequest loanCreateRequest = new LoanCreateRequest();
        loanCreateRequest.setBankAgent(bankAgentNode);
        loanCreateRequest.setLoanSum(loan.getSum());
        loanCreateRequest.setRate(loan.getRate());
        loanCreateRequest.setTerm(loan.getRequest().getTerm());
        loanCreateRequest.setBorrower(loan.getRequest().getCompany().getFullName());
        List<SyndicateParticipant> participants = loan.getRequest()
                .getSyndicate()
                .getParticipants()
                .stream()
                .filter(syndicateParticipant -> syndicateParticipant.getIssuedLoanSum() > 0)
                .collect(Collectors.toList());

        loanCreateRequest.setBanks(
                participants.stream()
                        .map(participant -> participant.getBank().getCompany().getFullName())
                        .collect(Collectors.toList())
        );

        LoanId loanId = blockchainService.createLoan(loanCreateRequest);
        BlockchainLoanId blockchainLoanId = new BlockchainLoanId();
        blockchainLoanId.setExternalId(loanId.getLoanExternalId());
        blockchainLoanId.setPrimaryId(loanId.getId());
        loan.setBlockchainLoanId(blockchainLoanId);

        participants.stream()
                .map(participant -> buildBankJoinRequest(participant, loanId))
                .forEach(blockchainService::joinBank);

    }

    private BankJoinRequest buildBankJoinRequest(SyndicateParticipant participant, LoanId loanId){
        Company bank = participant.getBank().getCompany();
        NodeUserInfo bankNode = nodeUserInfoConverter.convert(bank);
        if (bankNode == null){
            log.error("Participant bank={} without nodes", bank.getFullName());
            throw new NodeNotFoundException("Not found node of bank=" + bank.getFullName());
        }
        return new BankJoinRequest(
                bankNode,
                loanId,
                participant.getIssuedLoanSum()
        );
    }


    @Transactional
    public Loan startLoan(LoanRequest loanRequest){
        validateLoanRequest(loanRequest);
        List<SyndicateParticipant> finalSyndicateParticipants = buildFinalParticipants(loanRequest);
        Bank bankAgent = findBankAgent(finalSyndicateParticipants);
        Loan loan = save(buildLoan(loanRequest, bankAgent));
        plannedPaymentService.save(buildPlannedPayments(loan));
        return loan;
    }

    public Loan save(Loan loan){
        return loanRepository.save(loan);
    }

    @Transactional
    public ActualPayment acceptPayment(long loanRequestId, PaymentRequest paymentRequest, User user) {
        Loan loan = getLoanByRequestId(loanRequestId);
        validateBorrower(user, loan.getRequest());
        try {
            ActualPayment payment = actualPaymentService.createPayment(loan, paymentRequest);
            persistPaymentToBlockchain(loan, payment);
            return payment;
        } catch (Exception e) {
            log.error("Ошибка создания платежа по кредиту с id='{}'", loanRequestId, e);
            throw new AcceptPaymentException("Ошибка создания платежа по кредиту с id=" + loanRequestId, e);
        }
    }

    private void persistPaymentToBlockchain(Loan loan, ActualPayment actualPayment) {
        LoanId loanId = new LoanId(
                loan.getBlockchainLoanId().getExternalId(),
                loan.getBlockchainLoanId().getPrimaryId()
        );

        Company borrower = actualPayment.getLoan().getRequest().getCompany();
        NodeUserInfo payerNode = nodeUserInfoConverter.convert(borrower);
        if (payerNode == null){
            log.error("No nodes for borrower company='{}'", borrower.getFullName());
            throw new NodeNotFoundException("Not found nodes of borrower: " + borrower.getFullName());
        }

        PaymentBlockchainRequest paymentRequest = new PaymentBlockchainRequest(
                payerNode,
                loanId,
                actualPayment.getPayment()
        );

        blockchainService.makePayment(paymentRequest);
    }

    private void validateBorrower(User borrower, LoanRequest loanRequest){
        if (!Objects.equals(loanRequest.getCompany().getId(), borrower.getCompany().getId())) {
            throw new ForbiddenResourceException("Операция выполняется не от создателя заявки");
        }
    }

    private List<PlannedPayment> buildPlannedPayments(Loan loan) {
        BigDecimal rate = BigDecimal.valueOf(loan.getRate() / 100);
        Money loanSum = Money.of(loan.getSum(), CURRENCY_CODE);
        LoanInfo loanInfo = new LoanInfo(
                loanSum,
                rate,
                loan.getRegistrationDate(),
                loan.getRequest().getTerm()
        );
        AnnuityLoan annuityLoan = new AnnuityLoanFactory().create(loanInfo);
        return annuityLoan.getPaymentsList().stream()
                .map(this::toPlannedPayment)
                .map(plannedPayment -> {plannedPayment.setLoan(loan); return plannedPayment;})
                .collect(Collectors.toList());
    }

    private PlannedPayment toPlannedPayment(LoanPayment loanPayment) {
        PlannedPayment plannedPayment = new PlannedPayment();
        plannedPayment.setPrincipal(
                loanPayment.getPaymentSum()
                        .getPrincipalPart()
                        .getNumberStripped()
        );
        plannedPayment.setPercent(
                loanPayment.getPaymentSum()
                        .getPercentPart()
                        .getNumberStripped()
        );
        plannedPayment.setDate(loanPayment.getDate());
        return plannedPayment;
    }

    private Loan buildLoan(LoanRequest loanRequest, Bank bankAgent) {
        LocalDate registrationDate = LocalDate.now();
        Loan loan = new Loan();
        loan.setBankAgent(bankAgent);
        loan.setRequest(loanRequest);
        loan.setRegistrationDate(registrationDate);
        loan.setRate(loanRequest.getRate());
        loan.setCloseDate(registrationDate.plusMonths(loanRequest.getTerm()));
        loan.setSum(loanRequest.getSum());
        return loan;
    }

    private Bank findBankAgent(List<SyndicateParticipant> finalSyndicateParticipants) {
        return finalSyndicateParticipants.stream()
                .filter(SyndicateParticipant::isApproveBankAgent)
                .findFirst()
                .orElse(finalSyndicateParticipants.get(0))
                .getBank();

    }

    private List<SyndicateParticipant> buildFinalParticipants(LoanRequest loanRequest) {
        List<SyndicateParticipant> allSortedParticipants = loanRequest.getSyndicate()
                .getParticipants()
                .stream()
                .sorted(Comparator.comparingLong(SyndicateParticipant::getLoanSum).reversed())
                .collect(Collectors.toList());
        List<SyndicateParticipant> result = new ArrayList<>();
        long sum = 0;
        for (SyndicateParticipant participant: allSortedParticipants){
            if (sum < loanRequest.getSum()) {
                long issueSum = participant.getLoanSum();
                if (sum + participant.getLoanSum() > loanRequest.getSum()) {
                    issueSum = loanRequest.getSum() - sum;
                }
                participant.setIssuedLoanSum(issueSum);
                result.add(participant);
                sum += participant.getLoanSum();
            } else {
                participant.setIssuedLoanSum(null);
            }
        }
        participantService.saveAll(allSortedParticipants);
        return result;
    }

    private void validateLoanRequest(LoanRequest loanRequest) {
        if (loanRequest.getLoan() != null){
            throw new InvalidLoanRequestException("Кредит уже существует");
        }
        long sumFromSyndicate = loanRequestService.calcSumFromSyndicate(loanRequest);
        if (sumFromSyndicate < loanRequest.getSum()){
            throw new InvalidLoanRequestException("Собранной суммы недостаточно для выдачи кредита");
        }
    }
}
