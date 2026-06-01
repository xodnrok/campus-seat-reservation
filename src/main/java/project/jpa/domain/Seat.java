package project.jpa.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import project.jpa.Time.BaseTimeEntity;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
//  1. @Version이 있으므로 WHERE 절에 파라미터(?)를 2개(seat_id, version) 받아야 정상 작동 , 삭제요청이 올경우 이걸 동작해
@SQLDelete(sql = "UPDATE seat SET is_deleted = true WHERE seat_id = ? AND version = ?")
//  2. Spring Boot 3 환경에 맞춘 최신 소프트 삭제 필터링 애노테이션 적용 , 앞으로 이 엔티티를 조회하는 모든 SELECT 쿼리 맨 끝에 무조건 이 조건을 강제로 붙임
@SQLRestriction("is_deleted = false")
public class Seat extends BaseTimeEntity { //관리자가 등록한 공간 정보와 시각적 레이아웃 데이터, 그리고 실시간 상태를 관리


    @Id
    @Column(name = "seat_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(nullable = false)
    private String buildingName; // 건물명

    @Column(nullable = false)
    private Integer floor; // 층수

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpaceType spaceType; // 공간 유형 ( 스터디 라운지 , 휴게실/테라스 , 취식 공간 )

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status; // 좌석 상태 ( 이용 가능 , 사용 중 , 점검 중 )

    @Column(nullable = false)
    private String seatNumber; //좌석 번호

    @Column(nullable = false)
    private Integer rowIndex; // 영화관식 그리드 배치를 위한 행 좌표 값

    @Column(nullable = false)
    private Integer colIndex; // 영화관식 그리드 배치를 위한 열 좌표 값

    //소프트 삭제 플래그 (삭제 시 true 로 변경됨)
    @Column(nullable = false)
    private boolean isDeleted = false;

    @Version
    private Long version; //다수 사용자의 동시 예약 방지

    @OneToMany(mappedBy = "seat")
    private List<UsageHistory> histories = new ArrayList<>(); // 일대다 'UsageHistory(이용 기록)' 연관관계

    // ================= 비즈니스 로직 ================= //

    /**
     * 좌석 생성
     */
    public static Seat createSeat(String buildingName, Integer floor, SpaceType spaceType,
                                  String seatNumber, Integer rowIndex, Integer colIndex) {
        Seat seat = new Seat();

        seat.buildingName = buildingName; // 건물명 등록

        seat.floor = floor; // 층수 등록

        seat.spaceType = spaceType; // 공간 유형 등록

        seat.seatNumber = seatNumber; // 좌석 번호 등록

        seat.rowIndex = rowIndex; // 영화관식 그리드 배치를 위한 행 좌표 값 등록

        seat.colIndex = colIndex; // 영화관식 그리드 배치를 위한 열 좌표 값 등록

        seat.status = SeatStatus.AVAILABLE; // 최초 등록 시 무조건 '이용 가능' 상태

        return seat;
    }

    /**
     * 좌석 수정
     */
    public void updateInformation(String buildingName, Integer floor, SpaceType spaceType ,String seatNumber, Integer rowIndex, Integer colIndex) {
        this.buildingName = buildingName;
        this.floor = floor;
        this.spaceType = spaceType;
        this.seatNumber = seatNumber;
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
    }

    /**
     * 좌석 사용 시작
     * 사용자가 예약을 시작할 때 호출됩니다.
     */
    public void assignUser() {
        if (this.status != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("현재 이용할 수 없는 좌석입니다."); // 에러 발생
        }
        this.status = SeatStatus.IN_USE; // 좌석 사용중으로 변경
    }


    /**
     * 좌석 사용 종료
     * 사용 종료 시 상태를 AVAILABLE로 원복합니다.
     */
    public void releaseUser() {
        if (this.status != SeatStatus.IN_USE) {
            throw new IllegalStateException("현재 사용 중인 좌석이 아닙니다."); //에러 발생
        }
        this.status = SeatStatus.AVAILABLE; // 좌석 이용 가능으로 변경
    }


    /**
     * 시설 상태 강제 전환
     * 관리자가 고장, 청소 등의 이유로 강제로 '점검 중'으로 전환합니다.
     */
    public void changeToMaintenance() {
        this.status = SeatStatus.MAINTENANCE; // 점검중 상태로 변경
    }

    /**
     * 점검 완료 후 다시 이용 가능 상태로 복구 (관리자용 추가 편의 메서드)
     */
    public void resolveMaintenance() {
        if (this.status == SeatStatus.MAINTENANCE) { // 점검중이라면
            this.status = SeatStatus.AVAILABLE; // 이용 가능으로 변경
        }
    }
}


