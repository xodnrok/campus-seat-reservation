package project.jpa.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;
import project.jpa.ApiController.MemberApiController;

public class ViewAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute(MemberApiController.LOGIN_MEMBER) == null) {
            //  예외를 던지지 않고, 바로 로그인 페이지로 주소를 틀어버립니다.
            response.sendRedirect("/login");
            return false; // 더 이상 컨트롤러로 요청을 진행하지 않고 종료
        }
        return true;
    }
}
