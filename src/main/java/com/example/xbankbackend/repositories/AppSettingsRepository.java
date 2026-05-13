package com.example.xbankbackend.repositories;

import com.example.xbankbackend.models.AppSetting;
import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

import static com.example.xbankbackend.generated.Tables.APP_SETTINGS;

@AllArgsConstructor
@Repository
public class AppSettingsRepository {

    private final DSLContext dsl;

    public boolean existsByKey(String key) {
        return dsl.fetchExists(APP_SETTINGS, APP_SETTINGS.SETTING_KEY.eq(key));
    }

    public String getValueByKey(String key) {
        return dsl.select(APP_SETTINGS.SETTING_VALUE)
                .from(APP_SETTINGS)
                .where(APP_SETTINGS.SETTING_KEY.eq(key))
                .fetchOne()
                .into(String.class);
    }

    public AppSetting getByKey(String key) {
        return dsl.selectFrom(APP_SETTINGS)
                .where(APP_SETTINGS.SETTING_KEY.eq(key))
                .fetchOne()
                .into(AppSetting.class);
    }

    public List<AppSetting> getAllSettings() {
        return dsl.selectFrom(APP_SETTINGS)
                .fetchInto(AppSetting.class);
    }

    public void update(AppSetting setting) {
        dsl.update(APP_SETTINGS)
                .set(APP_SETTINGS.SETTING_VALUE, setting.getSettingValue())
                .set(APP_SETTINGS.DESCRIPTION, setting.getDescription())
                .set(APP_SETTINGS.UPDATED_AT, OffsetDateTime.now())
                .set(APP_SETTINGS.UPDATED_BY, setting.getUpdatedBy())
                .where(APP_SETTINGS.SETTING_KEY.eq(setting.getSettingKey()))
                .execute();
    }
}
