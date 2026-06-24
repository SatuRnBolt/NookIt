package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "座位地图 VO")
public class SeatMapVO {

    private Long id;
    private Integer versionNo;
    private String mapStatus;
    private Integer mapWidth;
    private Integer mapHeight;
    private String backgroundUrl;
    private LocalDateTime publishedAt;
    private List<SeatVO> seats;

    @Data
    @Schema(description = "地图内座位 VO")
    public static class SeatVO {
        private Long id;
        private String seatCode;
        private String displayLabel;
        private String seatType;
        private Integer rowNo;
        private Integer colNo;
        private Boolean hasPower;
        private Boolean isWindowSide;
        private Boolean isAccessible;
        private String seatStatus;
        private Boolean isBookable;
        private Double mapX;
        private Double mapY;
        private Double mapWidth;
        private Double mapHeight;
        private Double mapRotation;
    }
}
