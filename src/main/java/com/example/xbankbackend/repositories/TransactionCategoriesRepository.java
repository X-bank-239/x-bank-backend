package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.TransactionCategory;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.xbankbackend.generated.Tables.TRANSACTION_CATEGORIES;

@AllArgsConstructor
@Repository
public class TransactionCategoriesRepository {

    private final DSLContext dsl;

    public void create(String code, String displayName, String colorCode) {
        dsl.insertInto(TRANSACTION_CATEGORIES)
                .set(TRANSACTION_CATEGORIES.CODE, code)
                .set(TRANSACTION_CATEGORIES.DISPLAY_NAME, displayName)
                .set(TRANSACTION_CATEGORIES.COLOR_CODE, colorCode)
                .onDuplicateKeyIgnore()
                .execute();
    }

    public void update(TransactionCategory category) {
        dsl.update(TRANSACTION_CATEGORIES)
                .set(TRANSACTION_CATEGORIES.DISPLAY_NAME, category.getDisplayName())
                .set(TRANSACTION_CATEGORIES.COLOR_CODE, category.getColorCode())
                .where(TRANSACTION_CATEGORIES.CODE.eq(category.getCode()))
                .execute();
    }

    public TransactionCategory findByCode(String code) {
        return dsl.selectFrom(TRANSACTION_CATEGORIES)
                .where(TRANSACTION_CATEGORIES.CODE.eq(code))
                .fetchOne()
                .into(TransactionCategory.class);
    }

    public void deleteByCode(String code) {
        dsl.update(TRANSACTION_CATEGORIES)
                .set(TRANSACTION_CATEGORIES.IS_ACTIVE, false)
                .where(TRANSACTION_CATEGORIES.CODE.eq(code))
                .execute();
    }

    public boolean existsByCode(String code) {
        return dsl.selectFrom(TRANSACTION_CATEGORIES)
                .where(TRANSACTION_CATEGORIES.CODE.eq(code))
                .and(TRANSACTION_CATEGORIES.IS_ACTIVE.eq(true))
                .fetch()
                .size() >= 1;
    }

    public List<TransactionCategory> findAllCategories() {
        return dsl.selectFrom(TRANSACTION_CATEGORIES)
                .where(TRANSACTION_CATEGORIES.IS_ACTIVE.eq(true))
                .fetchInto(TransactionCategory.class);
    }
}
