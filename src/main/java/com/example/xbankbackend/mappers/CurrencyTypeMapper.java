package com.example.xbankbackend.mappers;

import com.example.xbankbackend.enums.CurrencyType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyTypeMapper {
    com.example.xbankbackend.generated.enums.CurrencyType toGenerated(CurrencyType currencyType);
}
