package project.jpa.dto.reportapidto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminReportDto {

    private Long reportId;       //신고 PK
    private String reporterName; // 관리자용은 누가 신고했는지 볼 수 있게 추가
    private String buildingName; //건물 이름
    private Integer floor;       //건물 층수
    private String spaceType;    //장소 유형
    private String seatNumber;   //좌석 번호
    private String content;      //신고 내용
    private String status;       //신고 상태
    private LocalDateTime createdAt; //신고 날짜
}
