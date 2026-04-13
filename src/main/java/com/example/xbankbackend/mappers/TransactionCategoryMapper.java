package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.CreateCategoryRequest;
import com.example.xbankbackend.dtos.requests.UpdateCategoryRequest;
import com.example.xbankbackend.models.TransactionCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface TransactionCategoryMapper {
    TransactionCategory requestToCategory(CreateCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget TransactionCategory category);
}
