package com.example.xbankbackend.services.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.CurrencyParsingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CurrencyParserService")
class CurrencyParserServiceTest {

    private CurrencyParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new CurrencyParserService();
    }

    @Nested
    @DisplayName("parseCurrencies")
    class ParseCurrenciesTests {

        @Test
        void shouldParseValidXmlResponse() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>90.5</VunitRate>
                        <VchCode>USD</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>100.25</VunitRate>
                        <VchCode>EUR</VchCode>
                    </ValuteCursOnDate>
                </ValCursOnDate>
                """;

            Map<CurrencyType, BigDecimal> rates = parserService.parseCurrencies(xmlResponse);

            assertThat(rates).hasSize(2);
            assertThat(rates.get(CurrencyType.USD)).isEqualByComparingTo(BigDecimal.valueOf(90.5));
            assertThat(rates.get(CurrencyType.EUR)).isEqualByComparingTo(BigDecimal.valueOf(100.25));
        }

        @Test
        void shouldParseXmlWithAllSupportedCurrencies() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>90.5</VunitRate>
                        <VchCode>USD</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>100.25</VunitRate>
                        <VchCode>EUR</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>15.50</VunitRate>
                        <VchCode>CNY</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>1.0</VunitRate>
                        <VchCode>RUB</VchCode>
                    </ValuteCursOnDate>
                </ValCursOnDate>
                """;

            Map<CurrencyType, BigDecimal> rates = parserService.parseCurrencies(xmlResponse);

            assertThat(rates).hasSize(4);
            assertThat(rates.get(CurrencyType.USD)).isEqualByComparingTo(BigDecimal.valueOf(90.5));
            assertThat(rates.get(CurrencyType.EUR)).isEqualByComparingTo(BigDecimal.valueOf(100.25));
            assertThat(rates.get(CurrencyType.CNY)).isEqualByComparingTo(BigDecimal.valueOf(15.50));
            assertThat(rates.get(CurrencyType.RUB)).isEqualByComparingTo(BigDecimal.valueOf(1.0));
        }

        @Test
        void shouldSkipUnsupportedCurrencies() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>90.5</VunitRate>
                        <VchCode>USD</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>75.0</VunitRate>
                        <VchCode>GBP</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>50.0</VunitRate>
                        <VchCode>JPY</VchCode>
                    </ValuteCursOnDate>
                </ValCursOnDate>
                """;

            Map<CurrencyType, BigDecimal> rates = parserService.parseCurrencies(xmlResponse);

            assertThat(rates).hasSize(1);
            assertThat(rates).containsOnlyKeys(CurrencyType.USD);
        }

        @Test
        void shouldThrowException_WhenXmlIsInvalid() {
            String invalidXml = "not a valid xml";

            assertThatThrownBy(() -> parserService.parseCurrencies(invalidXml))
                    .isInstanceOf(CurrencyParsingException.class);
        }

        @Test
        void shouldThrowException_WhenNoValuteCursOnDateElements() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                </ValCursOnDate>
                """;

            assertThatThrownBy(() -> parserService.parseCurrencies(xmlResponse))
                    .isInstanceOf(CurrencyParsingException.class);
        }

        @Test
        void shouldHandleEmptyXmlResponse() {
            String xmlResponse = "";

            assertThatThrownBy(() -> parserService.parseCurrencies(xmlResponse))
                    .isInstanceOf(CurrencyParsingException.class);
        }

        @Test
        void shouldHandleNullXmlResponse() {
            assertThatThrownBy(() -> parserService.parseCurrencies(null))
                    .isInstanceOf(CurrencyParsingException.class);
        }

        @Test
        void shouldParseXmlWithDecimalRates() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>90.123456</VunitRate>
                        <VchCode>USD</VchCode>
                    </ValuteCursOnDate>
                </ValCursOnDate>
                """;

            Map<CurrencyType, BigDecimal> rates = parserService.parseCurrencies(xmlResponse);

            assertThat(rates).hasSize(1);
            assertThat(rates.get(CurrencyType.USD)).isEqualByComparingTo(BigDecimal.valueOf(90.123456));
        }

        @Test
        void shouldIgnoreMalformedValuteElements() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>90.5</VunitRate>
                        <VchCode>USD</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VchCode>EUR</VchCode>
                    </ValuteCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>invalid</VunitRate>
                        <VchCode>CNY</VchCode>
                    </ValuteCursOnDate>
                </ValCursOnDate>
                """;

            Map<CurrencyType, BigDecimal> rates = parserService.parseCurrencies(xmlResponse);

            assertThat(rates).hasSize(1);
            assertThat(rates.get(CurrencyType.USD)).isEqualByComparingTo(BigDecimal.valueOf(90.5));
        }

        @Test
        void shouldHandleXmlWithWhitespaceAndFormatting() {
            String xmlResponse = """
                <?xml version="1.0" encoding="UTF-8"?>
                <ValCursOnDate>
                    <ValuteCursOnDate>
                        <VunitRate>  90.5  </VunitRate>
                        <VchCode>  USD  </VchCode>
                    </ValuteCursOnDate>
                </ValCursOnDate>
                """;

            Map<CurrencyType, BigDecimal> rates = parserService.parseCurrencies(xmlResponse);

            assertThat(rates).hasSize(1);
            assertThat(rates.get(CurrencyType.USD)).isEqualByComparingTo(BigDecimal.valueOf(90.5));
        }
    }

    @Nested
    @DisplayName("isCurrencySupported")
    class IsCurrencySupportedTests {

        @Test
        void shouldReturnTrue_ForSupportedCurrencies() {
            assertThat(parserService.isCurrencySupported("USD")).isTrue();
            assertThat(parserService.isCurrencySupported("EUR")).isTrue();
            assertThat(parserService.isCurrencySupported("RUB")).isTrue();
            assertThat(parserService.isCurrencySupported("CNY")).isTrue();
        }

        @Test
        void shouldReturnFalse_ForUnsupportedCurrencies() {
            assertThat(parserService.isCurrencySupported("GBP")).isFalse();
            assertThat(parserService.isCurrencySupported("JPY")).isFalse();
            assertThat(parserService.isCurrencySupported("CHF")).isFalse();
            assertThat(parserService.isCurrencySupported("INVALID")).isFalse();
        }

        @Test
        void shouldReturnFalse_ForEmptyCurrencyCode() {
            assertThat(parserService.isCurrencySupported("")).isFalse();
        }
    }
}
