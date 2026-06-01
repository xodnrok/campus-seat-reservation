package project.jpa.dto.seatapidto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class MultipleSeatRequest {

    @NotEmpty(message = "선택된 좌석이 없습니다. 하나 이상의 좌석을 선택해주세요.") // List 컬렉션 검증
    private List<Long> seatIds;
}
