package project.jpa.service.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.dto.memberapidto.LoginResponse;
import project.jpa.enums.MemberRole;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.ReportRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.service.MemberService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberQueryServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    SeatRepository seatRepository;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    MemberQueryService memberQueryService;

    @Test
    void 로그인_성공() {

        // given
        // 로그인할 회원을 미리 가입
        Member member = Member.createMember("testLoginId", "1234", "로그인유저", MemberRole.USER);
        memberService.join(member);

        // when
        // login() -> loginCheck() 로 변경하고, 반환값을 LoginResponse DTO로 받기
        LoginResponse loginResponse = memberQueryService.loginCheck("testLoginId", "1234");

        // then
        // 엔티티 대신 DTO 상자에 값이 잘 담겨왔는지 검증
        Assertions.assertThat(loginResponse).isNotNull();
        Assertions.assertThat(loginResponse.getName()).isEqualTo("로그인유저");
        Assertions.assertThat(loginResponse.getRole()).isEqualTo(MemberRole.USER);
    }

    @Test
    void 로그인_실패_없는아이디() {

        // given (준비)
        Member member = Member.createMember("testLoginId", "1234", "로그인유저", MemberRole.USER);
        memberService.join(member);

        // when & then
        try {
            // 💡 DB에 없는 엉뚱한 아이디를 넣고 쿼리 서비스에 로그인을 시도
            memberQueryService.loginCheck("wrongId", "1234");
        } catch (IllegalArgumentException e) {
            System.out.println("없는 아이디 방어 로직 동작 성공: " + e.getMessage());
            return;
        }

        // 에러가 안 터지면 테스트 실패
        fail("에러 발생 안함 테스트 실패");
    }

    @Test
    void 로그인_실패_틀린비밀번호() {

        // given
        Member member = Member.createMember("testLoginId", "1234", "로그인유저", MemberRole.USER);
        memberService.join(member);

        // when & then
        try {
            //  아이디는 맞는데, 비밀번호를 틀리게 입력하여 쿼리 서비스에 요청
            memberQueryService.loginCheck("testLoginId", "9999");
        } catch (IllegalArgumentException e) {
            System.out.println("틀린 비밀번호 방어 로직 동작 성공: " + e.getMessage());
            return;
        }

        // 에러가 안 터지면 테스트 실패
        fail("에러 발생 안함 테스트 실패");
    }
}