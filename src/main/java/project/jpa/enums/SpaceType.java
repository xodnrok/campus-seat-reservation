package project.jpa.enums;

import lombok.Getter;

@Getter
public enum SpaceType {

    STUDY_LOUNGE("스터디 라운지"),
    REST_AREA("휴게실"),
    DINING_AREA("취식 공간");

    private final String description;

    SpaceType(String description) {
        this.description = description;
    }

}
