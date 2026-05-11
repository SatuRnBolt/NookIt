package com.nookit.common.domain.reservation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservations")
public class Reservation extends BaseEntity {

    private String reservationNo;
    private Long userId;
    private Long createdByUserId;
    private Long studyRoomId;
    private Long seatId;
    private LocalDate reservationDate;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer startHour;
    private Integer endHour;
    private Integer durationHours;
    private String reservationStatus;
    private String sourceChannel;
    private String cancelReasonType;
    private String cancelReasonNote;
    private LocalDateTime reminderAt;
    private LocalDateTime warningAt;
    private LocalDateTime checkinDeadlineAt;
    private LocalDateTime checkedInAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private Long repeatedFromReservationId;
    private String policySnapshotJson;
    private String notesText;
}
