package project.jpa.dto.seatapidto;

import lombok.Data;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;

@Data
public class SeatSearchCondition {

    private String buildingName; //건물이름 예: "호천관"
    private Integer floor;       //층수 예: 7
    private SpaceType spaceType; //장소 유형 예: STUDY_LOUNGE
    private SeatStatus status;   //좌석의 상태 예: AVAILABLE (보통 '이용 가능'한 자리만 찾을 때 사용)

}
