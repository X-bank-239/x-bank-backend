package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateKeywordRequest;
import com.example.xbankbackend.dtos.requests.UpdateKeywordRequest;
import com.example.xbankbackend.models.TransactionKeyword;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TransactionKeywordMapper {
    TransactionKeyword requestToKeyword(CreateKeywordRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateKeywordRequest request, @MappingTarget TransactionKeyword keyword);
}
