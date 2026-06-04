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

public class ViewAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 정적 리소스 요청이면 패스
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        HttpSession session = request.getSession(false);

        // 1. [비회원 방어]
        if (session == null || session.getAttribute(MemberApiController.LOGIN_MEMBER) == null) {
            // "로그인이 필요하다"는 꼬리표를 달아서 보냄
            response.sendRedirect("/login?error=required");
            return false;
        }

        SessionMember loginMember = (SessionMember) session.getAttribute(MemberApiController.LOGIN_MEMBER);

        // 2. [관리자 방어]
        if (handlerMethod.hasMethodAnnotation(AdminOnly.class)) {
            if (loginMember.getRole() != MemberRole.ADMIN) {
                // "권한이 없다(어드민 전용)"는 꼬리표를 달아서 보냄
                response.sendRedirect("/login?error=admin");
                return false;
            }
        }

        return true;
    }
}
