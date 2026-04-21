package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateCurrencyRateRequest;
import com.example.xbankbackend.dtos.requests.UpdateCurrencyRateRequest;
import com.example.xbankbackend.models.CurrencyRate;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    CurrencyRate requestToCurrencyRate(CreateCurrencyRateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCurrencyFromRequest(UpdateCurrencyRateRequest request, @MappingTarget CurrencyRate rate);
}
