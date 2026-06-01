package project.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.jpa.domain.SeatBookmark;

import java.util.List;
import java.util.Optional;

public interface SeatBookmarkRepository extends JpaRepository<SeatBookmark, Long> , SeatBookmarkRepositoryCustom{

    /**
     * 회원이 탈퇴할때 자식의 테이블에서 외래키를 지운다.
     */
    @Modifying(clearAutomatically = true)
    @Query("delete from SeatBookmark s where s.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 회원이 특정 좌석을 이미 즐겨찾기 했는지 확인 (토글 로직용)
     */
    @Query("SELECT b FROM SeatBookmark b " +
            "WHERE b.member.id = :memberId AND b.seat.id = :seatId")
    Optional<SeatBookmark> findBookmarkByMemberAndSeat(@Param("memberId") Long memberId, @Param("seatId") Long seatId);



}
