package com.nookit.modules.admin.room.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.space.StudyRoom;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.room.dto.RoomPageQuery;
import com.nookit.modules.admin.room.dto.RoomStatsVO;
import com.nookit.modules.admin.room.dto.RoomVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RoomMapper extends BaseMapperX<StudyRoom> {

    IPage<RoomVO> pageRooms(Page<RoomVO> page, @Param("q") RoomPageQuery query);

    RoomVO findRoomDetail(@Param("id") Long id);

    RoomStatsVO queryStats();

    List<Map<String, Object>> listSimple();
}
