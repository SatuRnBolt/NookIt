package com.nookit.modules.admin.room.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DictMapper {

    @Select("SELECT campus_name AS name FROM campuses WHERE campus_status = 'active' ORDER BY campus_name")
    List<String> listCampusNames();

    @Select("SELECT id, org_name AS name FROM organizations WHERE org_status = 'active' AND deleted_at IS NULL ORDER BY display_order")
    List<Map<String, Object>> listOrganizations();

    @Select("SELECT room_type_code AS value, room_type_name AS label FROM room_types ORDER BY room_type_code")
    List<Map<String, Object>> listRoomTypes();
}
