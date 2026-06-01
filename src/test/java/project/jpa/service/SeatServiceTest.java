package project.jpa.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.domain.UsageHistory;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;
import project.jpa.enums.UsageStatus;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SeatServiceTest {

    @Autowired
    SeatService seatService;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    UsageHistoryRepository usageHistoryRepository;

    @Test
//    @Rollback(value = false)
    public void 좌석_등록() {

        //given : 새로운 좌석 정보 준비
        String building = "호천관";
        Integer floor = 7;
        SpaceType type = SpaceType.STUDY_LOUNGE;
        String number = "A-10";
        Integer row = 1;
        Integer col = 5;

        // when: 좌석 등록 서비스 호출
        Long savedId = seatService.registerSeat(building, floor, type, number, row, col);

        // then: DB에서 조회하여 값이 일치하는지 검증
        Seat findSeat = seatRepository.findById(savedId).get();

        Assertions.assertThat(findSeat.getBuildingName()).isEqualTo(building);
        Assertions.assertThat(findSeat.getFloor()).isEqualTo(floor);
        Assertions.assertThat(findSeat.getSpaceType()).isEqualTo(type);
        Assertions.assertThat(findSeat.getSeatNumber()).isEqualTo(number);
        Assertions.assertThat(findSeat.getRowIndex()).isEqualTo(row);
    }

    @Test
//    @Rollback(value = false)
    public void 좌석_수정() {

        // given: 먼저 좌석을 하나 등록함
        Long seatId = seatService.registerSeat("정보관", 1, SpaceType.REST_AREA, "R-01", 10, 10);

        // when: 등록된 좌석의 정보를 수정 (건물명 변경, 장소 유형 변경 등)
        String newBuilding = "호천관";
        SpaceType newType = SpaceType.STUDY_LOUNGE;
        Integer newRow = 5;

        seatService.updateSeatInfo(seatId, newBuilding, 7, newType, "R-01-Modified", newRow, 5);

        // then: DB에서 다시 조회했을 때 변경된 내용이 반영되었는지 확인
        Seat updatedSeat = seatRepository.findById(seatId).get();

        Assertions.assertThat(updatedSeat.getBuildingName()).isEqualTo(newBuilding);
        Assertions.assertThat(updatedSeat.getSpaceType()).isEqualTo(newType);
        Assertions.assertThat(updatedSeat.getRowIndex()).isEqualTo(newRow);


    }


    @Test
//    @Rollback(value = false)
    public void 시설_상태_제어_점검중() {
        // given: 이용 가능한 좌석 하나를 등록함 (기본 상태 AVAILABLE)
        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "A-01", 1, 1);

        // when: 관리자가 해당 좌석을 점검 중으로 변경
        seatService.changeSeatToMaintenance(seatId);

        // then: DB에서 좌석 상태가 MAINTENANCE인지 확인
        Seat findSeat = seatRepository.findById(seatId).get();
        Assertions.assertThat(findSeat.getStatus()).isEqualTo(SeatStatus.MAINTENANCE);
    }

    @Test
//    @Rollback(value = false)
    void 없는ID로_시설_상태_제어_점검중() {
        // given: 존재하지 않는 랜덤한 PK 값
        Long invalidSeatId = 12345L;

        // when & then
        try {
            seatService.changeSeatToMaintenance(invalidSeatId);

            //위에서 에러가 안 터지고 여기까지 코드가 내려오면 테스트가 실패 해야함
            fail("예외가 발생해야 하는데 정상 처리되었습니다.");

        } catch (IllegalArgumentException e) {
            // 예상한 예외가 터져서 catch로 넘어왔다면, 메시지가 정확한지 검증
            Assertions.assertThat(e.getMessage()).contains("존재하지 않는 좌석입니다.");
        }
    }

    @Test
