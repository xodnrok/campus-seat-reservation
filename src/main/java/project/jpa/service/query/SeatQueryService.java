package project.jpa.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.dto.seatapidto.SeatDto;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.dto.usagehistoryapidto.ActiveUserDto;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatQueryService {

    private final SeatRepository seatRepository;
    private final UsageHistoryRepository usageHistoryRepository;

    /**
     * FR#6. 통합공간 및 실시간 좌석 검색(건물이름, 층수 , 장소유형 , 좌석의 상태)
     */
    public List<SeatDto> searchSeats(SeatSearchCondition condition) {
        return seatRepository.search(condition);
    }

    /**
     * FR#17 [관리자] 실시간 이용자 모니터링 목록 조회 (페이징) API
     */
    public Page<ActiveUserDto> getActiveUsers(SeatSearchCondition condition, Pageable pageable) {
        return usageHistoryRepository.findActiveUsersMonitoring(condition , pageable);
    }



}
