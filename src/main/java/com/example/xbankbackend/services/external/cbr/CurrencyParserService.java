package com.example.xbankbackend.services.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CurrencyParserService {

    public Map<CurrencyType, Float> parseCurrencies(String xmlResponse) {
        Document doc = Jsoup.parse(xmlResponse, "", Parser.xmlParser());
        Map<CurrencyType, Float> rates = new HashMap<>();

        for (Element val : doc.select("ValuteCursOnDate")) {
            String currencyCode = val.selectFirst("VchCode").text().trim();
            Float rate = Float.parseFloat(val.selectFirst("VunitRate").text());
            if (isCurrencySupported(currencyCode)) {
                rates.put(CurrencyType.valueOf(currencyCode), rate);
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
