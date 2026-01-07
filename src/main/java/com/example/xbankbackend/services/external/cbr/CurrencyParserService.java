package com.example.xbankbackend.services.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.exceptions.CurrencyParsingException;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@Service
public class CurrencyParserService {

    public Map<CurrencyType, BigDecimal> parseCurrencies(String xmlResponse) {
        Document doc;
        try {
            doc = Jsoup.parse(xmlResponse, "", Parser.xmlParser());
        } catch (Exception e) {
            throw new CurrencyParsingException("Failed to parse XML response");
        }

        Map<CurrencyType, BigDecimal> rates = new HashMap<>();

        Elements valuteElements = doc.select("ValuteCursOnDate");
        if (valuteElements.isEmpty()) {
            throw new CurrencyParsingException("No currency data found in XML response");
        }

        for (Element val : valuteElements) {
            try {
                String currencyCode = val.selectFirst("VchCode").text().trim();
                BigDecimal rate = BigDecimal.valueOf(Float.parseFloat(val.selectFirst("VunitRate").text()));
                if (isCurrencySupported(currencyCode)) {
                    rates.put(CurrencyType.valueOf(currencyCode), rate);
                }
            } catch (Exception e) {
                log.error("Error parsing currency element", e);
            }
        }

        return rates;
    }

    private boolean isCurrencySupported(String currencyCode) {
        try {
            CurrencyType.valueOf(currencyCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
