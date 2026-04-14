package com.example.xbankbackend.repositories;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.example.xbankbackend.generated.Tables.VERIFICATION_CODES;

@AllArgsConstructor
@Repository
public class VerificationCodesRepository {

    private final DSLContext dsl;

    public UUID create(UUID userId, String hashedCode) {
        return dsl.insertInto(VERIFICATION_CODES)
                .set(VERIFICATION_CODES.USER_ID, userId)
                .set(VERIFICATION_CODES.CODE_HASH, hashedCode)
                .set(VERIFICATION_CODES.PURPOSE, "2FA_LOGIN")
                .set(VERIFICATION_CODES.EXPIRES_AT, OffsetDateTime.now().plusMinutes(5))
                .returning(VERIFICATION_CODES.ID)
                .fetchOne()
                .getId();
    }

    public String getCode(UUID stateId) {
        return dsl.select(VERIFICATION_CODES.CODE_HASH)
                .from(VERIFICATION_CODES)
                .where(VERIFICATION_CODES.ID.eq(stateId))
                .and(VERIFICATION_CODES.PURPOSE.eq("2FA_LOGIN"))
                .and(VERIFICATION_CODES.USED.eq(false))
                .and(VERIFICATION_CODES.EXPIRES_AT.gt(OffsetDateTime.now()))
                .orderBy(VERIFICATION_CODES.CREATED_AT.desc())
                .fetchOne()
                .into(String.class);
    }

    public boolean exists(UUID stateId) {
        return dsl.selectFrom(VERIFICATION_CODES)
                .where(VERIFICATION_CODES.ID.eq(stateId))
                .and(VERIFICATION_CODES.PURPOSE.eq("2FA_LOGIN"))
                .and(VERIFICATION_CODES.USED.eq(false))
                .and(VERIFICATION_CODES.EXPIRES_AT.gt(OffsetDateTime.now()))
                .fetch()
                .size() >= 1;
    }

    public void setUsed(UUID stateId) {
        dsl.update(VERIFICATION_CODES)
                .set(VERIFICATION_CODES.USED, true)
                .where(VERIFICATION_CODES.ID.eq(stateId))
                .execute();
    }
}
