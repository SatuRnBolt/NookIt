package com.nookit.modules.admin.seat.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.admin.seat.dto.SeatAdminCreateReq;
import com.nookit.modules.admin.seat.dto.SeatAdminVO;
import com.nookit.modules.admin.seat.dto.SeatPageQuery;

public interface SeatAdminService {

    PageResult<SeatAdminVO> pageSeats(SeatPageQuery query);

    SeatAdminVO createSeat(SeatAdminCreateReq req);

    void updateSeat(Long id, SeatAdminCreateReq req);

    void updateSeatStatus(Long id, String status);

    void deleteSeat(Long id);
}
