package project.jpa.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.jpa.Time.BaseTimeEntity;
import project.jpa.enums.ReportStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseTimeEntity { // 사용자가 특정 좌석의 고장을 신고하면, 관리자가 이를 확인하고 처리 상태를 변경

    @Id
    @Column(name = "report_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; //누가 신고 했는지 알기 위해서 (다대일 단방향)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat; //어떤 자리를 신고했는지 (다대일 단방향)

    @Column(nullable = false, length = 500)
    private String content; // 신고 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // 처리 상태


    // ================= 생성 및 비즈니스 로직 ================= //


    /**
     * 신고 접수 (생성 팩토리 메서드)
     */
    public static Report createReport(Member member, Seat seat, String content) {

        Report report = new Report();

        report.member = member;
        report.seat = seat;
        report.content = content;
        report.status = ReportStatus.RECEIVED; //접수대기

        return report;
    }

    /**
     * 상태 변경 (관리자용) , 점검 완료로 상태변경
     */
    public void resolve() {
        this.status = ReportStatus.RESOLVED;
    }

    /**
     * 상태 변경 (관리자용) , 점검 중으로 상태 변경
     */
    public void startProgress() {
        if (this.status == ReportStatus.RESOLVED) {
            throw new IllegalStateException("이미 처리 완료된 신고입니다.");
        }
        this.status = ReportStatus.IN_PROGRESS;
    }




}
