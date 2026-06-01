package project.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.jpa.domain.Report;
import project.jpa.enums.ReportStatus;

public interface ReportRepositoryCustom {

    // 1. [사용자용] 내 신고 내역 페이징 조회
    Page<Report> findMyReports(Long memberId, Pageable pageable);

    // 2. [관리자용] 상태별 전체 신고 내역 페이징 조회 (동적 쿼리)
    Page<Report> findReportsByStatus(ReportStatus status, Pageable pageable);

}
