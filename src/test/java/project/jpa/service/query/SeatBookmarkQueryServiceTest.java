package project.jpa.service.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.dto.bookmarkdto.BookmarkDto;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.service.SeatBookmarkService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SeatBookmarkQueryServiceTest {

    @Autowired
    SeatBookmarkService seatBookmarkService;

    @Autowired
    SeatBookmarkQueryService seatBookmarkQueryService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SeatRepository seatRepository;

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 내 즐겨찾기 목록을 페이징 조회하면 내가 등록한 좌석 DTO만 최신순으로 반환되어야 한다")
    void 내_즐겨찾기_목록_조회_성공() throws InterruptedException {

        // [1. Given] 두 명의 회원과 여러 개의 좌석 생성
        Member me = Member.createMember("me", "1234", "내이름", MemberRole.USER);
        Member other = Member.createMember("other", "1234", "타인", MemberRole.USER);
        memberRepository.save(me);
        memberRepository.save(other);

        Seat seat1 = Seat.createSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-10", 1, 10);
        Seat seat2 = Seat.createSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-11", 1, 11);
        Seat seat3 = Seat.createSeat("호천관", 7, SpaceType.STUDY_LOUNGE, "S-12", 1, 12);
        seatRepository.save(seat1);
        seatRepository.save(seat2);
        seatRepository.save(seat3);

        // [2. Given] 내 계정으로 좌석 1, 2를 순서대로 즐겨찾기 등록
        seatBookmarkService.toggleBookmark(me.getId(), seat1.getId());

        // BaseTimeEntity의 createdAt 차이를 두기 위해 아주 짧은 시간 대기
        Thread.sleep(10);

        seatBookmarkService.toggleBookmark(me.getId(), seat2.getId());

        // [2. Given] 타인 계정으로 좌석 3을 즐겨찾기 등록 (내 목록에 나오면 안 됨)
        seatBookmarkService.toggleBookmark(other.getId(), seat3.getId());

        // 💡 페이징 요청 객체 생성 (0페이지, 10개씩)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // [3. When] 내 즐겨찾기 목록 조회 실행 (쿼리 서비스 호출 -> Page<BookmarkDto> 반환)
        Page<BookmarkDto> myBookmarks = seatBookmarkQueryService.findMyBookmarks(me.getId(), pageRequest);

        // [4. Then] 검증 시작
        // 1. 개수 검증: 총 3건 중 내 것인 2건만 나와야 함 (getTotalElements 활용)
        Assertions.assertThat(myBookmarks.getTotalElements()).isEqualTo(2);

        // 2. 격리 검증: 타인의 즐겨찾기(S-12)는 포함되지 않아야 함
        boolean hasOtherBookmark = myBookmarks.getContent().stream()
                .anyMatch(b -> b.getSeatNumber().equals("S-12")); // 💡 DTO이므로 바로 getSeatNumber() 호출
        Assertions.assertThat(hasOtherBookmark).isFalse();

        // 3. 정렬 검증: 💡 [핵심] 최신순(createdAt DESC)이므로 나중에 등록한 S-11이 맨 위(index 0)에 와야 함
        Assertions.assertThat(myBookmarks.getContent().get(0).getSeatNumber()).isEqualTo("S-11");
        Assertions.assertThat(myBookmarks.getContent().get(1).getSeatNumber()).isEqualTo("S-10");

        // 4. DTO 매핑 검증: 좌석 정보(건물명 등)가 DTO에 잘 담겨왔는지 확인
        Assertions.assertThat(myBookmarks.getContent().get(0).getBuildingName()).isEqualTo("호천관");
    }

    @Test
//  @Rollback(value = false)
//    @DisplayName("성공: 즐겨찾기 내역이 하나도 없는 회원이 페이징 조회하면 빈 페이지 객체를 반환해야 한다")
    void 즐겨찾기_없는_회원_조회_성공() {

        // [1. Given] 즐겨찾기를 하지 않은 신규 회원 생성
        Member newMember = Member.createMember("newbie", "1234", "신입생", MemberRole.USER);
        memberRepository.save(newMember);

        // 💡 페이징 요청 객체 생성 (0페이지, 10개씩)
        PageRequest pageRequest = PageRequest.of(0, 10);

        // [2. When] 목록 조회 (쿼리 서비스 호출)
        Page<BookmarkDto> result = seatBookmarkQueryService.findMyBookmarks(newMember.getId(), pageRequest);

        // [3. Then] Null이 아닌 비어있는 Page 객체가 반환되어야 함
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.isEmpty()).isTrue(); // 페이지 내 컨텐츠가 비어있는지 검증
        Assertions.assertThat(result.getTotalElements()).isEqualTo(0); // 전체 개수가 0개인지 검증
    }
}