package project.jpa.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Report;
import project.jpa.domain.Seat;
import project.jpa.enums.MemberRole;
import project.jpa.enums.ReportStatus;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.ReportRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportServiceTest {

    @Autowired
    ReportService reportService;

    @Autowired
    SeatService seatService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    UsageHistoryRepository usageHistoryRepository;

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 좌석 신고 시 신고가 접수되고, 좌석은 점검 중으로 바뀌며, 이용 기록은 종료되어야 한다")
    void 시설신고_및_이용강제종료_성공() {

        // given: 회원 가입 및 좌석 예약 세팅
        Member member = Member.createMember("testUser1", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);

        // 회원이 좌석을 예약해서 현재 사용 중(USING)인 상태로 만듦
        seatService.startUsingSeat(member.getId(), seatId);

        // when: 시설 불편 신고 실행
        Long reportId = reportService.createReport(member.getId(), seatId, "콘센트가 작동하지 않습니다.");

        // then 1: 신고 내역이 RECEIVED 상태로 잘 저장되었는지 검증
        Report findReport = reportRepository.findById(reportId).get();
        Assertions.assertThat(findReport.getContent()).isEqualTo("콘센트가 작동하지 않습니다.");
        Assertions.assertThat(findReport.getStatus()).isEqualTo(ReportStatus.RECEIVED);

        // then 2: 좌석 상태가 MAINTENANCE(점검 중)로 즉시 변경되었는지 검증
        Seat findSeat = seatRepository.findById(seatId).get();
        Assertions.assertThat(findSeat.getStatus()).isEqualTo(SeatStatus.MAINTENANCE);

        // then 3: [핵심] 회원의 사용 중(USING) 기록이 0개가 되었는지(완료 처리되었는지) 검증
        long activeCount = usageHistoryRepository.countActiveUsageByMemberId(member.getId());
        Assertions.assertThat(activeCount).isEqualTo(0);
    }

    @Test
//    @DisplayName("실패: 본인이 이용 중이지 않은 빈 좌석을 신고하려 하면 예외가 발생해야 한다")
    void 빈좌석_신고_예외발생() {

        // given: 빈 좌석 세팅 (예약을 하지 않음)
        Member member = Member.createMember("testUser2", "1234", "이름", MemberRole.USER);
        memberRepository.save(member);

        Long emptySeatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-02", 1, 2);

        // when & then: 예약 없이 바로 신고 시도
        try {
            reportService.createReport(member.getId(), emptySeatId, "여기에 쓰레기가 많습니다.");
            fail("본인이 이용 중이 아닌데 신고가 접수되었습니다.");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("현재 본인이 이용 중인 좌석만 신고할 수 있습니다");
        }
    }

    @Test
//    @DisplayName("실패(도배 방지): 한 번 신고하여 이용이 종료된 후, 동일한 좌석을 연속으로 다시 신고하면 예외가 발생한다")
    void 중복도배_신고_차단_성공() {

        // given: 정상적으로 예약된 상태
        Member member = Member.createMember("testUser3", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-03", 1, 3);
        seatService.startUsingSeat(member.getId(), seatId);

        // 1차 신고 (정상 성공, 여기서 이용 기록이 COMPLETED 됨)
        reportService.createReport(member.getId(), seatId, "의자가 부서졌어요 (1차)");

        // when & then: 악의적으로 2차 신고 버튼을 연속해서 눌렀을 때
        try {
            reportService.createReport(member.getId(), seatId, "의자가 부서졌어요 (2차)");
            fail("이용이 종료된 좌석에 대해 중복 신고가 접수되었습니다.");
        } catch (IllegalStateException e) {
            // 이미 1차 신고 때 이용 기록이 종료되었으므로, 방어 로직에 의해 차단됨
            Assertions.assertThat(e.getMessage()).contains("현재 본인이 이용 중인 좌석만 신고할 수 있습니다");
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 관리자가 신고를 확인하고 수리를 완료하면, 신고 상태가 RESOLVED가 되고 좌석은 다시 AVAILABLE로 원복되어야 한다")
    void 신고처리완료_및_좌석원복_성공() {

        // [1. Given] 신고가 접수되어 좌석이 점검 중인 상태에서 시작
        Member member = Member.createMember("adminTest", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-05", 1, 5);
        seatService.startUsingSeat(member.getId(), seatId); // 사용 시작

        // 학생이 고장 신고 (이 시점에 좌석은 MAINTENANCE 상태가 됨)
        Long reportId = reportService.createReport(member.getId(), seatId, "의자 높낮이 조절이 안 됩니다.");

        // [2. When - Step 1] 관리자가 수리를 시작 (IN_PROGRESS)
        reportService.startRepairingReport(reportId);
        Report inProgressReport = reportRepository.findById(reportId).get();
        Assertions.assertThat(inProgressReport.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);

        // [3. When - Step 2] 관리자가 수리를 완료 (RESOLVED)
        reportService.resolveReport(reportId);

        // [4. Then] 최종 상태 검증
        Report finalReport = reportRepository.findById(reportId).get();
        Seat finalSeat = seatRepository.findById(seatId).get();

        // 신고 상태가 RESOLVED(완료)인가?
        Assertions.assertThat(finalReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);

        // 핵심: 좌석이 다시 AVAILABLE(이용 가능)로 돌아왔는가?
        Assertions.assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 관리자가 수리를 시작하면 신고 상태가 IN_PROGRESS(처리 중)로 변경되어야 한다")
    void 수리시작_상태변경_성공() {

        // given: 회원 세팅, 좌석 예약, 그리고 신고 접수
        Member member = Member.createMember("adminTest2", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-06", 1, 6);
        seatService.startUsingSeat(member.getId(), seatId);

        // 학생이 고장 신고 (초기 상태: RECEIVED)
        Long reportId = reportService.createReport(member.getId(), seatId, "책상이 심하게 흔들립니다.");

        // when: 관리자가 수리 시작 버튼을 누름
        reportService.startRepairingReport(reportId);

        // then: 신고 상태가 IN_PROGRESS로 정상적으로 바뀌었는지 검증 (더티 체킹 확인)
        Report findReport = reportRepository.findById(reportId).get();
        Assertions.assertThat(findReport.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
    }

    @Test
//    @DisplayName("실패: 이미 처리 완료된(RESOLVED) 신고를 수리 시작하려고 하면 예외가 발생해야 한다")
    void 이미_완료된_신고_수리시작_예외발생() {

        // given
        Member member = Member.createMember("adminTest3", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-07", 1, 7);
        seatService.startUsingSeat(member.getId(), seatId);

        Long reportId = reportService.createReport(member.getId(), seatId, "조명이 안 들어옵니다.");

        // 관리자가 이미 수리를 끝내고 '처리 완료'를 누른 상태라고 가정 (RECEIVED -> RESOLVED)
        reportService.resolveReport(reportId);

        // when & then: 완료된 건에 대해 다시 '수리 시작'을 누르면 엔티티의 방어 로직이 작동해야 함
        try {
            reportService.startRepairingReport(reportId);
            fail("완료된 신고를 수리 시작했는데 예외가 발생하지 않았습니다.");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("이미 처리 완료된 신고입니다");
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 관리자가 처리를 완료하면 신고 상태는 RESOLVED가 되고, 좌석은 AVAILABLE로 원복되어야 한다")
    void 신고처리완료_및_좌석상태원복_성공() {

        // given: 회원 가입, 좌석 세팅, 신고 접수 및 수리 시작까지 진행
        Member member = Member.createMember("adminTest4", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-08", 1, 8);
        seatService.startUsingSeat(member.getId(), seatId); // 예약 (USING)

        // 고장 신고 접수 (좌석: MAINTENANCE, 신고: RECEIVED)
        Long reportId = reportService.createReport(member.getId(), seatId, "의자 바퀴가 빠졌습니다.");

        // 관리자 수리 시작 (신고: IN_PROGRESS)
        reportService.startRepairingReport(reportId);

        // when: 관리자가 수리를 마치고 완료 처리를 누름
        reportService.resolveReport(reportId);

        // then 1: 신고 상태가 RESOLVED(완료)로 변경되었는지 검증
        Report finalReport = reportRepository.findById(reportId).get();
        Assertions.assertThat(finalReport.getStatus()).isEqualTo(ReportStatus.RESOLVED);

        // then 2:  [핵심] 좌석 상태가 AVAILABLE(이용 가능)로 확실하게 돌아왔는지 검증
        Seat finalSeat = seatRepository.findById(seatId).get();
        Assertions.assertThat(finalSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
//    @DisplayName("실패: DB에 존재하지 않는 신고 내역 번호로 완료 처리를 시도하면 예외가 발생해야 한다")
    void 존재하지않는_신고_처리완료_예외발생() {

        // given: 엉뚱한(존재하지 않는) 신고 ID
        Long fakeReportId = 999999L;

        // when & then: 없는 내역을 조회하려 했으므로 우리가 설정한 예외가 터져야 함
        try {
            reportService.resolveReport(fakeReportId);
            fail("존재하지 않는 신고인데 예외가 발생하지 않았습니다.");
        } catch (IllegalArgumentException e) {
            Assertions.assertThat(e.getMessage()).contains("존재하지 않는 신고 내역입니다");
        }
    }

}