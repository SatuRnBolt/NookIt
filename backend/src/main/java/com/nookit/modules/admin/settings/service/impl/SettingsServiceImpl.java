package com.nookit.modules.admin.settings.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.domain.system.SystemSetting;
import com.nookit.modules.admin.settings.dto.SettingsVO;
import com.nookit.modules.admin.settings.mapper.SettingsMapper;
import com.nookit.modules.admin.settings.service.SettingsService;
import com.nookit.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingsMapper settingsMapper;

    private static final String KEY_MAX_HOURS = "reservation.max_hours";
    private static final String KEY_CHECKIN_BEFORE = "checkin.remind_before_minutes";
    private static final String KEY_CHECKIN_AFTER = "checkin.deadline_after_start_minutes";
    private static final String KEY_REMIND_BEFORE = "checkin.remind_before_minutes";
    private static final String KEY_VIOLATION_LIMIT = "violation.max_count";
    private static final String KEY_SUSPEND_DAYS = "violation.suspend_days";
    private static final String KEY_ALLOW_WEEKEND = "reservation.allow_weekend";

    @Override
    public SettingsVO getSettings() {
        List<Map<String, Object>> all = settingsMapper.listAllSettings();
        Map<String, String> map = all.stream().collect(
                Collectors.toMap(r -> (String) r.get("settingKey"), r -> (String) r.get("valueText"),
                        (a, b) -> a));

        SettingsVO vo = new SettingsVO();
        vo.setMaxBookingHours(toInt(map.get(KEY_MAX_HOURS), 4));
        vo.setCheckInWindowBefore(toInt(map.get(KEY_CHECKIN_BEFORE), 15));
        vo.setCheckInWindowAfter(toInt(map.get(KEY_CHECKIN_AFTER), 15));
        vo.setReminderMinutesBefore(toInt(map.get(KEY_REMIND_BEFORE), 15));
        vo.setViolationLimit(toInt(map.get(KEY_VIOLATION_LIMIT), 3));
        vo.setSuspendDays(toInt(map.get(KEY_SUSPEND_DAYS), 7));
        vo.setAllowWeekend("true".equalsIgnoreCase(map.get(KEY_ALLOW_WEEKEND)));
        return vo;
    }

    @Override
    public void updateSettings(SettingsVO req, UserPrincipal operator) {
        updateKey(KEY_MAX_HOURS, String.valueOf(req.getMaxBookingHours()), operator.getUserId());
        updateKey(KEY_CHECKIN_BEFORE, String.valueOf(req.getCheckInWindowBefore()), operator.getUserId());
        updateKey(KEY_CHECKIN_AFTER, String.valueOf(req.getCheckInWindowAfter()), operator.getUserId());
        updateKey(KEY_VIOLATION_LIMIT, String.valueOf(req.getViolationLimit()), operator.getUserId());
        updateKey(KEY_SUSPEND_DAYS, String.valueOf(req.getSuspendDays()), operator.getUserId());
        updateKey(KEY_ALLOW_WEEKEND, String.valueOf(req.getAllowWeekend()), operator.getUserId());
    }

    @Override
    public PageResult<Map<String, Object>> getSettingsLogs(int page, int pageSize) {
        Page<Map<String, Object>> pg = new Page<>(page, pageSize);
        settingsMapper.pageSettingsLogs(pg);
        return PageResult.from(pg);
    }

    private void updateKey(String key, String value, Long updatedBy) {
        SystemSetting setting = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSetting>().eq(SystemSetting::getSettingKey, key));
        if (setting != null) {
            setting.setValueText(value);
            setting.setUpdatedBy(updatedBy);
            settingsMapper.updateById(setting);
        }
    }

    private int toInt(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
