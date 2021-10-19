package com.sinloans.loans.service;

import com.sinloans.loans.model.LoanRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanRequestService {
    public LoanRequest getById(Long id) {
        return new LoanRequest();
    }

    public List<LoanRequest> getListOfRequests(Long limit) {
        return List.of(new LoanRequest());
    }
}
