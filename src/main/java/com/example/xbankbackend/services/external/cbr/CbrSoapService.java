package com.example.xbankbackend.services.external.cbr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;

@Service
public class CbrSoapService {

    @Value("${cbr.base-url}")
    private String baseUrl;

    @Value("${cbr.soap-url}")
    private String soapUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getCursOnDate(LocalDate date) {
        String requestBody = String.format("""
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetCursOnDate xmlns="http://web.cbr.ru/">
                      <On_date>%s</On_date>
                    </GetCursOnDate>
                  </soap:Body>
                </soap:Envelope>
                """, date.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "text/xml; charset=utf-8");
        headers.set("SOAPAction", soapUrl);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        return response.getBody();
    }

}
