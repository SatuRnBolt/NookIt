package com.nookit.modules.admin.booking.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.nookit.common.domain.org.User;
import com.nookit.common.domain.reservation.Reservation;
import com.nookit.common.domain.violation.Violation;
import com.nookit.modules.admin.booking.mapper.BookingMapper;
import com.nookit.modules.admin.user.mapper.UserAdminMapper;
import com.nookit.modules.admin.violation.mapper.ViolationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约状态结算任务：未签到且已过签到截止时间的预约，自动判定为违约。
 * <ul>
 *   <li>reservation_status: {@code pending_checkin} -> {@code violated}</li>
 *   <li>写入一条 {@code violations}（类型 {@code no_checkin}）</li>
 *   <li>对应用户 {@code violation_count + 1}</li>
 * </ul>
 * 每分钟执行一次（应用启动后立即先跑一次）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationSettlementTask {

    private final BookingMapper bookingMapper;
    private final ViolationMapper violationMapper;
    private final UserAdminMapper userAdminMapper;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void settleOverdueReservations() {
        LocalDateTime now = LocalDateTime.now();

        // 待签到，且：签到截止时间已过；若无截止时间则按结束时间已过
        List<Reservation> overdue = bookingMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getReservationStatus, "pending_checkin")
                        .and(w -> w
                                .lt(Reservation::getCheckinDeadlineAt, now)
                                .or(o -> o.isNull(Reservation::getCheckinDeadlineAt)
                                        .lt(Reservation::getEndAt, now))
                        )
        );
        if (overdue.isEmpty()) {
            return;
        }

        for (Reservation r : overdue) {
            r.setReservationStatus("violated");
            bookingMapper.updateById(r);

            // violations.reservation_id 唯一，先查重避免重复插入
            long exists = violationMapper.selectCount(
                    new LambdaQueryWrapper<Violation>().eq(Violation::getReservationId, r.getId()));
            if (exists > 0) {
                continue;
            }

            Violation v = new Violation();
            v.setReservationId(r.getId());
            v.setUserId(r.getUserId());
            v.setViolationType("no_checkin");
            v.setViolationStatus("active");
            v.setOccurredAt(now);
            v.setPoints(1);
            v.setDescriptionText("超过签到时间未签到，系统自动判定违约");
            violationMapper.insert(v);

            // 用户违约次数 +1（原子自增）
            userAdminMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("violation_count = violation_count + 1")
                    .eq(User::getId, r.getUserId()));
        }

        log.info("[违约结算] 处理超时未签到预约 {} 条", overdue.size());
    }
}
