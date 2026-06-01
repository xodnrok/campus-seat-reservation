package project.jpa.enums;

import lombok.Getter;

@Getter
public enum MemberRole {
    USER("일반 회원"),
    ADMIN("관리자");

    private final String description;

    MemberRole(String description) {
        this.description = description;
    }

}
