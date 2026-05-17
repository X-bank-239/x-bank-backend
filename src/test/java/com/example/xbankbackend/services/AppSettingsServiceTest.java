package com.example.xbankbackend.services;

import com.example.xbankbackend.dtos.requests.UpdateAppSettingRequest;
import com.example.xbankbackend.mappers.AppSettingMapper;
import com.example.xbankbackend.models.AppSetting;
import com.example.xbankbackend.repositories.AppSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppSettingsService")
class AppSettingsServiceTest {

    @Mock
    private AppSettingsRepository appSettingsRepository;

    @Mock
    private AppSettingMapper appSettingMapper;

    @InjectMocks
    private AppSettingsService appSettingsService;

    @Nested
    @DisplayName("getSavingsRate")
    class GetSavingsRateTests {

        @Test
        void shouldReturnFlexibleRate_WhenAllowWithdrawalAndTopUp() {
            BigDecimal expectedRate = BigDecimal.valueOf(6.00);

            when(appSettingsRepository.existsByKey("savings.rate.flexible")).thenReturn(true);
            when(appSettingsRepository.getValueByKey("savings.rate.flexible")).thenReturn("6.00");

            BigDecimal result = appSettingsService.getSavingsRate(true, true);

            assertThat(result).isEqualByComparingTo(expectedRate);
        }

        @Test
        void shouldReturnFixedRate_WhenNoWithdrawalAndNoTopUp() {
            BigDecimal expectedRate = BigDecimal.valueOf(12.00);

            when(appSettingsRepository.existsByKey("savings.rate.fixed")).thenReturn(true);
            when(appSettingsRepository.getValueByKey("savings.rate.fixed")).thenReturn("12.00");

            BigDecimal result = appSettingsService.getSavingsRate(false, false);

            assertThat(result).isEqualByComparingTo(expectedRate);
        }

        @Test
        void shouldReturnWithdrawOnlyRate_WhenAllowWithdrawalOnly() {
            BigDecimal expectedRate = BigDecimal.valueOf(7.00);

            when(appSettingsRepository.existsByKey("savings.rate.withdrawal-only")).thenReturn(true);
            when(appSettingsRepository.getValueByKey("savings.rate.withdrawal-only")).thenReturn("7.00");

            BigDecimal result = appSettingsService.getSavingsRate(true, false);

            assertThat(result).isEqualByComparingTo(expectedRate);
        }

        @Test
        void shouldReturnTopupOnlyRate_WhenAllowTopUpOnly() {
            BigDecimal expectedRate = BigDecimal.valueOf(9.00);

            when(appSettingsRepository.existsByKey("savings.rate.topup-only")).thenReturn(true);
            when(appSettingsRepository.getValueByKey("savings.rate.topup-only")).thenReturn("9.00");

            BigDecimal result = appSettingsService.getSavingsRate(false, true);

            assertThat(result).isEqualByComparingTo(expectedRate);
        }

        @Test
        void shouldThrowException_WhenSettingNotFound() {
            when(appSettingsRepository.existsByKey("savings.rate.flexible")).thenReturn(false);

            assertThatThrownBy(() -> appSettingsService.getSavingsRate(true, true))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getTransactionsBaseFee")
    class GetTransactionsBaseFeeTests {

        @Test
        void shouldReturnBaseFee_FromSettings() {
            BigDecimal expectedFee = BigDecimal.valueOf(1.5);

            when(appSettingsRepository.existsByKey("transfer.base-fee")).thenReturn(true);
            when(appSettingsRepository.getValueByKey("transfer.base-fee")).thenReturn("1.5");

            BigDecimal result = appSettingsService.getTransactionsBaseFee();

            assertThat(result).isEqualByComparingTo(expectedFee);
        }

        @Test
        void shouldThrowException_WhenSettingNotFound() {
            when(appSettingsRepository.existsByKey("transfer.base-fee")).thenReturn(false);

            assertThatThrownBy(() -> appSettingsService.getTransactionsBaseFee())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getByKey")
    class GetByKeyTests {

        @Test
        void shouldReturnSetting_WhenExists() {
            String key = "test.setting";
            AppSetting expected = new AppSetting();
            expected.setSettingKey(key);
            expected.setSettingValue("value");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);
            when(appSettingsRepository.getByKey(key)).thenReturn(expected);

            AppSetting result = appSettingsService.getByKey(key);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        void shouldThrowException_WhenNotExists() {
            String key = "nonexistent.setting";

            when(appSettingsRepository.existsByKey(key)).thenReturn(false);

            assertThatThrownBy(() -> appSettingsService.getByKey(key))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getAllSettings")
    class GetAllSettingsTests {

        @Test
        void shouldReturnAllSettings() {
            List<AppSetting> expected = List.of(
                    createSetting("setting1", "value1"),
                    createSetting("setting2", "value2")
            );

            when(appSettingsRepository.getAllSettings()).thenReturn(expected);

            List<AppSetting> result = appSettingsService.getAllSettings();

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
        }

        @Test
        void shouldReturnEmptyList_WhenNoSettings() {
            when(appSettingsRepository.getAllSettings()).thenReturn(List.of());

            List<AppSetting> result = appSettingsService.getAllSettings();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTests {

        @Test
        void shouldUpdateSetting_WhenValidValue() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("50.00");

            AppSetting existing = createSetting(key, "40.00");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);
            when(appSettingsRepository.getByKey(key)).thenReturn(existing);

            appSettingsService.update(key, request);

            verify(appSettingMapper).updateEntityFromRequest(request, existing);
            verify(appSettingsRepository).update(existing);
        }

        @Test
        void shouldThrowException_WhenSettingNotExists() {
            String key = "nonexistent.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("50.00");

            when(appSettingsRepository.existsByKey(key)).thenReturn(false);

            assertThatThrownBy(() -> appSettingsService.update(key, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_WhenInvalidNumericValue() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("not-a-number");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);

            assertThatThrownBy(() -> appSettingsService.update(key, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_WhenValueIsZero() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("0");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);

            assertThatThrownBy(() -> appSettingsService.update(key, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_WhenValueIsNegative() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("-10");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);

            assertThatThrownBy(() -> appSettingsService.update(key, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowException_WhenValueExceeds100() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("100.01");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);

            assertThatThrownBy(() -> appSettingsService.update(key, request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldAcceptValue_Exactly100() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("100");

            AppSetting existing = createSetting(key, "50");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);
            when(appSettingsRepository.getByKey(key)).thenReturn(existing);

            assertThatNoException().isThrownBy(() -> appSettingsService.update(key, request));
        }

        @Test
        void shouldAcceptValue_Exactly0_01() {
            String key = "test.setting";
            UpdateAppSettingRequest request = new UpdateAppSettingRequest();
            request.setSettingValue("0.01");

            AppSetting existing = createSetting(key, "50");

            when(appSettingsRepository.existsByKey(key)).thenReturn(true);
            when(appSettingsRepository.getByKey(key)).thenReturn(existing);

            assertThatNoException().isThrownBy(() -> appSettingsService.update(key, request));
        }
    }

    private AppSetting createSetting(String key, String value) {
        AppSetting setting = new AppSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setUpdatedBy(UUID.randomUUID());
        return setting;
    }
}