//    @Rollback(value = false)
    void 시설_상태_제어_이용가능() {
        // given: 1. 좌석을 등록하고, 2. 점검 중(MAINTENANCE) 상태로 미리 만듬
        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "A-01", 1, 1);
        seatService.changeSeatToMaintenance(seatId);

        // when: 점검 완료(원복) 서비스 로직 호출
        seatService.resolveSeatMaintenance(seatId);

        // then: DB에서 확인했을 때 상태가 다시 AVAILABLE로 돌아왔는지 검증
        Seat findSeat = seatRepository.findById(seatId).get();
        Assertions.assertThat(findSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void 없는ID로_시설_상태_제어_이용가능() {
        // given: 존재하지 않는 랜덤한 PK 값
        Long invalidSeatId = 9999L;

        // when & then: try-catch를 이용한 예외 검증
        try {
            seatService.resolveSeatMaintenance(invalidSeatId);

            // 에러가 발생하지 않고 여기까지 내려오면 테스트 실패 처리!
            fail("예외가 발생해야 하는데 정상 처리되었습니다.");

        } catch (IllegalArgumentException e) {
            // 예상한 예외가 잡혔다면 메시지 내용이 정확한지 확인
            Assertions.assertThat(e.getMessage()).contains("존재하지 않는 좌석입니다.");
        }
    }

    @Test
//    @Rollback(value = false)
    void 좌석예약_단건_스터디라운지() {
        // given: 회원 생성 및 스터디 라운지 좌석 생성
        Member member = Member.createMember("testUser", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);

        // when: 단건 예약 실행
        Long historyId = seatService.startUsingSeat(member.getId(), seatId);

        // then: 이용 기록(History) ID가 정상적으로 반환되고, 좌석 상태가 IN_USE으로 변경되어야 함
        Assertions.assertThat(historyId).isNotNull();

        Seat findSeat = seatRepository.findById(seatId).get();
        Assertions.assertThat(findSeat.getStatus()).isEqualTo(SeatStatus.IN_USE);
    }


    @Test
    void 다른좌석을_이용중_상태에서_새로운_좌석을_예약시_예외발생() {
        // given: 회원 생성
        Member member = Member.createMember("testUser", "1234", "권태욱",MemberRole.USER);
        memberRepository.save(member);

        // 좌석 2개 생성 (1번: 휴게실, 2번: 스터디 라운지)
        Long seatId1 = seatService.registerSeat("호천관", 7, SpaceType.REST_AREA, "R-01", 1, 1);
        Long seatId2 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 2, 1);

        // 회원이 이미 휴게실(1번 좌석)을 예약해서 사용 중인 상태로 만듦
        seatService.startUsingSeat(member.getId(), seatId1);

        // when & then: 이 상태에서 스터디 라운지(2번 좌석)를 또 예약하려고 시도함
        try {
            seatService.startUsingSeat(member.getId(), seatId2);

            //  여기까지 코드가 내려오면 차단 로직이 뚫린 것이므로 테스트 실패 처리
            fail("예약 차단 로직이 작동하지 않았습니다. 예외가 발생해야 합니다.");

        } catch (IllegalStateException e) {
            // 예상한 에러가 정확히 터졌다면, 메시지 내용 검증
            Assertions.assertThat(e.getMessage()).contains("이미 이용 중인 좌석이 있습니다");
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: UI에서 휴게실만 3개 선택해서 넘어왔을 때 완벽하게 예약되어야 한다")
    void 휴게실만_다건예약_성공() {

        // given: 회원 생성 , 좌석 생성
        Member member = Member.createMember("testUser1", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId1 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long seatId2 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-02", 1, 2);
        Long seatId3 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-03", 1, 3);

        // when: 휴게실(REST_AREA)만 3개 요청
        seatService.startUsingMultipleSeats(member.getId(), Arrays.asList(seatId1, seatId2, seatId3));

        // then: 모두 상태 변경 확인
        Assertions.assertThat(seatRepository.findById(seatId1).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
        Assertions.assertThat(seatRepository.findById(seatId2).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
        Assertions.assertThat(seatRepository.findById(seatId3).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: UI에서 취식공간만 2개 선택해서 넘어왔을 때 완벽하게 예약되어야 한다")
    void 취식공간만_다건예약_성공() {
        // given : 회원 생성 , 좌석 생성
        Member member = Member.createMember("testUser2", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId1 = seatService.registerSeat("호천관", 1, SpaceType.DINING_AREA, "E-01", 1, 1);
        Long seatId2 = seatService.registerSeat("호천관", 1, SpaceType.DINING_AREA, "E-02", 1, 2);

        // when: 취식공간(DINING_AREA)만 2개 요청
        seatService.startUsingMultipleSeats(member.getId(), Arrays.asList(seatId1, seatId2));

        // then
        Assertions.assertThat(seatRepository.findById(seatId1).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
        Assertions.assertThat(seatRepository.findById(seatId2).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
    }

    @Test
//    @DisplayName("실패: 누군가 API를 조작해 휴게실과 취식공간을 섞어서 보내면 백엔드가 차단해야 한다")
    void 서로다른_공간_혼합예약시_예외발생() {
        // given
        Member member = Member.createMember("testUser3", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long restAreaSeat = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long eatingAreaSeat = seatService.registerSeat("호천관", 1, SpaceType.DINING_AREA, "E-01", 2, 1);

        // when & then
        try {
            // 휴게실과 취식공간을 섞어서 요청!
            seatService.startUsingMultipleSeats(member.getId(), Arrays.asList(restAreaSeat, eatingAreaSeat));
            fail("서로 다른 장소 유형이 섞였는데 예약이 통과되었습니다. (보안 실패)");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("서로 다른 장소 유형");
        }
    }

    @Test
//    @DisplayName("실패: 단건/다건 상관없이 이미 이용 중인 좌석이 있다면 차단되어야 한다")
    void 이미_이용중인_좌석있으면_다건예약_실패() {
        // given
        Member member = Member.createMember("testUser4", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long usingSeat = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        Long newSeat1 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long newSeat2 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-02", 1, 2);

        // 회원이 이미 1자리를 쓰고 있음
        seatService.startUsingSeat(member.getId(), usingSeat);

        // when & then: 새로운 다건 예약을 시도함
        try {
            seatService.startUsingMultipleSeats(member.getId(), Arrays.asList(newSeat1, newSeat2));
            fail("기존 이용 좌석이 있는데 예약이 통과되었습니다.");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("이미 이용 중인 좌석이 있습니다");
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("범용 예약 테스트: 1개를 예약하든 3개를 예약하든 동일한 로직으로 성공해야 한다")
    void startUsingMultipleSeats_Integration_Test() {

        // given
        Member member = Member.createMember("testUser", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        // 테스트용 휴게실 좌석 4개 생성
        Long r1 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long r2 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-02", 1, 2);
        Long r3 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-03", 1, 3);
        Long r4 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-04", 1, 4);

        // 1. 단건 예약 시뮬레이션 (리스트에 1개만 담음)
        seatService.startUsingMultipleSeats(member.getId(), List.of(r1));
        Assertions.assertThat(seatRepository.findById(r1).get().getStatus()).isEqualTo(SeatStatus.IN_USE);

        // (테스트를 위해 상태 원복 및 히스토리 삭제 로직이 필요할 수 있음.
        // 여기서는 별도의 사용자로 다건 테스트 진행)
        Member member2 = Member.createMember("testUser2", "1234", "이름", MemberRole.USER);
        memberRepository.save(member2);

        // 2. 다건 예약 시뮬레이션 (리스트에 3개 담음)
        List<Long> multiSeats = List.of(r2, r3, r4);
        seatService.startUsingMultipleSeats(member2.getId(), multiSeats);

        // then
        Assertions.assertThat(seatRepository.findById(r2).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
        Assertions.assertThat(seatRepository.findById(r4).get().getStatus()).isEqualTo(SeatStatus.IN_USE);
    }
    @Test
//    @DisplayName("실패: 처음 예약하는 회원이라도 한 번에 5개 이상의 좌석을 다건 예약하려 하면 예외가 발생한다")
    void 다건예약_최대개수_초과시_예외발생() {
        // given: 회원 생성
        Member member = Member.createMember("testUser_Max", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        // 테스트용 휴게실 좌석 5개 생성
        Long s1 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long s2 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-02", 1, 2);
        Long s3 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-03", 1, 3);
        Long s4 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-04", 1, 4);
        Long s5 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-05", 1, 5);

        // 5개의 좌석 ID를 리스트에 담음
        List<Long> exceedSeatIds = Arrays.asList(s1, s2, s3, s4, s5);

        // when & then: 5개를 동시에 예약 요청!
        try {
            seatService.startUsingMultipleSeats(member.getId(), exceedSeatIds);

            // 여기까지 도달했다면 4개 제한 로직이 뚫린 것
            fail("최대 예약 가능 개수(4개) 방어 로직이 뚫렸습니다. 예외가 발생해야 합니다.");

        } catch (IllegalStateException e) {
            // "한 번에 최대 4개의 좌석까지만 예약 가능합니다." 예외 메시지 검증
            Assertions.assertThat(e.getMessage()).contains("최대 4개");
            System.out.println("✅ 5개 초과 예약 차단 성공: " + e.getMessage());
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 스터디 라운지 좌석을 정상적으로 반납하면 좌석과 기록 상태가 변경되어야 한다")
    void 좌석반납_정상성공() {
        // given: 회원 가입 및 좌석 등록 후 예약까지 진행
        Member member = Member.createMember("testUser1", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);

        // 회원이 좌석을 예약함 (상태: IN_USE / USING)
        Long history_id = seatService.startUsingSeat(member.getId(), seatId);

        // when: 좌석 반납 실행
        seatService.stopUsingSeat(member.getId(), seatId);

        // then: 좌석 상태는 AVAILABLE로, 기록은 COMPLETED로 변경되었는지 확인
        Seat findSeat = seatRepository.findById(seatId).get();
        Assertions.assertThat(findSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);

        UsageHistory findUsageHistory = usageHistoryRepository.findById(history_id).get();
        Assertions.assertThat(findUsageHistory.getStatus()).isEqualTo(UsageStatus.COMPLETED);

        // 반납 처리된 히스토리 상태 검증
        // (주의: findActiveHistoryByMemberAndSeat는 USING 상태만 찾으므로 전체 조회 후 검증 필요)
        long activeCount = usageHistoryRepository.countActiveUsageByMemberId(member.getId());
        Assertions.assertThat(activeCount).isEqualTo(0); // 현재 사용 중인 좌석이 0개가 되어야 함
    }

    @Test
//    @DisplayName("실패: 다른 사람이 예약한 좌석을 제3자가 반납하려 하면 차단되어야 한다")
    void 권한없는_좌석반납_예외발생() {
        // given: 두 명의 회원과 한 개의 좌석
        Member owner = Member.createMember("owner", "1234", "예약자", MemberRole.USER);
        Member hacker = Member.createMember("hacker", "1234", "해커", MemberRole.USER);
        memberRepository.save(owner);
        memberRepository.save(hacker);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);

        // owner가 좌석을 정상 예약함
        seatService.startUsingSeat(owner.getId(), seatId);

        // when & then: hacker가 owner의 좌석을 반납하려고 시도함
        try {
            seatService.stopUsingSeat(hacker.getId(), seatId);
            fail("본인이 예약하지 않은 좌석인데 반납이 통과되었습니다. (보안 뚫림)");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("현재 해당 좌석을 사용 중인 기록이 없습니다");
        }
    }

    @Test
//    @DisplayName("실패: 이미 반납했거나 아예 빌린 적 없는 좌석을 반납하려 하면 예외가 발생한다")
    void 빌린적없는_좌석반납_예외발생() {
        // given
        Member member = Member.createMember("testUser2", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long seatId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        // 예약 로직을 호출하지 않음 (빈 좌석 상태)

        // when & then
        try {
            seatService.stopUsingSeat(member.getId(), seatId);
            fail("빌리지도 않은 좌석인데 반납이 진행되었습니다.");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("현재 해당 좌석을 사용 중인 기록이 없습니다");
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 회원이 예약한 휴게실 좌석 3개를 한 번에 모두 정상 반납해야 한다")
    void 다건반납_정상성공() {

        // given: 회원 가입 및 좌석 생성
        Member member = Member.createMember("testUser1", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long s1 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long s2 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-02", 1, 2);
        Long s3 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-03", 1, 3);
        List<Long> seatIds = Arrays.asList(s1, s2, s3);

        // 먼저 다건 예약을 진행 (상태: IN_USE / USING)
        seatService.startUsingMultipleSeats(member.getId(), seatIds);

        // when: 다건 반납 실행
        seatService.stopUsingMultipleSeats(member.getId(), seatIds);

        // then: 3개 좌석 모두 AVAILABLE 상태로 돌아왔는지 검증
        Assertions.assertThat(seatRepository.findById(s1).get().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        Assertions.assertThat(seatRepository.findById(s2).get().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        Assertions.assertThat(seatRepository.findById(s3).get().getStatus()).isEqualTo(SeatStatus.AVAILABLE);

        // 회원이 현재 사용 중인 좌석 기록이 0개여야 함
        long activeCount = usageHistoryRepository.countActiveUsageByMemberId(member.getId());
        Assertions.assertThat(activeCount).isEqualTo(0);
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 다건 반납 API에 리스트로 1개만(단건) 보내도 정상 반납되어야 한다")
    void 다건반납API로_1개반납_성공() {

        //given
        Member member = Member.createMember("testUser2", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long s1 = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "E-01", 1, 1);

        //when
        // 1개 예약
        seatService.startUsingMultipleSeats(member.getId(), List.of(s1));

        // 1개 반납
        seatService.stopUsingMultipleSeats(member.getId(), List.of(s1));

        //then
        Assertions.assertThat(seatRepository.findById(s1).get().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
//    @DisplayName("실패: 다건 반납 리스트에 '스터디 라운지'가 포함되어 있으면 차단되어야 한다")
    void 다건반납시_스터디라운지_포함되면_예외발생() {

        //given
        Member member = Member.createMember("testUser3", "1234", "권태욱", MemberRole.USER);
        memberRepository.save(member);

        Long studyLoungeId = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);

        // 스터디 라운지는 단건 예약 API로 정상 예약함
        seatService.startUsingSeat(member.getId(), studyLoungeId);

        // when & then: 반납을 다건 API로 시도함!
        try {
            seatService.stopUsingMultipleSeats(member.getId(), List.of(studyLoungeId));
            fail("스터디 라운지 반납 차단 로직이 뚫렸습니다.");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("스터디 라운지는 단건 반납을 이용해주세요");
        }
    }

    @Test
//    @DisplayName("실패: 3개 중 1개라도 남의 자리이거나 빌린 적이 없다면 전체 롤백되어야 한다")
    void 남의자리_포함하여_다건반납시_예외발생() {

        //given
        Member owner = Member.createMember("owner", "1234", "정상유저", MemberRole.USER);
        Member hacker = Member.createMember("hacker", "1234", "해커", MemberRole.USER);
        memberRepository.save(owner);
        memberRepository.save(hacker);

        Long mySeat = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-01", 1, 1);
        Long otherSeat = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "R-02", 1, 2);

        // owner가 남의 자리를, hacker가 내 자리를 예약
        seatService.startUsingMultipleSeats(owner.getId(), List.of(otherSeat));
        seatService.startUsingMultipleSeats(hacker.getId(), List.of(mySeat));

        // when & then: hacker가 자기 자리(mySeat)와 남의 자리(otherSeat)를 묶어서 같이 반납 시도
        try {
            seatService.stopUsingMultipleSeats(hacker.getId(), Arrays.asList(mySeat, otherSeat));
            fail("남의 자리가 포함되었는데 반납이 진행되었습니다.");
        } catch (IllegalStateException e) {
            Assertions.assertThat(e.getMessage()).contains("현재 해당 좌석을 사용 중인 기록이 없습니다");
        }
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 관리자가 특정 이용자의 좌석을 강제 퇴실 처리한다.")
    void forceStopUsage_Success() {
        // given: 이용 중인 기록 생성
        Member member = Member.createMember("testId", "password", "김서일", MemberRole.USER);
        memberRepository.save(member);

        Seat seat = Seat.createSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "A-1", 1, 1);
        seatRepository.save(seat);

        seat.assignUser(); // 좌석 상태: USING
        UsageHistory history = UsageHistory.createHistory(member, seat);
        usageHistoryRepository.save(history);

        // when: 관리자 강제 퇴실 실행
        seatService.forceStopUsage(history.getId());

        // then: 좌석은 AVAILABLE, 기록은 COMPLETED 상태여야 함
        Seat updatedSeat = seatRepository.findById(seat.getId()).get();
        UsageHistory updatedHistory = usageHistoryRepository.findById(history.getId()).get();

        Assertions.assertThat(updatedSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        Assertions.assertThat(updatedHistory.getEndTime()).isNotNull();
    }

    @Test
//    @DisplayName("실패: 이미 종료된 기록을 강제 퇴실 처리하려 하면 예외가 발생한다.")
    void forceStopUsage_Fail_AlreadyEnded() {
        // given: 이미 종료된 기록 생성
        Member member = Member.createMember("testId", "password", "김서일", MemberRole.USER);
        memberRepository.save(member);

        Seat seat = Seat.createSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "A-1", 1, 1);
        seatRepository.save(seat);

        UsageHistory history = UsageHistory.createHistory(member, seat);
        history.completeUsage(); // 강제 종료 상태
        usageHistoryRepository.save(history);

        // when & then
        try {
            seatService.forceStopUsage(history.getId());

            // 만약 예외가 안 터지고 여기까지 코드가 내려오면 테스트를 강제로 실패
            Assertions.fail("예외가 발생해야 하는데, 강제 퇴실 로직이 정상 통과되었습니다.");

        } catch (IllegalStateException e) {
            // 예상한 예외가 터졌다면, 메시지가 정확한지 한 번 더 검증
            Assertions.assertThat(e.getMessage()).contains("이미 종료 처리된 이용 기록입니다.");
        }
    }

    @Test
//    @DisplayName("실패: 존재하지 않는 이용 기록 ID로 강제 퇴실 시도 시 예외가 발생한다.")
    void forceStopUsage_Fail_NotFound() {
        // given: 없는 ID (999L)
        Long invalidHistoryId = 999L;

        // when & then
        try {
            // 이 로직에서 예외가 터져야 정상
            seatService.forceStopUsage(invalidHistoryId);

            // 예외가 안 터졌다면 테스트 실패
            Assertions.fail("예외가 발생해야 하는데, 강제 퇴실 로직이 정상 통과되었습니다.");

        } catch (IllegalArgumentException e) {
            // 발생한 예외의 메시지 검증
            Assertions.assertThat(e.getMessage()).contains("존재하지 않는 이용 기록입니다.");
        }
    }


}