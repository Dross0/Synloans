package com.synloans.loans.model.dto.loanrequest;

public enum LoanRequestStatus {
    OPEN("Заявка создана, идет присоединение к синдикату банков"),
    READY_TO_ISSUE("Заявка открыта, требуемая сумма кредита собрана"),
    ISSUE("Кредит выдан, заемщик в процессе выплаты"),
    CLOSE("Кредит выплачен");

    private final String description;

    LoanRequestStatus(String description){
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
