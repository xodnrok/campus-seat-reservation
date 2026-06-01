package project.jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Report;
import project.jpa.domain.Seat;
import project.jpa.domain.UsageHistory;
import project.jpa.enums.ReportStatus;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.ReportRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;
    private final UsageHistoryRepository usageHistoryRepository;

    /**
     * FR#14. 시설 고장 및 불편 신고 접수(회원이 앉은 좌석을 신고시 회원의 기록을 완료시간,완료로 바꾸고 좌석상태도 점검중으로 바꾼다.)
     */
    public Long createReport(Long memberId, Long seatId, String content) {

        // 1. [검증] 신고자와 대상 좌석 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 2. [검증 및 조회] "현재 이용 중인 기록"을 직접 가져옵니다.
        // (단순 존재 여부 체크 대신 엔티티를 직접 꺼내서 종료 처리하기 위함)
        UsageHistory activeHistory = usageHistoryRepository.findActiveHistoryByMemberAndSeat(memberId, seatId)
                .orElseThrow(() -> new IllegalStateException("현재 본인이 이용 중인 좌석만 신고할 수 있습니다."));

        // 3. [실행] 신고 내역(Report) 생성 및 저장
        Report report = Report.createReport(member, seat, content);
        reportRepository.save(report);

        // 💡 4. [실행 - 상태 변경] 좌석은 '점검 중'으로, 이용 기록은 '종료'로 처리
        seat.changeToMaintenance();    // 좌석 상태: USING -> MAINTENANCE
        activeHistory.completeUsage(); // 이용 기록: USING -> COMPLETED 및 종료 시간 기록

        return report.getId();
    }

    /**
     *  FR#9. [관리자용] 신고 건에 대해 수리(점검) 작업을 시작함
     */
    public void startRepairingReport(Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고 내역입니다."));

        // 상태를 PENDING -> IN_PROGRESS 로 변경 (더티 체킹 발동)
        report.startProgress();
    }

    /**
     * FR#9. 관리자 전용: 접수된 신고 처리 완료
     */
    public void resolveReport(Long reportId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고 내역입니다."));

        // 상태를 완료(RESOLVED)로 변경하는 엔티티 비즈니스 메서드 호출 (더티 체킹 발동)
        report.resolve();

        // 2. 수리가 끝났으므로 해당 좌석을 다시 이용 가능(AVAILABLE) 상태로 원복 (더티 체킹)
        // Report 엔티티 안에 Seat 연관관계가 설정되어 있다고 가정 (report.getSeat())
        report.getSeat().resolveMaintenance();
    }

}
