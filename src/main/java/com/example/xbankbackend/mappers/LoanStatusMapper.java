package com.example.xbankbackend.mappers;

import com.example.xbankbackend.enums.LoanStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanStatusMapper {
    com.example.xbankbackend.generated.enums.LoanStatus toGenerated(LoanStatus loanStatus);
}
