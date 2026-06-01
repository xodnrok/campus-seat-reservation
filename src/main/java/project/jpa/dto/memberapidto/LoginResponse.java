package project.jpa.dto.memberapidto;

import lombok.AllArgsConstructor;
import lombok.Data;
import project.jpa.enums.MemberRole;

@Data //로그인 결과 DTO
@AllArgsConstructor
public class LoginResponse {

    private Long memberId;
    private String name;
    private MemberRole role; // USER(회원) 또는 ADMIN(관리자)
}
