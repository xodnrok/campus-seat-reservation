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
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.ReportRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.service.query.MemberQueryService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ReportRepository reportRepository;

    @Test
//    @Rollback(value = false)
    public void 회원_가입_검증() {

        //given
        // 새로운 회원을 만들기 위한 데이터를 세팅
        Member member = Member.createMember("testUser", "1234", "테스터", MemberRole.USER);

        //when
        // 서비스의 회원가입 기능을 실제로 호출
        Long saveId = memberService.join(member);

        //then
        // 방금 가입한 회원의 ID로 DB에서 다시 조회해 온 뒤, 원래 데이터와 똑같은지 비교
        Member findMember = memberRepository.findById(saveId).get();

        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId()); //PK
        Assertions.assertThat(findMember.getLoginId()).isEqualTo("testUser"); //로그인 아이디
        Assertions.assertThat(findMember.getPassword()).isEqualTo(member.getPassword()); //패스워드
        Assertions.assertThat(findMember.getName()).isEqualTo(member.getName()); //이름
        Assertions.assertThat(findMember.getRole()).isEqualTo(MemberRole.USER); //권한

    }

    @Test
//    @Rollback(value = false)
    void 중복_아이디_검증() {

        // given (준비)
        // 아이디가 똑같은 두 명의 회원을 준비
        Member member1 = Member.createMember("duplicateId", "1234", "유저1", MemberRole.USER);
        Member member2 = Member.createMember("duplicateId", "5678", "유저2", MemberRole.USER);

        // when
        memberService.join(member1);

        //then

        try {
            memberService.join(member2);
        } catch (IllegalStateException e) {
            System.out.println("중복 아이디 검증 성공");
            return;
        }

        fail("에러 발생 안함 테스트 실패");
    }

    @Test
//    @Rollback(value = false)
    void 회원_삭제_검증() {

        // given (준비)
        // 탈퇴시킬 회원을 하나 만들고 가입
        Member member = Member.createMember("deleteUser", "1234", "탈퇴할유저", MemberRole.USER);
        Long savedId = memberService.join(member);

        //when
        // 서비스의 탈퇴 기능을 호출해서 DB에서 삭제
        memberService.deleteMember(savedId);

        //then
        //삭제된 회원을 DB에서 다시 조회 이후 PK로 조회시 비어있는지 검증
        Optional<Member> deletedMember = memberRepository.findById(savedId);
        Assertions.assertThat(deletedMember).isEmpty();
    }

    @Test
//    @Rollback(value = false)
    void 회원_삭제_PK값틀림() {

        //given
        //가짜 PK값 생성
        Long fakeMemberId = 99999L;

        // when & then (실행 및 검증)
        try {
            memberService.deleteMember(fakeMemberId);
        } catch (IllegalStateException e) {
            System.out.println("회원 삭제 가짜 PK검증 로직 동작 성공");
            return;
        }

        fail("에러 발생 안함 테스트 실패");
    }

    @Test
//    @Rollback(value = false)
    void 회원_탈퇴_외래키_우선삭제() {
        // 1. 회원과 좌석을 생성하고 저장합니다.
        Member member = memberRepository.save(Member.createMember("1234","5678","test",MemberRole.USER));
        Seat seat = seatRepository.save(Seat.createSeat("호천관", 7, SpaceType.REST_AREA,
                "1번", 1, 2));

        // 2. 회원이 좌석을 고장 신고  (자식 데이터 생성)
        Report report = Report.createReport(member, seat, "콘센트 고장");
        reportRepository.save(report);

        // 3. 회원을 강제로 삭제
        //  여기서 DataIntegrityViolationException FK 에러 발생
        memberService.deleteMember(member.getId());

        //  JPA에게  DB에 SQL을 쏘라고 강제 명령
        memberRepository.flush();
    }

    @Test
//    @Rollback(value = false)
    void 회원_정보_수정_성공() {

        // given
        // 기존 회원을 하나 가입
        Member member = Member.createMember("updateUser", "1234", "기존이름", MemberRole.USER);
        Long savedId = memberService.join(member);

        // when
        // 새로운 비밀번호와 이름으로 변경을 요청
        memberService.updateMember(savedId, "5678", "변경된이름");

        // then
        // DB에서 회원을 다시 꺼내와서, 값이 진짜로 바뀌었는지 확인
        Member updatedMember = memberRepository.findById(savedId).get();

        //변경된게 맞는지 확인
        Assertions.assertThat(updatedMember.getPassword()).isEqualTo("5678");
        Assertions.assertThat(updatedMember.getName()).isEqualTo("변경된이름");
    }

    @Test
//    @Rollback(value = false)
    void 회원_정보_수정_가짜PK() {

        // given
        //가짜 PK값 생성
        Long fakeMemberId = 99999L;

        // when & then (실행 및 검증)
        try {
            // 없는 회원인데 억지로 수정을 시도
            memberService.updateMember(fakeMemberId, "5678", "변경된이름");
        } catch (IllegalStateException e) {
            System.out.println("회원 수정 가짜 PK 검증 로직 동작 성공");
            return;
        }

        fail("에러 발생 안함 테스트 실패");
    }

}