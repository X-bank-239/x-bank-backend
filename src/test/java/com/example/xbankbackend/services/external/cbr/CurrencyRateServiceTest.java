package com.example.xbankbackend.services.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.external.cbr.CurrencyRate;
import com.example.xbankbackend.repositories.external.cbr.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CurrencyRateServiceTest {
    @Mock
    private CbrSoapService cbrSoapService;

    @Mock
    private CurrencyParserService currencyParserService;

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @InjectMocks
    private CurrencyRateService currencyRateService;

    @Test
    void updateOnDate_shouldUpdateRatesWhenNoRatesExistForDate() {
        LocalDate date = LocalDate.of(2025, 12, 15);
        String xmlResponse = "<xml>...</xml>";
        Map<CurrencyType, BigDecimal> rates = Map.of(
                CurrencyType.USD, BigDecimal.valueOf(79.43),
                CurrencyType.EUR, BigDecimal.valueOf(93.81)
        );

        when(currencyRateRepository.existsByDate(date)).thenReturn(false);
        when(cbrSoapService.getCursOnDate(date)).thenReturn(xmlResponse);
        when(currencyParserService.parseCurrencies(xmlResponse)).thenReturn(rates);

        currencyRateService.updateOnDate(date);

        verify(currencyRateRepository).existsByDate(date);
        verify(cbrSoapService).getCursOnDate(date);
        verify(currencyParserService).parseCurrencies(xmlResponse);

        verify(currencyRateRepository, times(2)).create(any(CurrencyRate.class));
    }

    @Test
    void updateOnDate_shouldNotUpdateRatesWhenRatesExistForDate() {
        LocalDate date = LocalDate.of(2025, 12, 15);

        when(currencyRateRepository.existsByDate(date)).thenReturn(true);

        currencyRateService.updateOnDate(date);

        verify(currencyRateRepository).existsByDate(date);
        verify(currencyRateRepository, never()).create(any(CurrencyRate.class));
    }

    @Test
    void convert_shouldConvertCurrencyWhenBothAreNotRUB() {
        CurrencyRate usdRate = new CurrencyRate();
        usdRate.setRate(BigDecimal.valueOf(79.43));
        usdRate.setCurrency(CurrencyType.USD);

        CurrencyRate eurRate = new CurrencyRate();
        eurRate.setRate(BigDecimal.valueOf(93.81));
        eurRate.setCurrency(CurrencyType.EUR);

        when(currencyRateRepository.findLatestByCurrency(CurrencyType.USD)).thenReturn(usdRate);
        when(currencyRateRepository.findLatestByCurrency(CurrencyType.EUR)).thenReturn(eurRate);

        BigDecimal result = currencyRateService.convert(CurrencyType.USD, CurrencyType.EUR, BigDecimal.valueOf(100.0));

        BigDecimal expected = BigDecimal.valueOf(79.43f).divide(BigDecimal.valueOf(93.81), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100.0));
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    void convert_shouldConvertCurrencyWhenFromIsRUB() {
        CurrencyRate eurRate = new CurrencyRate();
        eurRate.setRate(BigDecimal.valueOf(93.81));
        eurRate.setCurrency(CurrencyType.EUR);

        when(currencyRateRepository.findLatestByCurrency(CurrencyType.EUR)).thenReturn(eurRate);

        BigDecimal result = currencyRateService.convert(CurrencyType.RUB, CurrencyType.EUR, BigDecimal.valueOf(100.0));

        BigDecimal expected = BigDecimal.valueOf(1.0).divide(BigDecimal.valueOf(93.81), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100.0));
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    void convert_shouldConvertCurrencyWhenToIsRUB() {
        CurrencyRate usdRate = new CurrencyRate();
        usdRate.setRate(BigDecimal.valueOf(79.43));
        usdRate.setCurrency(CurrencyType.USD);

        when(currencyRateRepository.findLatestByCurrency(CurrencyType.USD)).thenReturn(usdRate);

        BigDecimal result = currencyRateService.convert(CurrencyType.USD, CurrencyType.RUB, BigDecimal.valueOf(100.0));

        BigDecimal expected = BigDecimal.valueOf(79.43f).divide(BigDecimal.valueOf(1.0), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100.0));
        assertEquals(0, expected.compareTo(result));
    }

    @Test
    void convert_shouldConvertCurrencyWhenBothAreRUB() {
        BigDecimal result = currencyRateService.convert(CurrencyType.RUB, CurrencyType.RUB, BigDecimal.valueOf(100.0));

        assertEquals(0, result.compareTo(BigDecimal.valueOf(100.0)));
    }
}
