package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.TransactionKeyword;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.xbankbackend.generated.Tables.TRANSACTION_KEYWORDS;

@AllArgsConstructor
@Repository
public class TransactionKeywordsRepository {

    private final DSLContext dsl;

    public void create(String categoryCode, String word) {
        dsl.insertInto(TRANSACTION_KEYWORDS)
                .set(TRANSACTION_KEYWORDS.WORD, word)
                .set(TRANSACTION_KEYWORDS.CATEGORY_CODE, categoryCode)
                .onDuplicateKeyIgnore()
                .execute();
    }

    public void update(String prevCategory, String prevWord, TransactionKeyword keyword) {
        dsl.update(TRANSACTION_KEYWORDS)
                .set(TRANSACTION_KEYWORDS.WORD, keyword.getWord())
                .set(TRANSACTION_KEYWORDS.CATEGORY_CODE, keyword.getCategoryCode())
                .where(TRANSACTION_KEYWORDS.CATEGORY_CODE.eq(prevCategory))
                .and(TRANSACTION_KEYWORDS.WORD.eq(prevWord))
                .execute();
    }

    public void deleteByCodeAndWord(String categoryCode, String word) {
        dsl.deleteFrom(TRANSACTION_KEYWORDS)
                .where(TRANSACTION_KEYWORDS.CATEGORY_CODE.eq(categoryCode))
                .and(TRANSACTION_KEYWORDS.WORD.eq(word))
                .execute();
    }

    public boolean existsByCodeAndWord(String categoryCode, String word) {
        return dsl.selectFrom(TRANSACTION_KEYWORDS)
                .where(TRANSACTION_KEYWORDS.CATEGORY_CODE.eq(categoryCode))
                .and(TRANSACTION_KEYWORDS.WORD.eq(word))
                .fetch()
                .size() >= 1;
    }

    public TransactionKeyword findByCodeAndWord(String categoryCode, String word) {
        return dsl.selectFrom(TRANSACTION_KEYWORDS)
                .where(TRANSACTION_KEYWORDS.CATEGORY_CODE.eq(categoryCode))
                .and(TRANSACTION_KEYWORDS.WORD.eq(word))
                .fetchOne()
                .into(TransactionKeyword.class);
    }

    public List<TransactionKeyword> findByCode(String categoryCode) {
        return dsl.selectFrom(TRANSACTION_KEYWORDS)
                .where(TRANSACTION_KEYWORDS.CATEGORY_CODE.eq(categoryCode))
                .fetchInto(TransactionKeyword.class);
    }

    public List<TransactionKeyword> findByWord(String word) {
        return dsl.selectFrom(TRANSACTION_KEYWORDS)
                .where(TRANSACTION_KEYWORDS.WORD.eq(word))
                .fetchInto(TransactionKeyword.class);
    }

    public List<TransactionKeyword> findAllKeywords() {
        return dsl.selectFrom(TRANSACTION_KEYWORDS)
                .fetchInto(TransactionKeyword.class);
    }
}
