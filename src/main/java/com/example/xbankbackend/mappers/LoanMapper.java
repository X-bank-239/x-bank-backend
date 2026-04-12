package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.responses.LoanResponse;
import com.example.xbankbackend.models.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    LoanResponse loanToResponse(Loan loan);

}
