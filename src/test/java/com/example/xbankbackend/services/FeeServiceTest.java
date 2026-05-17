package com.example.xbankbackend.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeeService")
class FeeServiceTest {

    @Mock
    private AppSettingsService settingsService;

    @InjectMocks
    private FeeService feeService;

    @Nested
    @DisplayName("applyBaseFee")
    class ApplyBaseFeeTests {

        @Test
        void shouldApplyBaseFee_FromSettings() {
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal baseFee = BigDecimal.valueOf(0.015);

            when(settingsService.getTransactionsBaseFee()).thenReturn(baseFee);

            BigDecimal result = feeService.applyBaseFee(amount);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1015));
        }

        @Test
        void shouldApplyBaseFee_WithZeroAmount() {
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal baseFee = BigDecimal.valueOf(0.015);

            when(settingsService.getTransactionsBaseFee()).thenReturn(baseFee);

            BigDecimal result = feeService.applyBaseFee(amount);

            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void shouldApplyBaseFee_WithLargeAmount() {
            BigDecimal amount = BigDecimal.valueOf(100_000);
            BigDecimal baseFee = BigDecimal.valueOf(0.02);

            when(settingsService.getTransactionsBaseFee()).thenReturn(baseFee);

            BigDecimal result = feeService.applyBaseFee(amount);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(102_000));
        }
    }

    @Nested
    @DisplayName("applyFee")
    class ApplyFeeTests {

        @Test
        void shouldApplyCustomFee_Percentage() {
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal fee = BigDecimal.valueOf(0.025);

            BigDecimal result = feeService.applyFee(amount, fee);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1025));
        }

        @Test
        void shouldApplyCustomFee_WithZeroPercentage() {
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal fee = BigDecimal.ZERO;

            BigDecimal result = feeService.applyFee(amount, fee);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1000));
        }

        @Test
        void shouldApplyCustomFee_WithHighPercentage() {
            BigDecimal amount = BigDecimal.valueOf(500);
            BigDecimal fee = BigDecimal.valueOf(0.1);

            BigDecimal result = feeService.applyFee(amount, fee);

            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(550));
        }
    }

    @Nested
    @DisplayName("getBaseFeeAmount")
    class GetBaseFeeAmountTests {

        @Test
        void shouldReturnOnlyFeeAmount_WithoutPrincipal() {
            BigDecimal amount = BigDecimal.valueOf(1000);
            BigDecimal baseFee = BigDecimal.valueOf(0.015);

            when(settingsService.getTransactionsBaseFee()).thenReturn(baseFee);

            BigDecimal feeAmount = feeService.getBaseFeeAmount(amount);

            assertThat(feeAmount).isEqualByComparingTo(BigDecimal.valueOf(15));
        }

        @Test
        void shouldReturnZeroFee_WhenAmountIsZero() {
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal baseFee = BigDecimal.valueOf(0.015);

            when(settingsService.getTransactionsBaseFee()).thenReturn(baseFee);

            BigDecimal feeAmount = feeService.getBaseFeeAmount(amount);

            assertThat(feeAmount).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void shouldCalculateFee_WithDifferentRate() {
            BigDecimal amount = BigDecimal.valueOf(5000);
            BigDecimal baseFee = BigDecimal.valueOf(0.02);

            when(settingsService.getTransactionsBaseFee()).thenReturn(baseFee);

            BigDecimal feeAmount = feeService.getBaseFeeAmount(amount);

            assertThat(feeAmount).isEqualByComparingTo(BigDecimal.valueOf(100));
        }
    }
}
