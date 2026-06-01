package project.jpa.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.UsageHistory;
import project.jpa.dto.usagehistoryapidto.UsageHistoryDto;
import project.jpa.repository.UsageHistoryRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UsageHistoryQueryService {

    private final UsageHistoryRepository usageHistoryRepository;

    /**
     * FR#10. 내 이용 기록 전체 조회 API (엔티티 노출 차단, 즉시 DTO 변환)
     */
    public Page<UsageHistoryDto> findMyUsageHistories(Long memberId, Pageable pageable) {

        // 1. DB에서 엔티티를 Page로 가져옴
        Page<UsageHistory> historyPage = usageHistoryRepository.findMyHistories(memberId, pageable);

        // 2. 트랜잭션이 살아있을 때 안전하게 DTO로 변환하여 반환
        return historyPage.map(history -> new UsageHistoryDto(
                history.getId(),                        //이용기록 PK
                history.getSeat().getId(),
                history.getSeat().getBuildingName(),    //이용한 좌석의 건물이름
                history.getSeat().getFloor(),
                history.getSeat().getSpaceType().getDescription(),
                history.getSeat().getSeatNumber(),      //이용한 좌석의 번호
                history.getStartTime(),                 //이용한 시작 시간
                history.getEndTime(),                   //이용이 끝난 시간
                history.getStatus().getDescription()    //이용기록 상태
        ));
    }


    /**
     * 마이페이지 메인: 현재 이용 중인 좌석 단건 조회 (엔티티 노출 차단, 즉시 DTO 변환)
     */
    public UsageHistoryDto findMyActiveUsage(Long memberId) {

        return usageHistoryRepository.findMyActiveHistory(memberId)
                .map(history -> new UsageHistoryDto(
                        history.getId(),
                        history.getSeat().getId(),
                        history.getSeat().getBuildingName(),
                        history.getSeat().getFloor(),
                        history.getSeat().getSpaceType().getDescription(),
                        history.getSeat().getSeatNumber(),
                        history.getStartTime(),
                        history.getEndTime(),
                        history.getStatus().getDescription()
                ))
                .orElse(null); // 이용 중인 좌석이 없으면 null 반환
    }
}
