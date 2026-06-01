package project.jpa.Time;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass //  자식 엔티티에게 매핑 정보만 제공
@EntityListeners(AuditingEntityListener.class) //  JPA에게 시간 자동 관리를 위임
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 엔티티 생성 시 자동 저장(등록일)

    @LastModifiedDate
    private LocalDateTime updatedAt; // 엔티티 수정 시 자동 저장(수정일)
}
