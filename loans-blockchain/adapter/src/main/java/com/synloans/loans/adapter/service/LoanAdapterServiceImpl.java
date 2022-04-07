package com.synloans.loans.adapter.service;

import com.synloans.loans.CreateLoanFlow;
import com.synloans.loans.LoanPaymentFlow;
import com.synloans.loans.SyndicateParticipantFlow;
import com.synloans.loans.adapter.connection.ConnectionFactory;
import com.synloans.loans.adapter.dto.*;
import com.synloans.loans.adapter.exception.BankJoinException;
import com.synloans.loans.adapter.exception.LoanCreateException;
import com.synloans.loans.adapter.exception.PartyResolveException;
import com.synloans.loans.adapter.exception.PaymentException;
import com.synloans.loans.states.LoanPaymentState;
import com.synloans.loans.states.LoanState;
import com.synloans.loans.states.SyndicateParticipantState;
import lombok.extern.slf4j.Slf4j;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.transactions.SignedTransaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.synloans.loans.adapter.utils.PartyResolveUtils.resolveParties;
import static com.synloans.loans.adapter.utils.PartyResolveUtils.resolveParty;

@Slf4j
@Service
public class LoanAdapterServiceImpl implements LoanAdapterService {

    @Override
    public LoanId createLoan(LoanCreateRequest loanInfo) {
        NodeUserInfo bankAgent = loanInfo.getBankAgent();
        try (CordaRPCConnection connection = ConnectionFactory.createConnection(bankAgent)) {
            CordaRPCOps proxy = connection.getProxy();

            Party borrower = resolveParty(proxy, loanInfo.getBorrower())
                    .orElseThrow(
                            () -> new PartyResolveException("Cant resolve party by name: " + loanInfo.getBorrower())
                    );

            List<Party> banks = resolveParties(proxy, loanInfo.getBanks());

            SignedTransaction signedTransaction = proxy.startFlowDynamic(
                    CreateLoanFlow.Initiator.class,
                    borrower,
                    loanInfo.getLoanSum(),
                    loanInfo.getTerm(),
                    loanInfo.getRate(),
                    banks
            ).getReturnValue().get();
            ContractState output = signedTransaction.getCoreTransaction().getOutput(0);
            if (output instanceof LoanState) {
                LoanState loanState = (LoanState) output;
                return new LoanId(
                        loanState.getId().getExternalId(),
                        loanState.getLinearId().getId()
                );
            } else {
                log.error("Invalid class of output, expected: '{}' actual: '{}'",
                        LoanState.class.getSimpleName(),
                        output.getClass().getSimpleName()
                );
                throw new LoanCreateException("Cant cast output state to LoanState, actual type: " + output.getClass().getSimpleName());
            }
        } catch (InterruptedException e) {
            log.error("Interrupt while create loan flow by rpc to address: {}", bankAgent.getAddress(), e);
            throw new LoanCreateException(e);
        } catch (ExecutionException e) {
            log.error("Execution exception while create loan flow by rpc to address: {}", bankAgent.getAddress(), e);
            throw new LoanCreateException(e);
        }
    }

    @Override
    public void joinBank(BankJoinRequest joinRequest) {
        NodeUserInfo bank = joinRequest.getBank();
        try (CordaRPCConnection connection = ConnectionFactory.createConnection(bank)) {
            CordaRPCOps proxy = connection.getProxy();
            UniqueIdentifier loanId = new UniqueIdentifier(
                    joinRequest.getLoanId().getLoanExternalId(),
                    joinRequest.getLoanId().getId()
            );
            SignedTransaction signedTransaction = proxy.startFlowDynamic(
                    SyndicateParticipantFlow.Initiator.class,
                    loanId,
                    joinRequest.getIssuedLoanSum()
            ).getReturnValue().get();
            ContractState output = signedTransaction.getCoreTransaction().getOutput(0);
            if (output instanceof SyndicateParticipantState) {
                SyndicateParticipantState participantState = (SyndicateParticipantState) output;
                log.info("Successful bank join to syndicate id='{}'", participantState.getId());
            } else {
                log.error("Invalid class of output, expected: '{}' actual: '{}'",
                        SyndicateParticipantState.class.getSimpleName(),
                        output.getClass().getSimpleName()
                );
                throw new BankJoinException("Cant cast output state to SyndicateParticipantState, actual type: "
                        + output.getClass().getSimpleName());
            }
        } catch (InterruptedException e) {
            log.error("Interrupt while bank join to syndicate flow by rpc to address: {}", bank.getAddress(), e);
            throw new BankJoinException(e);
        } catch (ExecutionException e) {
            log.error("Execution exception while bank join to syndicate flow by rpc to address: {}", bank.getAddress(), e);
            throw new BankJoinException(e);
        }
    }

    @Override
    public void payLoan(PaymentRequest paymentRequest) {
        NodeUserInfo payer = paymentRequest.getPayer();
        try (CordaRPCConnection connection = ConnectionFactory.createConnection(payer)) {
            CordaRPCOps proxy = connection.getProxy();

            UniqueIdentifier loanId = new UniqueIdentifier(
                    paymentRequest.getLoanId().getLoanExternalId(),
                    paymentRequest.getLoanId().getId()
            );
            SignedTransaction signedTransaction = proxy.startFlowDynamic(
                    LoanPaymentFlow.Initiator.class,
                    loanId,
                    paymentRequest.getPayment()
            ).getReturnValue().get();
            ContractState output = signedTransaction.getCoreTransaction().getOutput(0);
            if (output instanceof LoanPaymentState) {
                LoanPaymentState loanPaymentState = (LoanPaymentState) output;
                log.info("Successful accept payment id='{}'", loanPaymentState.getId());
            } else {
                log.error("Invalid class of output, expected: '{}' actual: '{}'",
                        LoanPaymentState.class.getSimpleName(),
                        output.getClass().getSimpleName()
                );
                throw new PaymentException("Cant cast output state to LoanPaymentState, actual type: "
                        + output.getClass().getSimpleName());
            }
        } catch (InterruptedException e) {
            log.error("Interrupt while accept loan payment flow by rpc to address: {}", payer.getAddress(), e);
            throw new PaymentException(e);
        } catch (ExecutionException e) {
            log.error("Execution exception while accept loan payment flow by rpc to address: {}", payer.getAddress(), e);
            throw new PaymentException(e);
        }
    }
}
