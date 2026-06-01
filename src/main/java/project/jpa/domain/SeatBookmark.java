package project.jpa.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.jpa.Time.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatBookmark extends BaseTimeEntity { // 사용자가 마음에 드는 좌석을 찜해두는 기능

    @Id
    @Column(name = "seatbookmark_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id" , nullable = false)
    private Member member; //누가 찜했는가? (다대일 단방향)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id" , nullable = false)
    private Seat seat; // 어떤 자리를 찜했는가? (다대일 단방향)

    // ================= 생성 팩토리 메서드 ================= //

    /**
     * 즐겨찾기시 생성
     */
    public static SeatBookmark createSeatBookmark(Member member, Seat seat) {
        SeatBookmark seatBookmark = new SeatBookmark();

        seatBookmark.member = member;
        seatBookmark.seat = seat;

        return seatBookmark;
    }



}
