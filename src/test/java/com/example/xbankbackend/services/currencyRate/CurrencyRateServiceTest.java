package com.example.xbankbackend.services.currencyRate;

import com.example.xbankbackend.dtos.requests.UpdateCurrencyRateRequest;
import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.RateNotFoundException;
import com.example.xbankbackend.mappers.CurrencyMapper;
import com.example.xbankbackend.models.CurrencyRate;
import com.example.xbankbackend.repositories.CurrencyRateRepository;
import com.example.xbankbackend.services.external.cbr.CbrSoapService;
import com.example.xbankbackend.services.external.cbr.CurrencyParserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyRateService")
class CurrencyRateServiceTest {

    @Mock
    private CbrSoapService cbrSoapService;

    @Mock
    private CurrencyParserService currencyParserService;

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @Mock
    private CurrencyRateValidationService currencyRateValidationService;

    @Mock
    private CurrencyMapper currencyMapper;

    @InjectMocks
    private CurrencyRateService service;

    @Nested
    @DisplayName("updateOnDate")
    class UpdateOnDateTests {

        @Test
        void shouldCreateNewRates_WhenRatesNotExists() {
            LocalDate date = LocalDate.now();
            String xml = "<xml>...</xml>";
            Map<CurrencyType, BigDecimal> rates = Map.of(
                    CurrencyType.USD, BigDecimal.valueOf(90),
                    CurrencyType.EUR, BigDecimal.valueOf(100)
            );

            when(cbrSoapService.getCursOnDate(date)).thenReturn(xml);
            when(currencyParserService.parseCurrencies(xml)).thenReturn(rates);
            when(currencyRateRepository.existsByCurrencyAndDate(any(), any())).thenReturn(false);

            service.updateOnDate(date);

            verify(currencyRateRepository, times(2)).create(any(CurrencyRate.class));
            verify(currencyRateRepository, never()).update(any(), any(), any());
        }

        @Test
        void shouldSkipExistingRates_WhenRatesAlreadyExist() {
            LocalDate date = LocalDate.now();
            String xml = "<xml>...</xml>";
            Map<CurrencyType, BigDecimal> rates = Map.of(CurrencyType.USD, BigDecimal.valueOf(90));

            when(cbrSoapService.getCursOnDate(date)).thenReturn(xml);
            when(currencyParserService.parseCurrencies(xml)).thenReturn(rates);
            when(currencyRateRepository.existsByCurrencyAndDate(CurrencyType.USD, date)).thenReturn(true);

            service.updateOnDate(date);

            verify(currencyRateRepository, never()).create(any(CurrencyRate.class));
        }

        @Test
        void shouldSetAutoComment_WhenCreatingRate() {
            LocalDate date = LocalDate.now();
            String xml = "<xml>...</xml>";
            Map<CurrencyType, BigDecimal> rates = Map.of(CurrencyType.USD, BigDecimal.valueOf(90));

            when(cbrSoapService.getCursOnDate(date)).thenReturn(xml);
            when(currencyParserService.parseCurrencies(xml)).thenReturn(rates);
            when(currencyRateRepository.existsByCurrencyAndDate(any(), any())).thenReturn(false);

            service.updateOnDate(date);

            verify(currencyRateRepository).create(argThat(rate ->
                    "[AUTO]".equals(rate.getComment()) &&
                            rate.getCreatedAt() != null
            ));
        }
    }

    @Nested
    @DisplayName("convert")
    class ConvertTests {

        @Test
        void shouldConvertViaRub_WhenBothCurrenciesAreNotRub() {
            CurrencyType from = CurrencyType.USD;
            CurrencyType to = CurrencyType.EUR;
            BigDecimal amount = BigDecimal.valueOf(100);
            BigDecimal usdRate = BigDecimal.valueOf(90);
            BigDecimal eurRate = BigDecimal.valueOf(100);

            when(currencyRateRepository.findLatestByCurrency(from)).thenReturn(buildRate(usdRate));
            when(currencyRateRepository.findLatestByCurrency(to)).thenReturn(buildRate(eurRate));

            BigDecimal result = service.convert(from, to, amount);

            // (90 / 100) * 100 = 90
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(90));
        }

        @Test
        void shouldReturnAmount_WhenBothCurrenciesAreRub() {
            BigDecimal amount = BigDecimal.valueOf(100);

            BigDecimal result = service.convert(CurrencyType.RUB, CurrencyType.RUB, amount);

            assertThat(result).isEqualByComparingTo(amount);
        }

