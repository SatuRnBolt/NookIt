package com.nookit.modules.student.reservation.service;

import com.nookit.common.api.PageResult;
import com.nookit.modules.student.reservation.dto.CreateReservationReq;

import java.util.Map;

public interface StudentReservationService {

    PageResult<Map<String, Object>> listMyReservations(Long userId, int page, int pageSize, String status);

    Map<String, Object> createReservation(Long userId, CreateReservationReq req);

    void cancelReservation(Long userId, Long reservationId);
}
