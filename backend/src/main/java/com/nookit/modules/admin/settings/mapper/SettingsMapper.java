package com.nookit.modules.admin.settings.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.system.SystemSetting;
import com.nookit.common.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SettingsMapper extends BaseMapperX<SystemSetting> {

    @Select("SELECT setting_key AS settingKey, value_text AS valueText FROM system_settings")
    List<Map<String, Object>> listAllSettings();

    IPage<Map<String, Object>> pageSettingsLogs(Page<Map<String, Object>> page);
}
