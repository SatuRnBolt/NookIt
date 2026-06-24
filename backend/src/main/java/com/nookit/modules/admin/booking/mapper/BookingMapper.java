package com.nookit.modules.admin.booking.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.domain.reservation.Reservation;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.booking.dto.BookingPageQuery;
import com.nookit.modules.admin.booking.dto.BookingStatsVO;
import com.nookit.modules.admin.booking.dto.BookingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookingMapper extends BaseMapperX<Reservation> {

    IPage<BookingVO> pageBookings(Page<BookingVO> page, @Param("q") BookingPageQuery query);

    BookingStatsVO queryStats(@Param("tab") String tab);
}
