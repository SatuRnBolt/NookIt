package com.nookit.modules.admin.seat.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.seat.Seat;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.seat.dto.SeatAdminVO;
import com.nookit.modules.admin.seat.dto.SeatPageQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeatAdminMapper extends BaseMapperX<Seat> {

    IPage<SeatAdminVO> pageSeats(Page<SeatAdminVO> page, @Param("q") SeatPageQuery query);
}
