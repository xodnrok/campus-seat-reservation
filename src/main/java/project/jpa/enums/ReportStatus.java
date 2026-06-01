package project.jpa.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {

    RECEIVED("접수 대기"),
    IN_PROGRESS("점검 중"),
    RESOLVED("점검 완료");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

}
