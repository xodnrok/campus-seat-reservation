package project.jpa.enums;

import lombok.Getter;

@Getter
public enum UsageStatus {

    USING("사용 중"),
    COMPLETED("이용 종료됨");

    private final String description;

    UsageStatus(String description) {
        this.description = description;
    }

}
