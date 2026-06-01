package project.jpa.dto.reportapidto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull(message = "신고할 좌석을 선택해주세요.")
    private Long seatId;

    @NotBlank(message = "신고 내용을 입력해주세요. (예: 콘센트 고장)")
    private String content;
}
