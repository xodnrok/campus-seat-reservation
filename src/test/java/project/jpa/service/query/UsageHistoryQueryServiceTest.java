package project.jpa.service.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.dto.usagehistoryapidto.UsageHistoryDto;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.service.SeatService;
import project.jpa.service.UsageHistoryService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UsageHistoryQueryServiceTest {


    @Autowired
    UsageHistoryService usageHistoryService;

    @Autowired
    UsageHistoryQueryService usageHistoryQueryService;

    @Autowired
    SeatService seatService;

    @Autowired
    MemberRepository memberRepository;

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 내 이용 기록을 페이징 조회하면 타인의 기록은 섞이지 않고, 내 기록만 최신순으로 정렬된 DTO 페이지가 반환되어야 한다")
    void 내_이용기록_조회_성공() throws InterruptedException {

        // [1. Given] 회원 2명(나, 타인)과 좌석 3개 세팅
        Member me = Member.createMember("historyUser", "1234", "권태욱", MemberRole.USER);
        Member other = Member.createMember("otherUser", "1234", "타인", MemberRole.USER);
        memberRepository.save(me);
        memberRepository.save(other);

        Long seatId1 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        Long seatId2 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-02", 1, 2);
        Long seatId3 = seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-03", 1, 3);

        // [2. Given - 나의 오전 일정] S-01 좌석 사용 후 반납 (COMPLETED 상태)
        seatService.startUsingSeat(me.getId(), seatId1);
        seatService.stopUsingSeat(me.getId(), seatId1); // 반납해야 다음 좌석 예약 가능!

        Thread.sleep(10); // 시간차 발생 (오전 -> 오후)

        // [2. Given - 타인의 일정] S-02 좌석 사용 중 (내 목록에 나오면 안 됨)
        seatService.startUsingSeat(other.getId(), seatId2);

        Thread.sleep(10); // 시간차 발생

        // [2. Given - 나의 오후 일정] S-03 좌석 새로 사용 시작 (USING 상태)
        seatService.startUsingSeat(me.getId(), seatId3);

        // 페이징 요청 객체 생성 (0페이지, 10개씩)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // [3. When] 쿼리 서비스 호출 -> Page<UsageHistoryDto> 반환
        Page<UsageHistoryDto> myHistories = usageHistoryQueryService.findMyUsageHistories(me.getId(), pageRequest);

        // [4. Then] 검증
        // 1) 격리 검증: 타인의 기록은 빠지고 내 기록 2건만 조회되어야 함
        Assertions.assertThat(myHistories.getTotalElements()).isEqualTo(2); // 전체 개수 2개 확인

        // 2) 정렬 검증: 가장 나중에 앉은(최신) S-03이 리스트의 첫 번째(index 0)로 와야 함 (DTO 활용)
        Assertions.assertThat(myHistories.getContent().get(0).getSeatNumber()).isEqualTo("S-03");
        Assertions.assertThat(myHistories.getContent().get(1).getSeatNumber()).isEqualTo("S-01");

        // 3) 상태 및 시간 검증: 첫 번째(현재 사용중)는 USING, 두 번째(반납완료)는 COMPLETED인지 확인
        Assertions.assertThat(myHistories.getContent().get(0).getEndTime()).isNull(); // 아직 안 끝났으니 종료시간이 없음
        Assertions.assertThat(myHistories.getContent().get(1).getEndTime()).isNotNull(); // 1번은 종료 시간이 찍혀있어야 함
    }

    @Test
//    @DisplayName("성공: 이용 기록이 하나도 없는 신입생이 페이징 조회하면 빈 페이지 객체를 반환해야 한다")
    void 이용기록_없는_회원_조회_성공() {

        // [1. Given] 아직 한 번도 좌석을 이용하지 않은 회원
        Member newMember = Member.createMember("newbie", "1234", "신입생", MemberRole.USER);
        memberRepository.save(newMember);

        //  페이징 요청 객체 생성 (0페이지, 10개씩)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // [2. When] 쿼리 서비스 호출
        Page<UsageHistoryDto> myHistories = usageHistoryQueryService.findMyUsageHistories(newMember.getId(), pageRequest);

        // [3. Then] 에러가 터지지 않고 안전하게 빈 페이지가 나와야 함
        Assertions.assertThat(myHistories).isNotNull();
        Assertions.assertThat(myHistories.isEmpty()).isTrue(); // 페이지 컨텐츠가 비어있음
        Assertions.assertThat(myHistories.getTotalElements()).isEqualTo(0); // 총 개수가 0개임
    }
}