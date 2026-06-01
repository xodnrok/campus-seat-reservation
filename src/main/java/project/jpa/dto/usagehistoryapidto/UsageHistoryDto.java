package project.jpa.dto.usagehistoryapidto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UsageHistoryDto {

    private Long historyId;          // 이용 기록 PK
    private Long seatId;             //  프론트엔드 버튼(즐겨찾기, 신고) 작동을 위해 추가
    private String buildingName;     // 사용했던 건물명 (예: 호천관)

    private Integer floor;           // 층수
    private String spaceType;        // 장소 유형

    private String seatNumber;       // 사용했던 좌석 번호 (예: A-1)

    private LocalDateTime startTime; // 좌석 사용 시작 시간
    private LocalDateTime endTime;   // 좌석 사용 종료 시간 (아직 사용 중이면 null)

    private String status;           // 현재 상태 (예: USING(사용중), COMPLETED(사용완료), CANCELED(취소됨))
}
