package com.example.xbankbackend.repositories.external.cbr;

import com.example.xbankbackend.enums.CurrencyType;
import com.example.xbankbackend.mappers.CurrencyTypeMapper;
import com.example.xbankbackend.models.external.cbr.CurrencyRate;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.example.xbankbackend.generated.Tables.CURRENCY_RATES;

@AllArgsConstructor
@Repository
public class CurrencyRateRepository {

    private final DSLContext dsl;
    private CurrencyTypeMapper currencyTypeMapper;

    public void create(CurrencyRate rate) {
        dsl.insertInto(CURRENCY_RATES)
                .values(rate.getCurrency(), rate.getRate(), rate.getDate(), rate.getCreatedAt())
                .execute();
    }

    public boolean existsByDate(LocalDate date) {
        return dsl.selectFrom(CURRENCY_RATES)
                .where(CURRENCY_RATES.DATE.eq(date))
                .fetch()
                .isNotEmpty();
    }

    public CurrencyRate findByCurrencyAndDate(CurrencyType currency, LocalDate date) {
        return dsl.selectFrom(CURRENCY_RATES)
                .where(CURRENCY_RATES.CURRENCY.eq(currencyTypeMapper.toGenerated(currency)))
                .and(CURRENCY_RATES.DATE.eq(date))
                .fetchOneInto(CurrencyRate.class);
    }

    public List<CurrencyRate> findByDateOrderByCurrencyAsc(LocalDate date) {
        return dsl.selectFrom(CURRENCY_RATES)
                .where(CURRENCY_RATES.DATE.eq(date))
                .orderBy(CURRENCY_RATES.CURRENCY.asc())
                .fetchInto(CurrencyRate.class);
    }

    public CurrencyRate findLatestByCurrency(CurrencyType currency) {
        return dsl.selectFrom(CURRENCY_RATES)
                .where(CURRENCY_RATES.CURRENCY.eq(currencyTypeMapper.toGenerated(currency)))
                .orderBy(CURRENCY_RATES.DATE.desc())
                .limit(1)
                .fetchOneInto(CurrencyRate.class);
    }
}
