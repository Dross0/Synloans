package com.synloans.loans.model.dto.loanrequest;

public enum LoanRequestStatus {
    OPEN("Заявка создана, идет присоединение к синдикату банков"),
    TRANSFER("Заявка закрыта, банки переводят деньги заемщику"),
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
