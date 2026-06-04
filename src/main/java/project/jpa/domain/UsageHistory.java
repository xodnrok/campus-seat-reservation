package project.jpa.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.jpa.Time.BaseTimeEntity;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.UsageStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageHistory extends BaseTimeEntity { //회원과 좌석 사이의 다대다 관계를 풀어주는 연결 엔티티이자, 개인의 이용 히스토리를 저장하는 객체

    @Id
    @Column(name = "usagehistory_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //PK

    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime; // 좌석 사용 시작 시간

    private LocalDateTime endTime;   // 좌석 사용 종료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsageStatus status; // 이용상태( USING, COMPLETED )


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id" , nullable = false)
    private Member member; //어떤 회원이 사용했는지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id" , nullable = false)
    private Seat seat; //어떤 좌석을 사용 했는지

    // 스냅샷 필드: 좌석이 삭제되어도 과거 기록에 이름을 띄워주기 위함
    private String historyBuildingName;

    private Integer historyFloor;

    private String historySpaceType;

    private String historySeatNumber;

    // ================= 비즈니스 로직 & 연관관계 편의 메서드 ================= //

    /**
     * 이용 기록 생성 (사용 시작 버튼을 눌렀을 때 호출)
     */
    public static UsageHistory createHistory(Member member, Seat seat) {

        UsageHistory history = new UsageHistory();

        history.setMember(member);

        history.setSeat(seat);

        history.startTime = LocalDateTime.now();

        history.status = UsageStatus.USING;

        // 예약 당시의 좌석 정보를 스냅샷으로 영구 박제!
        history.historyBuildingName = seat.getBuildingName();
        history.historyFloor = seat.getFloor();
        history.historySpaceType = seat.getSpaceType().getDescription();
        history.historySeatNumber = seat.getSeatNumber();

        return history;
    }

    /**
     * 이용 종료 처리 (반납 버튼을 눌렀을 때 호출)
     */
    public void completeUsage() {
        this.endTime = LocalDateTime.now(); //반납하는 순간 시간 값 넣기
        this.status = UsageStatus.COMPLETED; //상태 변경
    }

    /**
     * 연관 관계(member) 편의 메서드
     */

    public void setMember(Member member) {
        this.member = member;
        member.getHistories().add(this);
    }

    /**
     * 연관 관계(seat) 편의 메서드
     */
    public void setSeat(Seat seat) {
        this.seat = seat;
        seat.getHistories().add(this);
    }

}
