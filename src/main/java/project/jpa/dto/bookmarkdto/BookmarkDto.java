package project.jpa.dto.bookmarkdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookmarkDto {

    private Long bookmarkId;     // 즐겨찾기 자체의 PK
    private Long seatId;         // 좌석의 PK (선택 시 예약으로 넘어가기 위해 필수)

    private String buildingName; // 예: 호천관
    private Integer floor;       // 예: 1
    private String spaceType;    // 예: "스터디 라운지" (Enum의 한글 설명)
    private String seatNumber;   // 예: A-1
    private String status;       // 예: "이용 가능" (프론트에서 색상 다르게 표시할 때 유용함)
}
