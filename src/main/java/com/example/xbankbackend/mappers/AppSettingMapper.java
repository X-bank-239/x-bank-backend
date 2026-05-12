package com.example.xbankbackend.mappers;

import com.example.xbankbackend.dtos.requests.UpdateAppSettingRequest;
import com.example.xbankbackend.models.AppSetting;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AppSettingMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(UpdateAppSettingRequest request, @MappingTarget AppSetting setting);
}
