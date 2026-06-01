package project.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.jpa.domain.Report;
import project.jpa.enums.ReportStatus;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> , ReportRepositoryCustom{


    /**
     * 회원이 탈퇴할때 자식의 테이블에서 외래키를 지운다.
     */
    @Modifying(clearAutomatically = true)
    @Query("delete from Report r where r.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

}
