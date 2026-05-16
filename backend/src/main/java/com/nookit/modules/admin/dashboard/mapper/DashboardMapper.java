package com.nookit.modules.admin.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    Map<String, Object> querySummary();

    List<Map<String, Object>> queryWeeklyTrend();

    List<Map<String, Object>> queryRoomOccupancy(@Param("top") int top);

    List<Map<String, Object>> queryHeatmapData();

    Map<String, Object> queryTodos();
}
