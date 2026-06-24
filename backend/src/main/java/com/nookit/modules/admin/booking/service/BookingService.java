package com.nookit.modules.admin.booking.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.booking.dto.BookingPageQuery;
import com.nookit.modules.admin.booking.dto.BookingStatsVO;
import com.nookit.modules.admin.booking.dto.BookingVO;

public interface BookingService {

    PageResult<BookingVO> pageBookings(BookingPageQuery query);

    BookingStatsVO getStats(String tab);

    void cancelBooking(Long id, String reason);

    void checkinBooking(Long id);
}
