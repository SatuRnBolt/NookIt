package com.nookit.modules.admin.settings.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.settings.dto.SettingsVO;
import com.nookit.security.UserPrincipal;

import java.util.Map;

public interface SettingsService {

    SettingsVO getSettings();

    void updateSettings(SettingsVO req, UserPrincipal operator);

    PageResult<Map<String, Object>> getSettingsLogs(int page, int pageSize);
}
