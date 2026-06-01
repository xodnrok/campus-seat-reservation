package project.jpa.dto.seatapidto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;

@Data
public class SeatDto {
    private Long seatId; //Seat PK
    private String buildingName; //건물이름
    private Integer floor; //층
    private String seatNumber; //좌석 번호
    private SpaceType spaceType; // 장소 유형
    private SeatStatus status; //좌석 상태

    //화면에 바둑판을 그리기 위해 꼭 필요한 좌표 필드 추가
    private Integer rowIndex;
    private Integer colIndex;

    @QueryProjection
    public SeatDto(Long seatId, String buildingName, Integer floor, String seatNumber, SpaceType spaceType, SeatStatus status
    ,Integer rowIndex, Integer colIndex) {
        this.seatId = seatId;
        this.buildingName = buildingName;
        this.floor = floor;
        this.seatNumber = seatNumber;
        this.spaceType = spaceType;
        this.status = status;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }
}
