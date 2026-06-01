package project.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.jpa.domain.UsageHistory;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.dto.usagehistoryapidto.ActiveUserDto;

public interface UsageHistoryRepositoryCustom {

    // 마이페이지 이용 기록 페이징 조회용 메서드
    Page<UsageHistory> findMyHistories(Long memberId, Pageable pageable);

    // 실시간 모니터링 페이징 처리
    Page<ActiveUserDto> findActiveUsersMonitoring(SeatSearchCondition condition , Pageable pageable);
}
