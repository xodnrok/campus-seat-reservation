package project.jpa.ApiController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import project.jpa.domain.Member;
import project.jpa.dto.memberapidto.*;
import project.jpa.enums.MemberRole;
import project.jpa.service.MemberService;
import project.jpa.service.query.MemberQueryService;

@Tag(name = "회원 관리 API", description = "회원가입 , 로그인 , 로그아웃 , 정보 수정 , 회원 탈퇴 등 사용자 관련 API")
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;
    private final MemberQueryService memberQueryService;

    // 세션에 로그인 회원의 PK를 저장할 때 쓸 키 값
    public static final String LOGIN_MEMBER = "loginMember";

    /**
     * FR#1. 회원 가입 API
     */
    @Operation(summary = "회원가입", description = "아이디 , 비밀번호 , 이름을 입력받아 회원을 생성합니다.")
    @PostMapping("/api/members/join")
    public MemberApiResponse<Long> join(@RequestBody @Valid JoinRequest request) {

        // 프론트엔드에서 넘어온 데이터를 바탕으로 Member 엔티티 생성
        Member member = Member.createMember(
                request.getLoginId(),
                request.getPassword(),
                request.getName(),
                MemberRole.USER
        );

        // 서비스 로직 호출 (중복 아이디면 여기서 에러가 터짐)
        Long savedId = memberService.join(member);

        //  성공 시 깔끔하게 직접 DTO 반환
        return MemberApiResponse.success("회원가입이 완료되었습니다.", savedId);
    }

    /**
     * FR#4. 로그인 API
     */
    @Operation(summary = "로그인", description = "아이디와 비밀번호를 입력받아 세션을 생성합니다.")
    @PostMapping("/api/members/login")                              //스프링이 지원하는 HttpSession을 파라미터로 직접 주입
    public MemberApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpSession session) {

        // 1. 서비스 로직을 통해 아이디/비밀번호 검증
        LoginResponse responseData = memberQueryService.loginCheck(request.getLoginId(), request.getPassword());

        // 2. 세션에 PK 하나만 넣는 것이 아니라, 이름과 권한이 담긴 객체를 딱 하나만 저장
        SessionMember sessionMember = new SessionMember(responseData.getMemberId(), responseData.getName(), responseData.getRole());
        session.setAttribute(LOGIN_MEMBER, sessionMember);

        return MemberApiResponse.success(responseData.getName() + "님 환영합니다.", responseData);
    }

    /**
     * FR#5. 로그아웃 API
     */
    @Operation(summary = "로그아웃", description = "세선(쿠키)값을 제거해서 로그아웃 시킵니다.")
    @PostMapping("/api/members/logout")
    public MemberApiResponse<String> logout(HttpSession session) {

        if (session != null) {
            session.invalidate(); // 세션 날리기 (로그아웃 처리)
        }

        return MemberApiResponse.success("로그아웃 되었습니다.", null);
    }

    /**
     * FR#3. 회원 정보 수정 API
     */
    @Operation(summary = "회원 정보 수정", description = "이름 , 비밀번호를 입력받아 회원을 수정합니다.")
    @PutMapping("/api/members")
    public MemberApiResponse<String> updateMember(@RequestBody @Valid UpdateRequest request,
                                                  @SessionAttribute(name = LOGIN_MEMBER) SessionMember loginMember,
                                                  HttpSession session) {

        Long memberId = loginMember.getId();

        //1. 회원PK , 비밀번호 , 이름 수정된 정보를 넘긴다.
        memberService.updateMember(memberId, request.getPassword(), request.getName());

        //2. 수정이 되었으므로 수정된 이름으로 세션값을 수정한다.
        SessionMember updatedSessionMember = new SessionMember(memberId, request.getName(), loginMember.getRole());
        session.setAttribute(LOGIN_MEMBER, updatedSessionMember);

        return MemberApiResponse.success("정보가 성공적으로 수정되었습니다.", null);
    }


    /**
     * FR#2. 회원 탈퇴 API
     */
    @Operation(summary = "회원탈퇴", description = "현재 로그인된 사용자의 계정을 시스템에서 영구 삭제")
    @DeleteMapping("/api/members")
    public MemberApiResponse<String> deleteMember(@SessionAttribute(name = LOGIN_MEMBER) SessionMember loginMember,
                                                  HttpSession session) {

        // 1. 서비스 호출하여 DB에서 데이터 완벽 삭제 (외래키 포함)
        memberService.deleteMember(loginMember.getId());

        // 2. 데이터 삭제 후 세션도 함께 만료 처리
        session.invalidate();

        return MemberApiResponse.success("탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.", null);
    }

    /**
     * 내 정보 단일 조회 API (DB에서 최신 데이터 조회로 최적화)
     */
    @GetMapping("/api/members/me")
    public MemberApiResponse<LoginResponse> getMyInfo(@SessionAttribute(name = LOGIN_MEMBER) SessionMember loginMember) {

        // 1. 현재 로그인된 회원의 최신 상태를 DB에서 안전하게 가져와 프론트에 반환
        LoginResponse responseData = memberQueryService.findMyLatestInfo(loginMember.getId());

        return MemberApiResponse.success("내 정보 조회 성공", responseData);
    }

}
