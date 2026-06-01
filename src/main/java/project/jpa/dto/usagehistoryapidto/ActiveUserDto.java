package project.jpa.dto.usagehistoryapidto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import project.jpa.enums.SpaceType;

import java.time.LocalDateTime;

@Data
public class ActiveUserDto {

    private Long historyId;      // 강제 퇴실을 위해 필요한 이용 기록 PK
    private Long seatId;         // 좌석 PK
    private String memberName;   // 사용자 이름
    private String loginId;      // 사용자 아이디
    private String buildingName; // 건물명
    private Integer floor;       // 층수
    private SpaceType spaceType; // 공간 유형
    private String seatNumber;   // 좌석 번호
    private LocalDateTime startTime; // 이용 시작 시간


    @QueryProjection // 💡 QueryDSL 연동을 위한 어노테이션
    public ActiveUserDto(Long historyId, Long seatId, String memberName, String loginId,
                         String buildingName, Integer floor, SpaceType spaceType,
                         String seatNumber, LocalDateTime startTime) {
        this.historyId = historyId;
        this.seatId = seatId;
        this.memberName = memberName;
        this.loginId = loginId;
        this.buildingName = buildingName;
        this.floor = floor;
        this.spaceType = spaceType;
        this.seatNumber = seatNumber;
        this.startTime = startTime;
    }

}
