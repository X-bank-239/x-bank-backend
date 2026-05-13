package com.example.xbankbackend.mappers;

import com.example.xbankbackend.enums.BankAccountType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountTypeMapper {
    com.example.xbankbackend.generated.enums.BankAccountType toGenerated(BankAccountType bankAccountType);
}
