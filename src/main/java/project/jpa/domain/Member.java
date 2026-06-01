package project.jpa.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.jpa.Time.BaseTimeEntity;
import project.jpa.enums.MemberRole;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity { //회원의 기본 정보와 권한을 관리하며, 정보 수정 등의 행위를 담당

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //PK

    @Column(nullable = false, unique = true)
    private String loginId; //회원 아이디

    @Column(nullable = false)
    private String password; //회원 비밀번호

    @Column(nullable = false)
    private String name; //회원 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role; //권한( USER , ADMIN )

    @OneToMany(mappedBy = "member")
    private List<UsageHistory> histories = new ArrayList<>(); //일대다 'UsageHistory(이용 기록)' 연관관계


    // ================= 비즈니스 로직 ================= //

    /**
     * 회원 정보 수정
     */
    public void updateInfo(String password, String name) {
        this.password = password;
        this.name = name;
    }

    /**
     * 회원 생성
     */
    public static Member createMember(String loginId, String password, String name, MemberRole role) {
        return new Member(loginId, password, name, role);
    }


    private Member(String loginId, String password, String name, MemberRole role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
    }

}
