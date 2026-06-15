package project.jpa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.jpa.interceptor.AuthInterceptor;
import project.jpa.interceptor.ViewAuthInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * F21: 시스템 접근 제어 및 권한 검증
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 1. API 전용 인터셉터
        registry.addInterceptor(new AuthInterceptor())
                .order(1) // 가장 먼저 실행되도록 순서 지정
                .addPathPatterns("/api/**") // 모든 API 주소에 대해 로그인 체커 발동
                .excludePathPatterns(
                        "/api/members/join", // 회원가입은 로그인 없이 가능해야 함
                        "/api/members/login", // 로그인은 로그인 없이 가능해야 함
                        "/api/seats"          // 비로그인 학생도 잔여 좌석 현황 검색(GET)은 볼 수 있어야 함
                );


        // 2.  화면 전용 인터셉터
        registry.addInterceptor(new ViewAuthInterceptor())
                .order(2)
                .addPathPatterns("/mypage", "/admin"); // 보호할 화면 주소만 쏙 골라서 지정
    }
}
