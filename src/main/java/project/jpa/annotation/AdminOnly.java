package project.jpa.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메서드에만 붙일 수 있도록 제한
@Retention(RetentionPolicy.RUNTIME) // 실행 중(런타임)에 스프링이 감지할 수 있도록 설정 , 관리자검증
public @interface AdminOnly {
}
