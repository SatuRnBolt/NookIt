package com.nookit.modules.admin.room.mapper;

import com.nookit.common.domain.seat.SeatMap;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.room.dto.SeatMapVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeatMapMapper extends BaseMapperX<SeatMap> {

    SeatMapVO findPublishedMap(@Param("roomId") Long roomId);

    Integer findNextVersionNo(@Param("roomId") Long roomId);

    List<SeatMapVO.SeatVO> findSeatsByMapId(@Param("mapId") Long mapId);
}
