package project.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jmx.export.annotation.ManagedOperation;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.domain.UsageHistory;

import java.util.List;
import java.util.Optional;

public interface UsageHistoryRepository extends JpaRepository<UsageHistory, Long> , UsageHistoryRepositoryCustom {

    /**
     * 좌석 사용종료 할때 어떤 사용자가 어떤 좌석을 사용했었는지 알기 위한 메서드
     */
    @Query("SELECT u FROM UsageHistory u " +
            "WHERE u.member.id = :memberId " +
            "AND u.seat.id = :seatId " +
            "AND u.status = 'USING'")
    Optional<UsageHistory> findActiveHistoryByMemberAndSeat(@Param("memberId") Long memberId,
                                                            @Param("seatId") Long seatId);

    /**
     * 회원이 탈퇴할때 자식의 테이블에서 외래키를 지운다.
     */
    @Modifying(clearAutomatically = true)
    @Query("delete from UsageHistory u where u.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);


    /**
     * 특정 회원의 히스토리 중 상태가 'USING'인 것의 개수를 세어라
     */
    @Query("SELECT COUNT(u) FROM UsageHistory u WHERE u.member.id = :memberId AND u.status = 'USING'")
    long countActiveUsageByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 회원이 특정 좌석을 현재 사용 중(USING)인지 여부를 반환 (boolean)
     */
    @Query("SELECT COUNT(u) > 0 FROM UsageHistory u " +
            "WHERE u.member.id = :memberId " +
            "AND u.seat.id = :seatId " +
            "AND u.status = 'USING'")
    boolean existsActiveUsage(@Param("memberId") Long memberId, @Param("seatId") Long seatId);

    /**
     * 마이페이지용: 현재 사용 중(USING)인 좌석 내역 단건 조회
     */
    @Query("SELECT u FROM UsageHistory u JOIN FETCH u.seat WHERE u.member.id = :memberId AND u.status = 'USING'")
    Optional<UsageHistory> findMyActiveHistory(@Param("memberId") Long memberId);
}
