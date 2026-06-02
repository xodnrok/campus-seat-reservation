package project.jpa.service.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.dto.seatapidto.SeatDto;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.dto.usagehistoryapidto.ActiveUserDto;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;
import project.jpa.service.SeatService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test") // 💡 추가: 이 클래스는 'test' 환경에서 실행됨
@SpringBootTest
@Transactional
class SeatQueryServiceTest {

    @Autowired
    SeatService seatService;

    @Autowired
    SeatQueryService seatQueryService;

    @Autowired
    MemberRepository memberRepository;


    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 조건이 하나도 없으면(null) 모든 좌석이 조회되어야 한다")
    void 검색조건없음_전체조회_성공() {

        // given: 다양한 좌석 3개 생성 (명령: SeatService 사용)
        seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        seatService.registerSeat("호천관", 7, SpaceType.REST_AREA, "R-01", 1, 2);
        seatService.registerSeat("정보관", 1, SpaceType.DINING_AREA, "E-01", 1, 1);

        // when: 아무 조건도 세팅하지 않은 빈 condition 객체 전달
        SeatSearchCondition condition = new SeatSearchCondition();

        // 조회 로직은 SeatQueryService 호출
        List<SeatDto> result = seatQueryService.searchSeats(condition);

        // then: 3개가 모두 나와야 함
        Assertions.assertThat(result.size()).isEqualTo(3);
    }

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 조건이 1개일 때 (건물 이름) 해당 데이터만 필터링되어야 한다")
    void 단일조건_검색_성공() {

        // given
        seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        seatService.registerSeat("호천관", 7, SpaceType.REST_AREA, "R-01", 1, 2);
        seatService.registerSeat("정보관", 1, SpaceType.DINING_AREA, "E-01", 1, 1);

        // when: "정보관"만 검색 조건으로 설정
        SeatSearchCondition condition = new SeatSearchCondition();
        condition.setBuildingName("정보관");

        // 조회 로직은 SeatQueryService 호출
        List<SeatDto> result = seatQueryService.searchSeats(condition);

        // then: 정보관 좌석 1개만 나와야 함
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getBuildingName()).isEqualTo("정보관");
    }

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 조건이 다중일 때 (건물, 층수, 장소유형) 모두 만족하는 데이터만 나와야 한다")
    void 다중조건_검색_성공() {

        // given
        // 1. 호천관 7층 스터디 라운지 (찾고자 하는 타겟)
        seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        // 2. 호천관 7층 휴게실 (장소 유형이 다름)
        seatService.registerSeat("호천관", 7, SpaceType.REST_AREA, "R-01", 1, 2);
        // 3. 호천관 1층 스터디 라운지 (층수가 다름)
        seatService.registerSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "S-02", 1, 3);

        // when: 호천관 + 7층 + 스터디라운지 3단  검색
        SeatSearchCondition condition = new SeatSearchCondition();
        condition.setBuildingName("호천관");
        condition.setFloor(7);
        condition.setSpaceType(SpaceType.STUDY_LOUNGE);

        //  조회 로직은 SeatQueryService 호출
        List<SeatDto> result = seatQueryService.searchSeats(condition);

        // then: 정확히 1번 좌석 딱 1개만 걸러져서 나와야 함
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getBuildingName()).isEqualTo("호천관");
        Assertions.assertThat(result.get(0).getFloor()).isEqualTo(7);
        Assertions.assertThat(result.get(0).getSpaceType()).isEqualTo(SpaceType.STUDY_LOUNGE);
    }

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 조건에 맞는 데이터가 없으면 빈 리스트를 반환해야 한다")
    void 결과없음_빈리스트_반환() {

        // given
        seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);

        // when: 존재하지 않는 "우촌관" 검색
        SeatSearchCondition condition = new SeatSearchCondition();
        condition.setBuildingName("우촌관");

        // 조회 로직은 SeatQueryService 호출
        List<SeatDto> result = seatQueryService.searchSeats(condition);

        // then: 에러가 터지지 않고 안전하게 사이즈가 0인 리스트가 반환되어야 함
        Assertions.assertThat(result).isEmpty();
    }

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 건물과 층수가 모두 달라도, '스터디 라운지' 조건 하나만으로 캠퍼스 전체의 스터디 라운지가 검색되어야 한다")
    void 장소유형_스터디라운지_통합검색_성공() {

        // given: 캠퍼스 곳곳에 다양한 좌석 생성
        seatService.registerSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        seatService.registerSeat("세종관", 3, SpaceType.STUDY_LOUNGE, "S-02", 1, 1);
        seatService.registerSeat("배양관", 1, SpaceType.STUDY_LOUNGE, "S-03", 1, 1);
        // (함정 데이터) 배양관 휴게실
        seatService.registerSeat("배양관", 1, SpaceType.REST_AREA, "R-01", 1, 2);

        // when: 오직 '스터디 라운지'만 조건으로 세팅
        SeatSearchCondition condition = new SeatSearchCondition();
        condition.setSpaceType(SpaceType.STUDY_LOUNGE);

        //  조회 로직은 SeatQueryService 호출!
        List<SeatDto> result = seatQueryService.searchSeats(condition);

        // then: 함정 데이터를 제외하고, 정확히 3개의 스터디 라운지만 검색되어야 함
        Assertions.assertThat(result.size()).isEqualTo(3);

        // 검색된 3개의 결과가 모두 'STUDY_LOUNGE'인지 확실하게 검증
        boolean allStudyLounges = result.stream()
                .allMatch(seat -> seat.getSpaceType() == SpaceType.STUDY_LOUNGE);
        Assertions.assertThat(allStudyLounges).isTrue();
    }

    @Test
