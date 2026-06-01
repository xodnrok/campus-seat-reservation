package project.jpa.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.domain.SeatBookmark;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatBookmarkRepository;
import project.jpa.repository.SeatRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SeatBookmarkServiceTest {

    @Autowired
    SeatBookmarkService seatBookmarkService;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SeatBookmarkRepository seatBookmarkRepository;


    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 기존에 즐겨찾기가 없으면 새로 등록되고 true를 반환해야 한다")
    void 즐겨찾기_토글_등록_성공() {

        // given: 회원과 좌석을 생성하여 DB에 저장 (기존 시스템의 생성 방식 적용)
        Member member = Member.createMember("toggleTest1", "1234", "학생1", MemberRole.USER);
        memberRepository.save(member);

        Seat seat = Seat.createSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-01", 1, 1);
        seatRepository.save(seat);

        // when: 처음으로 즐겨찾기 버튼을 클릭 (토글)
        boolean isCreated = seatBookmarkService.toggleBookmark(member.getId(), seat.getId());

        // then: true가 반환되어야 하고, 실제 DB에 1건이 저장되어 있어야 한다.
        Assertions.assertThat(isCreated).isTrue();
        Assertions.assertThat(seatBookmarkRepository.findAll().size()).isEqualTo(1);
    }

    @Test
//    @Rollback(value = false)
//    @DisplayName("성공: 이미 즐겨찾기가 되어 있는 상태에서 누르면 해제(삭제)되고 false를 반환해야 한다")
    void 즐겨찾기_토글_해제_성공() {

        // given: 회원, 좌석 세팅 후 1차로 즐겨찾기를 등록해둔 상태
        Member member = Member.createMember("toggleTest2", "1234", "학생2", MemberRole.USER);
        memberRepository.save(member);

        Seat seat = Seat.createSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-02", 1, 2);
        seatRepository.save(seat);

        // 1차 클릭 -> 즐겨찾기 등록
        seatBookmarkService.toggleBookmark(member.getId(), seat.getId());

        // when: 두 번째로 즐겨찾기 버튼을 클릭 (토글 해제)
        boolean isCreated = seatBookmarkService.toggleBookmark(member.getId(), seat.getId());

        // then: false가 반환되어야 하고, 실제 DB에서 데이터가 지워져서(size=0) 없어야 한다.
        Assertions.assertThat(isCreated).isFalse();
        Assertions.assertThat(seatBookmarkRepository.findAll().isEmpty()).isTrue();
    }

    @Test
//    @DisplayName("실패: DB에 존재하지 않는 회원 ID로 요청하면 IllegalArgumentException 예외가 터져야 한다")
    void 존재하지않는_회원_즐겨찾기_예외발생() {

        // given: 정상적인 좌석은 있지만, 회원은 DB에 없는 가짜 ID 사용
        Seat seat = Seat.createSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-03", 1, 3);
        seatRepository.save(seat);

        Long fakeMemberId = 99999L; // 존재할 수 없는 ID

        // when & then
        try {
            seatBookmarkService.toggleBookmark(fakeMemberId, seat.getId());
            fail("존재하지 않는 회원인데 예외가 발생하지 않았습니다.");
        } catch (IllegalArgumentException e) {
            Assertions.assertThat(e.getMessage()).contains("존재하지 않는 회원입니다.");
        }
    }

    @Test
//    @DisplayName("실패: DB에 존재하지 않는 좌석 ID로 요청하면 IllegalArgumentException 예외가 터져야 한다")
    void 존재하지않는_좌석_즐겨찾기_예외발생() {
        // given: 정상적인 회원은 있지만, 좌석은 DB에 없는 가짜 ID 사용
        Member member = Member.createMember("toggleTest3", "1234", "학생3", MemberRole.USER);
        memberRepository.save(member);

        Long fakeSeatId = 99999L; // 존재할 수 없는 ID

        // when & then
        try {
            seatBookmarkService.toggleBookmark(member.getId(), fakeSeatId);
            fail("존재하지 않는 좌석인데 예외가 발생하지 않았습니다.");
        } catch (IllegalArgumentException e) {
            Assertions.assertThat(e.getMessage()).contains("존재하지 않는 좌석입니다.");
        }
    }

}