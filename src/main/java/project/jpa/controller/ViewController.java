package project.jpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    /**
     * 1. 메인 홈 화면 (비회원, 일반회원, 관리자 모두 공통 접근)
     * 접속 주소: localhost:8080/
     */
    @GetMapping("/")
    public String home() {
        return "index";  // -> src/main/resources/templates/index.html 을 띄워줌
    }

    /**
     * 2. 로그인 및 회원가입 화면
     * 접속 주소: localhost:8080/login
     */
    @GetMapping("/login")
    public String login() {
        return "login";  // -> src/main/resources/templates/login.html 을 띄워줌
    }

    /**
     * 3. 일반 사용자 마이페이지 (이용 기록, 즐겨찾기, 내 신고내역 등)
     * 접속 주소: localhost:8080/mypage
     */
    @GetMapping("/mypage")
    public String mypage() {
        return "mypage";  // -> src/main/resources/templates/mypage.html 을 띄워줌
    }

    /**
     * 4. 관리자 전용 대시보드 (좌석 등록, 시설 제어, 전체 신고내역 처리 등)
     * 접속 주소: localhost:8080/admin
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";  // -> src/main/resources/templates/admin.html 을 띄워줌
    }

    /**
     * 5. 실시간 좌석 검색 및 배치도 화면
     * 접속 주소: localhost:8080/seats/search
     */
    @GetMapping("/seats/search")
    public String seatSearch() {
        // -> src/main/resources/templates/seats/search.html 을 띄워줌
        return "seats/search";
    }


}