        @Test
        void shouldUseFromRateOnly_WhenToIsRub() {
            CurrencyType from = CurrencyType.USD;
            CurrencyType to = CurrencyType.RUB;
            BigDecimal amount = BigDecimal.valueOf(100);
            BigDecimal usdRate = BigDecimal.valueOf(90);

            when(currencyRateRepository.findLatestByCurrency(from)).thenReturn(buildRate(usdRate));

            BigDecimal result = service.convert(from, to, amount);

            // (90 / 1) * 100 = 9000
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(9000));
        }

        @Test
        void shouldUseToRateOnly_WhenFromIsRub() {
            CurrencyType from = CurrencyType.RUB;
            CurrencyType to = CurrencyType.USD;
            BigDecimal amount = BigDecimal.valueOf(9000);
            BigDecimal usdRate = BigDecimal.valueOf(90);

            when(currencyRateRepository.findLatestByCurrency(to)).thenReturn(buildRate(usdRate));

            BigDecimal result = service.convert(from, to, amount);

            // (1 / 90) * 9000 = 100
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        private CurrencyRate buildRate(BigDecimal rate) {
            CurrencyRate r = new CurrencyRate();
            r.setRate(rate);
            return r;
        }
    }

    @Nested
    @DisplayName("createRate")
    class CreateRateTests {

        @Test
        void shouldDelegateToRepository() {
            CurrencyRate rate = new CurrencyRate();
            rate.setCurrency(CurrencyType.USD);
            rate.setRate(BigDecimal.valueOf(90));

            service.createRate(rate);

            verify(currencyRateRepository).create(rate);
        }
    }

    @Nested
    @DisplayName("updateRate")
    class UpdateRateTests {

        @Test
        void shouldUpdateRate_WhenExists() {
            CurrencyType currency = CurrencyType.USD;
            LocalDate date = LocalDate.now();
            UpdateCurrencyRateRequest request = new UpdateCurrencyRateRequest();
            request.setRate(BigDecimal.valueOf(95));

            CurrencyRate existing = new CurrencyRate();
            existing.setCurrency(currency);
            existing.setDate(date);
            existing.setRate(BigDecimal.valueOf(90));

            doNothing().when(currencyRateValidationService)
                    .validateCurrencyRateExistsByCurrencyAndDate(currency, date);
            when(currencyRateRepository.findByCurrencyAndDate(currency, date)).thenReturn(existing);

            service.updateRate(currency, date, request);

            verify(currencyMapper).updateCurrencyFromRequest(request, existing);
            verify(currencyRateRepository).update(currency, date, existing);
        }

        @Test
        void shouldThrowRateNotFoundException_WhenRateNotExists() {
            CurrencyType currency = CurrencyType.USD;
            LocalDate date = LocalDate.now();
            UpdateCurrencyRateRequest request = new UpdateCurrencyRateRequest();

            doThrow(new RateNotFoundException("Not found"))
                    .when(currencyRateValidationService)
                    .validateCurrencyRateExistsByCurrencyAndDate(currency, date);

            assertThatThrownBy(() -> service.updateRate(currency, date, request))
                    .isInstanceOf(RateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteRatesByDate")
    class DeleteRatesByDateTests {

        @Test
        void shouldDeleteRates_WhenExists() {
            LocalDate date = LocalDate.now();

            doNothing().when(currencyRateValidationService).validateCurrencyRateExistsByDate(date);

            service.deleteRatesByDate(date);

            verify(currencyRateRepository).deleteRatesByDate(date);
        }

        @Test
        void shouldThrowRateNotFoundException_WhenRatesNotExists() {
            LocalDate date = LocalDate.now();

            doThrow(new RateNotFoundException("Not found"))
                    .when(currencyRateValidationService).validateCurrencyRateExistsByDate(date);

            assertThatThrownBy(() -> service.deleteRatesByDate(date))
                    .isInstanceOf(RateNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getRatesByDate")
    class GetRatesByDateTests {

        @Test
        void shouldReturnRates_WhenExists() {
            LocalDate date = LocalDate.now();
            List<CurrencyRate> expected = List.of(new CurrencyRate(), new CurrencyRate());

            doNothing().when(currencyRateValidationService).validateCurrencyRateExistsByDate(date);
            when(currencyRateRepository.findByDate(date)).thenReturn(expected);

            List<CurrencyRate> result = service.getRatesByDate(date);

            assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
        }
    }

    @Nested
    @DisplayName("getLatestRates")
    class GetLatestRatesTests {

        @Test
        void shouldReturnRatesForToday() {
            LocalDate today = LocalDate.now();
            List<CurrencyRate> expected = List.of(new CurrencyRate());

            when(currencyRateRepository.findByDateOrderByCurrencyAsc(today)).thenReturn(expected);

            List<CurrencyRate> result = service.getLatestRates();

            assertThat(result).containsExactlyElementsOf(expected);
            verify(currencyRateRepository).findByDateOrderByCurrencyAsc(today);
        }
    }

    @Nested
    @DisplayName("getLatestRateByCurrency")
    class GetLatestRateByCurrencyTests {

        @Test
        void shouldReturnLatestRateForCurrency() {
            CurrencyType currency = CurrencyType.USD;
            CurrencyRate expected = new CurrencyRate();
            expected.setRate(BigDecimal.valueOf(90));

            when(currencyRateRepository.findLatestByCurrency(currency)).thenReturn(expected);

            CurrencyRate result = service.getLatestRateByCurrency(currency);

            assertThat(result).isEqualTo(expected);
        }
    }
}
