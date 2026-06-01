package project.jpa.dto.memberapidto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberApiResponse<T> {

    private String status;  // "SUCCESS" 또는 "ERROR"
    private String message; // 프론트엔드 화면에 띄워줄 메시지
    private T data;         // 실제 데이터 (없으면 null)

    // 성공했을 때 간편하게 쓸 수 있는 생성 메서드
    public static <T> MemberApiResponse<T> success(String message, T data) {
        return new MemberApiResponse<>("SUCCESS", message, data);
    }

    // 실패했을 때 간편하게 쓸 수 있는 생성 메서드
    public static <T> MemberApiResponse<T> error(String message) {
        return new MemberApiResponse<>("ERROR", message, null);
    }

    // 필드 에러 바구니(data)까지 함께 보낼 수 있는 에러 생성 메서드
    public static <T> MemberApiResponse<T> error(String message, T data) {
        return new MemberApiResponse<>("ERROR", message, data);
    }
}
