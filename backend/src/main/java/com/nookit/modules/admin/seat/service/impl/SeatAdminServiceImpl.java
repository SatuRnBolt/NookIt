package com.nookit.modules.admin.seat.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nookit.common.api.PageResult;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.seat.Seat;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.seat.dto.SeatAdminCreateReq;
import com.nookit.modules.admin.seat.dto.SeatAdminVO;
import com.nookit.modules.admin.seat.dto.SeatPageQuery;
import com.nookit.modules.admin.seat.mapper.SeatAdminMapper;
import com.nookit.modules.admin.seat.service.SeatAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SeatAdminServiceImpl implements SeatAdminService {

    private final SeatAdminMapper seatAdminMapper;

    @Override
    public PageResult<SeatAdminVO> pageSeats(SeatPageQuery query) {
        Page<SeatAdminVO> page = new Page<>(query.getPage(), query.getPageSize());
        seatAdminMapper.pageSeats(page, query);
        return PageResult.from(page);
    }

    @Override
    public SeatAdminVO createSeat(SeatAdminCreateReq req) {
        Seat seat = new Seat();
        seat.setStudyRoomId(req.getRoomId());
        seat.setSeatCode(req.getSeatCode());
        seat.setDisplayLabel(req.getSeatCode());
        seat.setSeatType("standard");
        seat.setHasPower(req.getHasPower());
        seat.setIsWindowSide(req.getIsWindowSide());
        seat.setIsAccessible(false);
        seat.setSeatStatus("active");
        seat.setIsBookable(true);
        seatAdminMapper.insert(seat);

        SeatAdminVO vo = new SeatAdminVO();
        vo.setId(seat.getId());
        vo.setSeatNo(seat.getSeatCode());
        vo.setHasPower(seat.getHasPower());
        vo.setNearWindow(seat.getIsWindowSide());
        vo.setStatus(seat.getSeatStatus());
        return vo;
    }

    @Override
    public void updateSeat(Long id, SeatAdminCreateReq req) {
        Seat seat = seatAdminMapper.selectById(id);
        if (seat == null) throw new ResourceNotFoundException(ResultCode.SEAT_NOT_FOUND);
        seat.setStudyRoomId(req.getRoomId());
        seat.setSeatCode(req.getSeatCode());
        seat.setHasPower(req.getHasPower());
        seat.setIsWindowSide(req.getIsWindowSide());
        seatAdminMapper.updateById(seat);
    }

    @Override
    public void updateSeatStatus(Long id, String status) {
        Seat seat = seatAdminMapper.selectById(id);
        if (seat == null) throw new ResourceNotFoundException(ResultCode.SEAT_NOT_FOUND);
        Seat upd = new Seat();
        upd.setId(id);
        upd.setSeatStatus(status);
        seatAdminMapper.updateById(upd);
    }

    @Override
    public void deleteSeat(Long id) {
        if (seatAdminMapper.selectById(id) == null) throw new ResourceNotFoundException(ResultCode.SEAT_NOT_FOUND);
        seatAdminMapper.deleteById(id);
    }
}
