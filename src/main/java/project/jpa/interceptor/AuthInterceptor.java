package project.jpa.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import project.jpa.ApiController.MemberApiController;
import project.jpa.annotation.AdminOnly;
import project.jpa.dto.memberapidto.SessionMember;
import project.jpa.enums.MemberRole;

public class AuthInterceptor implements HandlerInterceptor {

    /**
     * F21: 시스템 접근 제어 및 권한 검증
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 정적 리소스(.html, .css 등) 요청이거나 컨트롤러 메서드 매핑이 아니면 검사를 건너뛰고 Pass 시킵니다.
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. [기본 방어] 로그인 세션 검증
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(MemberApiController.LOGIN_MEMBER) == null) {
            // 예외를 던지면 GlobalExceptionHandler가 캐치하여 JSON 에러를 준다.
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // 세션 유저 객체 꺼내기
        SessionMember loginMember = (SessionMember) session.getAttribute(MemberApiController.LOGIN_MEMBER);

        // 2. [어드민 방어] 메서드에 @AdminOnly 애노테이션이 붙어 있는지 확인
        if (handlerMethod.hasMethodAnnotation(AdminOnly.class)) {
            if (loginMember.getRole() != MemberRole.ADMIN) {
                throw new IllegalStateException("관리자만 접근할 수 있는 기능입니다.");
            }
        }

        return true; // 모든 검증을 무사히 통과하면 진짜 컨트롤러 메서드 실행
    }
}
