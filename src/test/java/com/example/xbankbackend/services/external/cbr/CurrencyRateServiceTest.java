package com.example.xbankbackend.services.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.external.cbr.CurrencyRate;
import com.example.xbankbackend.repositories.external.cbr.CurrencyRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Map<CurrencyType, Float> rates = Map.of(
                CurrencyType.USD, 79.43f,
                CurrencyType.EUR, 93.81f
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
        usdRate.setRate(79.43f);
        usdRate.setCurrency(CurrencyType.USD);

        CurrencyRate eurRate = new CurrencyRate();
        eurRate.setRate(93.81f);
        eurRate.setCurrency(CurrencyType.EUR);

        when(currencyRateRepository.findLatestByCurrency(CurrencyType.USD)).thenReturn(usdRate);
        when(currencyRateRepository.findLatestByCurrency(CurrencyType.EUR)).thenReturn(eurRate);

        Float result = currencyRateService.convert(CurrencyType.USD, CurrencyType.EUR, 100.0f);

        Float expected = (79.43f / 93.81f) * 100.0f;
        assertEquals(expected, result, 0.01f);
    }

    @Test
    void convert_shouldConvertCurrencyWhenFromIsRUB() {
        CurrencyRate eurRate = new CurrencyRate();
        eurRate.setRate(93.81f);
        eurRate.setCurrency(CurrencyType.EUR);

        when(currencyRateRepository.findLatestByCurrency(CurrencyType.EUR)).thenReturn(eurRate);

        Float result = currencyRateService.convert(CurrencyType.RUB, CurrencyType.EUR, 100.0f);

        Float expected = (1.0f / 93.81f) * 100.0f;
        assertEquals(expected, result, 0.01f);
    }

    @Test
    void convert_shouldConvertCurrencyWhenToIsRUB() {
        CurrencyRate usdRate = new CurrencyRate();
        usdRate.setRate(79.43f);
        usdRate.setCurrency(CurrencyType.USD);

        when(currencyRateRepository.findLatestByCurrency(CurrencyType.USD)).thenReturn(usdRate);

        Float result = currencyRateService.convert(CurrencyType.USD, CurrencyType.RUB, 100.0f);

        Float expected = (79.43f / 1.0f) * 100.0f;
        assertEquals(expected, result, 0.01f);
    }

    @Test
    void convert_shouldConvertCurrencyWhenBothAreRUB() {
        Float result = currencyRateService.convert(CurrencyType.RUB, CurrencyType.RUB, 100.0f);

        assertEquals(100.0f, result, 0.01f);
    }
}
