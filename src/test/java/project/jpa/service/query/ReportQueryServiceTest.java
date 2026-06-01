package project.jpa.service.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.dto.reportapidto.AdminReportDto;
import project.jpa.dto.reportapidto.MyReportDto;
import project.jpa.enums.MemberRole;
import project.jpa.enums.ReportStatus;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.service.ReportService;
import project.jpa.service.SeatService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ReportQueryServiceTest {

    @Autowired
    ReportService reportService;

    @Autowired
    ReportQueryService reportQueryService;

    @Autowired
    SeatService seatService;

    @Autowired
    MemberRepository memberRepository;


    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 관리자가 특정 상태(RECEIVED, IN_PROGRESS, RESOLVED)를 요청하면 해당 상태의 신고 내역 DTO가 정확하게 페이징 조회되어야 한다")
    void 상태별_신고내역_필터링_조회_성공() {

        // given: 1인 1석 룰을 지키기 위해 3명의 각기 다른 테스트 회원 생성
        Member member1 = Member.createMember("adminSearch1", "1234", "학생A", MemberRole.USER);
        Member member2 = Member.createMember("adminSearch2", "1234", "학생B", MemberRole.USER);
        Member member3 = Member.createMember("adminSearch3", "1234", "학생C", MemberRole.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);
        memberRepository.save(member3);

        // 3개의 좌석을 생성
        Long seatId1 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-10", 1, 10);
        Long seatId2 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-11", 1, 11);
        Long seatId3 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-12", 1, 12);

        // 각각의 회원이 1자리씩 따로 예약 (1인 1석 제약 통과)
        seatService.startUsingSeat(member1.getId(), seatId1);
        seatService.startUsingSeat(member2.getId(), seatId2);
        seatService.startUsingSeat(member3.getId(), seatId3);

        // 3건의 신고 접수 (초기 상태는 모두 RECEIVED)
        Long reportId1 = reportService.createReport(member1.getId(), seatId1, "의자 파손 (대기 건)");
        Long reportId2 = reportService.createReport(member2.getId(), seatId2, "책상 오염 (처리 중 건)");
        Long reportId3 = reportService.createReport(member3.getId(), seatId3, "콘센트 고장 (완료 건)");

        // [데이터 조작] 2번 신고는 '처리 중'으로 변경
        reportService.startRepairingReport(reportId2);

        // [데이터 조작] 3번 신고는 '처리 완료'로 변경
        reportService.startRepairingReport(reportId3);
        reportService.resolveReport(reportId3);

        // 페이징 요청 객체 생성 (0페이지, 10개씩)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when: 각각의 상태 값으로 3번의 조회를 실행 (쿼리 서비스 호출 -> Page<AdminReportDto> 반환)
        Page<AdminReportDto> receivedReports = reportQueryService.findReportsByStatus(ReportStatus.RECEIVED, pageRequest);
        Page<AdminReportDto> inProgressReports = reportQueryService.findReportsByStatus(ReportStatus.IN_PROGRESS, pageRequest);
        Page<AdminReportDto> resolvedReports = reportQueryService.findReportsByStatus(ReportStatus.RESOLVED, pageRequest);

        // then: 각각 다른 상태의 데이터가 정확하게 1건씩 필터링되어 나와야 함

        // 1. 접수 대기(RECEIVED) 검증
        Assertions.assertThat(receivedReports.getTotalElements()).isEqualTo(1); // 전체 개수가 1개인지
        Assertions.assertThat(receivedReports.getContent().get(0).getContent()).isEqualTo("의자 파손 (대기 건)"); // 내용 검증

        // 2. 처리 중(IN_PROGRESS) 검증
        Assertions.assertThat(inProgressReports.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(inProgressReports.getContent().get(0).getContent()).isEqualTo("책상 오염 (처리 중 건)");

        // 3. 처리 완료(RESOLVED) 검증
        Assertions.assertThat(resolvedReports.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(resolvedReports.getContent().get(0).getContent()).isEqualTo("콘센트 고장 (완료 건)");
    }

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 본인이 신고한 내역만 최신순으로 정확하게 페이징 조회되며, 타인의 신고 내역은 배제되어야 한다")
    void 내_신고내역_조회_및_정렬_성공() throws InterruptedException {

        // given: 2명의 회원 생성 (내 계정, 타인 계정)
        Member myMember = Member.createMember("myAccount", "1234", "내이름", MemberRole.USER);
        Member otherMember = Member.createMember("otherAccount", "1234", "타인", MemberRole.USER);
        memberRepository.save(myMember);
        memberRepository.save(otherMember);

        // 테스트용 좌석 3개 생성
        Long seatId1 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-20", 1, 20);
        Long seatId2 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-21", 1, 21);
        Long seatId3 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-22", 1, 22);

        // [내 계정]으로 1번째 신고 접수
        seatService.startUsingSeat(myMember.getId(), seatId1);
        reportService.createReport(myMember.getId(), seatId1, "내 첫번째 신고");

        // 최신순 정렬(OrderByCreatedAtDesc)을 명확하게 검증하기 위해 아주 미세한 시간차(10ms)를 둡니다.
        Thread.sleep(10);

        // [내 계정]으로 2번째 신고 접수
        seatService.startUsingSeat(myMember.getId(), seatId2);
        reportService.createReport(myMember.getId(), seatId2, "내 두번째 신고 (최신)");

        // [타인 계정]으로 신고 접수 (이 데이터는 내 목록에 나오면 안 됨)
        seatService.startUsingSeat(otherMember.getId(), seatId3);
        reportService.createReport(otherMember.getId(), seatId3, "타인의 신고");

        //  페이징 요청 객체 생성 (0페이지, 10개씩)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when: 내 계정의 ID로만 신고 내역을 쿼리 서비스에서 조회 (Page<MyReportDto> 반환)
        Page<MyReportDto> myReports = reportQueryService.findMyReports(myMember.getId(), pageRequest);

        // then 1: 총 3건의 신고 중, 내 계정으로 한 2건만 조회되어야 함 (getTotalElements 활용)
        Assertions.assertThat(myReports.getTotalElements()).isEqualTo(2);

        // then 2: [최신순 정렬 검증] 나중에 신고한 '두번째 신고'가 리스트의 맨 위(인덱스 0)에 와야 함
        Assertions.assertThat(myReports.getContent().get(0).getContent()).isEqualTo("내 두번째 신고 (최신)");
        Assertions.assertThat(myReports.getContent().get(1).getContent()).isEqualTo("내 첫번째 신고");
    }

    @Test
//    @DisplayName("성공: 신고 내역이 아예 없는 회원이 조회할 경우, 에러 없이 빈 페이지 객체를 반환해야 한다")
    void 신고내역_없는_회원_조회_성공() {

        // given: 신고를 한 번도 하지 않은 청정 회원
        Member newMember = Member.createMember("cleanAccount", "1234", "신규회원", MemberRole.USER);
        memberRepository.save(newMember);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when: 쿼리 서비스에서 조회 시도
        Page<MyReportDto> emptyReports = reportQueryService.findMyReports(newMember.getId(), pageRequest);

        // then: NullPointerException이 터지지 않고, 내용이 0개인 Page 객체가 안전하게 반환됨
        Assertions.assertThat(emptyReports.isEmpty()).isTrue(); // 페이지가 비어있는지 확인
        Assertions.assertThat(emptyReports.getTotalElements()).isEqualTo(0); // 총 개수가 0개인지 확인
    }
}