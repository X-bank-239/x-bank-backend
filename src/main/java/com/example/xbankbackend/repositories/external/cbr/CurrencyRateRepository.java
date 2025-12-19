package com.example.xbankbackend.repositories.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.models.external.cbr.CurrencyRate;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.xbankbackend.generated.Tables.CURRENCY_RATES;

@AllArgsConstructor
@Repository
public class CurrencyRateRepository {

    private final DSLContext dsl;

    public void create(CurrencyRate rate) {
        dsl.insertInto(CURRENCY_RATES)
                .values(rate.getCurrency(), rate.getRate(), rate.getDate(), rate.getCreatedAt())
                .execute();
    }

    public boolean existsByDate(LocalDate date) {
        return dsl.selectFrom(CURRENCY_RATES)
                .where(CURRENCY_RATES.DATE.eq(date))
                .fetch()
                .size() == 1;
    }

    public Optional<CurrencyRate> findByCurrencyCodeAndDate(CurrencyType currency, LocalDate date) {
        com.example.xbankbackend.generated.enums.CurrencyType generatedCurrency = com.example.xbankbackend.generated.enums.CurrencyType.valueOf(String.valueOf(currency));
        return Optional.ofNullable(
                dsl.selectFrom(CURRENCY_RATES)
                        .where(CURRENCY_RATES.CURRENCY.eq(generatedCurrency))
                        .and(CURRENCY_RATES.DATE.eq(date))
                        .fetchOneInto(CurrencyRate.class)
        );
    }

    public List<CurrencyRate> findByDateOrderByCurrencyAsc(LocalDate date) {
        return dsl.selectFrom(CURRENCY_RATES)
                .where(CURRENCY_RATES.DATE.eq(date))
                .orderBy(CURRENCY_RATES.CURRENCY.asc())
                .fetchInto(CurrencyRate.class);
    }

    public Optional<CurrencyRate> findLatestByCurrency(String currency) {
        com.example.xbankbackend.generated.enums.CurrencyType generatedCurrency = com.example.xbankbackend.generated.enums.CurrencyType.valueOf(String.valueOf(currency));
        return Optional.ofNullable(
                dsl.selectFrom(CURRENCY_RATES)
                        .where(CURRENCY_RATES.CURRENCY.eq(generatedCurrency))
                        .orderBy(CURRENCY_RATES.DATE.desc())
                        .limit(1)
                        .fetchOneInto(CurrencyRate.class)
        );
    }
}
