package project.jpa.dto.seatapidto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import project.jpa.enums.SpaceType;

@Data
public class SeatRegisterRequest {

    @NotBlank(message = "건물명은 필수 입력 값입니다.") // String 타입 검증
    private String buildingName;

    @NotNull(message = "층수는 필수 입력 값입니다.") // Integer 숫자 검증
    private Integer floor;

    @NotNull(message = "공간 유형을 선택해주세요.") // Enum 타입 검증
    private SpaceType spaceType;

    @NotBlank(message = "좌석 번호는 필수 입력 값입니다.")
    private String seatNumber;

    @NotNull(message = "행(Row) 좌표를 입력해주세요.")
    @Min(value = 0, message = "좌표 값은 0 이상이어야 합니다.") // 음수 방지
    private Integer rowIndex;

    @NotNull(message = "열(Column) 좌표를 입력해주세요.")
    @Min(value = 0, message = "좌표 값은 0 이상이어야 합니다.") // 음수 방지
    private Integer colIndex;
}
