package com.nookit.modules.admin.booking.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.reservation.Reservation;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.booking.dto.BookingPageQuery;
import com.nookit.modules.admin.booking.dto.BookingStatsVO;
import com.nookit.modules.admin.booking.dto.BookingVO;
import com.nookit.modules.admin.booking.mapper.BookingMapper;
import com.nookit.modules.admin.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingMapper bookingMapper;

    @Override
    public PageResult<BookingVO> pageBookings(BookingPageQuery query) {
        Page<BookingVO> page = new Page<>(query.getPage(), query.getPageSize());
        bookingMapper.pageBookings(page, query);
        return PageResult.from(page);
    }

    @Override
    public BookingStatsVO getStats(String tab) {
        return bookingMapper.queryStats(tab);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, String reason) {
        Reservation r = bookingMapper.selectById(id);
        if (r == null) throw new ResourceNotFoundException(ResultCode.RESERVATION_NOT_FOUND);
        r.setReservationStatus("cancelled");
        r.setCancelReasonNote(reason);
        r.setCancelledAt(LocalDateTime.now());
        bookingMapper.updateById(r);
    }

    @Override
    @Transactional
    public void checkinBooking(Long id) {
        Reservation r = bookingMapper.selectById(id);
        if (r == null) throw new ResourceNotFoundException(ResultCode.RESERVATION_NOT_FOUND);
        r.setReservationStatus("checked_in");
        r.setCheckedInAt(LocalDateTime.now());
        bookingMapper.updateById(r);
    }
}
