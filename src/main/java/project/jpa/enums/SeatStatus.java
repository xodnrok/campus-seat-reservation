package project.jpa.enums;

import lombok.Getter;

@Getter
public enum SeatStatus {

    AVAILABLE("이용 가능"),
    IN_USE("사용 중"),
    MAINTENANCE("점검 중");

    private final String description;

    SeatStatus(String description) {
        this.description = description;
    }

}
