package project.jpa.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Report;
import project.jpa.dto.reportapidto.AdminReportDto;
import project.jpa.dto.reportapidto.MyReportDto;
import project.jpa.enums.ReportStatus;
import project.jpa.repository.ReportRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportQueryService {

    private final ReportRepository reportRepository;


    /**
     * 2. FR#17. [사용자용] 내 신고 내역 조회 (엔티티 노출 차단, 즉시 DTO 변환)
     */
    public Page<MyReportDto> findMyReports(Long memberId, Pageable pageable) {

        // 1. DB에서 엔티티를 Page로 가져옴
        Page<Report> reportPage = reportRepository.findMyReports(memberId, pageable);

        // 2. 트랜잭션 안에서 안전하게 DTO로 변환하여 반환
        return reportPage.map(report -> new MyReportDto(
                report.getId(),                                         //신고 PK
                report.getSeat().getBuildingName(),                     //신고한 건물 이름
                report.getSeat().getFloor(),                            //신고한 건물 층수
                report.getSeat().getSpaceType().getDescription(),       //신고한 좌석 유형
                report.getSeat().getSeatNumber(),                       //신고환 좌석 번호
                report.getContent(),                                    //신고 내용
                report.getStatus().getDescription(),                    //신고 상태
                report.getCreatedAt()                                   //신고 접수일
        ));
    }

    /**
     * 3. FR#18. [관리자용] 상태별 전체 신고 내역 조회 (엔티티 노출 차단, 즉시 DTO 변환)
     */
    public Page<AdminReportDto> findReportsByStatus(ReportStatus status, Pageable pageable) {

        // 1. DB에서 엔티티를 Page로 가져옴
        Page<Report> reportPage = reportRepository.findReportsByStatus(status, pageable);

        // 2. 💡 트랜잭션 안에서 안전하게 DTO로 변환하여 반환
        return reportPage.map(report -> new AdminReportDto(
                report.getId(),                                     //신고 PK
                report.getMember().getName(),                       //신고한 회원 이름
                report.getSeat().getBuildingName(),                 //신고한 건물 이름
                report.getSeat().getFloor(),                        //신고한 건물 층수
                report.getSeat().getSpaceType().getDescription(),   //신고한 좌석 유형
                report.getSeat().getSeatNumber(),                   //신고한 좌석 번호
                report.getContent(),                                //신고한 내용
                report.getStatus().getDescription(),                //신고 상태
                report.getCreatedAt()                               //신고 접수일
        ));
    }

}
