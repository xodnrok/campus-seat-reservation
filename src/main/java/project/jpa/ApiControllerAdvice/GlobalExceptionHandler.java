package project.jpa.ApiControllerAdvice;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.jpa.dto.memberapidto.MemberApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "project.jpa.ApiController")
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리 1 (IllegalStateException)
     * 예: 중복 아이디 가입 시도, 권한 없는 좌석 반납 등
     */
    @ExceptionHandler(IllegalStateException.class)
    public MemberApiResponse<Object> handleIllegalStateException(IllegalStateException e) {

        // 서비스 계층에서 작성한 에러 메시지(e.getMessage())를 그대로 뽑아서 프론트엔드로 전달
        return MemberApiResponse.error(e.getMessage());

    }

    /**
     * 비즈니스 로직 예외 처리 2 (IllegalArgumentException)
     * 예: 틀린 비밀번호, 존재하지 않는 PK 조회 등
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public MemberApiResponse<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return MemberApiResponse.error(e.getMessage());
    }

    /**
     * 낙관적 락(Optimistic Lock) 동시성 충돌 예외 처리 |
     * 예시: 두 명의 사용자가 0.001초 차이로 동일한 좌석을 동시에 예약하려고 할 때 발생
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public MemberApiResponse<Object> handleOptimisticLockingFailureException(ObjectOptimisticLockingFailureException e) {
        // 충돌이 발생하여 튕겨난 사용자에게 보여줄 친절한 메시지
        return MemberApiResponse.error("다른 사용자가 이미 해당 좌석을 예약 중이거나 상태가 변경되었습니다. 새로고침 후 다시 시도해주세요.");
    }

    /**
     * 💡 DTO 검증(@Valid) 실패 시 발생하는 필드 에러 전담 핸들러
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public MemberApiResponse<Object> handleValidationExceptions(MethodArgumentNotValidException e) {

        // 1. 발생한 모든 필드 에러를 예쁘게 담을 Map(바구니) 준비
        Map<String, String> fieldErrors = new HashMap<>();

        // 2. 에러가 난 필드 이름(key)과 우리가 DTO에 적은 메시지(value)를 바구니에 쏙쏙 담기
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        // 3. 글로벌 메시지와 필드 에러 데이터(Map)를 함께 프론트엔드로 전달!
        return MemberApiResponse.error("입력값이 올바르지 않습니다. 다시 확인해주세요.", fieldErrors);
    }


    /**
     * 예상치 못한 런타임 에러 처리 (최후의 보루)
     */
    @ExceptionHandler(Exception.class)
    public MemberApiResponse<Object> handleException(Exception e) {

        e.printStackTrace(); // 서버 로그에 에러 원인 출력
        return MemberApiResponse.error("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.");

    }

}
