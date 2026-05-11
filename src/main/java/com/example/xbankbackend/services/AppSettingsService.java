package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.UpdateAppSettingRequest;
import com.example.xbankbackend.mappers.AppSettingMapper;
import com.example.xbankbackend.models.AppSetting;
import com.example.xbankbackend.repositories.AppSettingsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Service
public class AppSettingsService {

    private static final String SAVINGS_RATE_FLEX           = "savings.rate.flexible";
    private static final String SAVINGS_RATE_FIXED          = "savings.rate.fixed";
    private static final String SAVINGS_RATE_TOPUP_ONLY     = "savings.rate.fixed";
    private static final String SAVINGS_RATE_WITHDRAW_ONLY  = "savings.rate.fixed";
    private static final String TRANSACTIONS_BASE_FEE       = "fee.transfer.base";

    private AppSettingsRepository appSettingsRepository;
    private AppSettingMapper appSettingMapper;

    // Methods for getting values from code

    public BigDecimal getSavingsRate(boolean allowWithdrawal, boolean allowTopUp) {
        if (allowWithdrawal && allowTopUp) {
            return getDecimalValue(SAVINGS_RATE_FLEX);
        } else if (!allowWithdrawal && !allowTopUp) {
            return getDecimalValue(SAVINGS_RATE_FIXED);
        } else if (allowWithdrawal) {
            return getDecimalValue(SAVINGS_RATE_WITHDRAW_ONLY);
        }
        return getDecimalValue(SAVINGS_RATE_TOPUP_ONLY);
    }

    public BigDecimal getTransactionsBaseFee() {
        return getDecimalValue(TRANSACTIONS_BASE_FEE);
    }

    // Default methods for controller

    public AppSetting getByKey(String key) {
        validateKeyExists(key);

        return appSettingsRepository.getByKey(key);
    }

    public List<AppSetting> getAllSettings() {
        return appSettingsRepository.getAllSettings();
    }

    public AppSetting update(String key, UpdateAppSettingRequest request) {
        validateKeyExists(key);

        AppSetting setting = appSettingsRepository.getByKey(key);

        appSettingMapper.updateEntityFromRequest(request, setting);
        appSettingsRepository.update(setting);

        return setting;
    }

    private BigDecimal getDecimalValue(String key) {
        String value = appSettingsRepository.getValueByKey(key);

        return new BigDecimal(value);
    }

    private void validateKeyExists(String key) {
        if (!appSettingsRepository.existsByKey(key)) {
            throw new IllegalArgumentException("Настройка " + key + " не найдена");
        }
    }
}
