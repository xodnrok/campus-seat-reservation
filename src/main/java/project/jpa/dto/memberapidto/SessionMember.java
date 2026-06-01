package project.jpa.dto.memberapidto;

import lombok.AllArgsConstructor;
import lombok.Data;
import project.jpa.enums.MemberRole;

@Data
@AllArgsConstructor
public class SessionMember {

        //세션에 넣을 dto 클래스

        private Long id;
        private String name;
        private MemberRole role; // USER(회원) 또는 ADMIN(관리자)
}