//    @DisplayName("성공: 검색 조건이 없으면 현재 자리를 이용 중인 모든 학생이 페이징되어 반환된다.")
    void 실시간모니터링_조건없음_전체조회_성공() {
        // given
        // 수정할 방식 (정적 팩토리 메서드 사용)
        Member member1 = Member.createMember("20230001", "password123", "김서일", MemberRole.USER);
        Member member2 = Member.createMember("20230002", "password123", "이배양", MemberRole.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long seat1Id = seatService.registerSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "A-1", 1, 1);
        Long seat2Id = seatService.registerSeat("정보관", 2, SpaceType.REST_AREA, "B-1", 1, 1);

        // 학생 2명이 각각 자리를 예약하여 '사용 중(USING)' 상태로 만듦
        seatService.startUsingSeat(member1.getId(), seat1Id);
        seatService.startUsingSeat(member2.getId(), seat2Id);

        // when
        SeatSearchCondition condition = new SeatSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ActiveUserDto> result = seatQueryService.getActiveUsers(condition, pageRequest);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(2); // 총 2명이 검색되어야 함
        Assertions.assertThat(result.getContent().size()).isEqualTo(2);
    }

    @Test
//    @DisplayName("성공: 건물명으로 필터링 시, 해당 건물에서 이용 중인 학생만 정확히 조회된다.")
    void 실시간모니터링_건물명_단일조건_검색_성공() {
        // given
        Member member1 = Member.createMember("20230001", "password123", "김서일", MemberRole.USER);
        Member member2 = Member.createMember("20230002", "password123", "이배양", MemberRole.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long seat1Id = seatService.registerSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "A-1", 1, 1);
        Long seat2Id = seatService.registerSeat("정보관", 2, SpaceType.REST_AREA, "B-1", 1, 1);

        seatService.startUsingSeat(member1.getId(), seat1Id);
        seatService.startUsingSeat(member2.getId(), seat2Id);

        // when: '호천관'만 필터링
        SeatSearchCondition condition = new SeatSearchCondition();
        condition.setBuildingName("호천관");
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ActiveUserDto> result = seatQueryService.getActiveUsers(condition, pageRequest);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent().get(0).getBuildingName()).isEqualTo("호천관");
        Assertions.assertThat(result.getContent().get(0).getMemberName()).isEqualTo("김서일");
    }

    @Test
//    @DisplayName("성공: 건물, 층수, 공간 유형 다중 조건으로 필터링 시, 모두 일치하는 학생만 조회된다.")
    void 실시간모니터링_복합조건_검색_성공() {
        // given
        Member member1 = Member.createMember("20230001", "password123", "김서일", MemberRole.USER);
        Member member2 = Member.createMember("20230002", "password123", "이배양", MemberRole.USER);
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long seat1Id = seatService.registerSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "A-1", 1, 1);
        Long seat2Id = seatService.registerSeat("호천관", 1, SpaceType.REST_AREA, "A-2", 1, 2); // 층수/건물은 같지만 휴게실

        seatService.startUsingSeat(member1.getId(), seat1Id);
        seatService.startUsingSeat(member2.getId(), seat2Id);

        // when: 호천관 + 1층 + 스터디 라운지 필터링
        SeatSearchCondition condition = new SeatSearchCondition();
        condition.setBuildingName("호천관");
        condition.setFloor(1);
        condition.setSpaceType(SpaceType.STUDY_LOUNGE);
        PageRequest pageRequest = PageRequest.of(0, 10);

        Page<ActiveUserDto> result = seatQueryService.getActiveUsers(condition, pageRequest);

        // then
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent().get(0).getSpaceType()).isEqualTo(SpaceType.STUDY_LOUNGE);
        Assertions.assertThat(result.getContent().get(0).getMemberName()).isEqualTo("김서일");
    }
}