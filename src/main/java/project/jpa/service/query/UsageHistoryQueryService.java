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
                history.getId(),                            //사용기록 PK
                history.getSeat().getId(),                  //사용 좌석 PK
                history.getHistoryBuildingName(),           //사용좌석 건물 이름
                history.getHistoryFloor(),                  //사용좌석 층수
                history.getHistorySpaceType(),              //사용좌석 유형
                history.getHistorySeatNumber(),             //사용좌석 번호
                history.getStartTime(),                     //사용시작 시간
                history.getEndTime(),                       //사용종료 시간
                history.getStatus().getDescription()        //사용기록 상태
        ));
    }


    /**
     * 마이페이지 메인: 현재 이용 중인 좌석 단건 조회 (엔티티 노출 차단, 즉시 DTO 변환)
     */
    public UsageHistoryDto findMyActiveUsage(Long memberId) {

        return usageHistoryRepository.findMyActiveHistory(memberId)
                .map(history -> new UsageHistoryDto(
                        history.getId(),                            //사용기록 PK
                        history.getSeat().getId(),                  //사용 좌석 PK
                        history.getHistoryBuildingName(),           //사용좌석 건물 이름
                        history.getHistoryFloor(),                  //사용좌석 층수
                        history.getHistorySpaceType(),              //사용좌석 유형
                        history.getHistorySeatNumber(),             //사용좌석 번호
                        history.getStartTime(),                     //사용시작 시간
                        history.getEndTime(),                       //사용종료 시간
                        history.getStatus().getDescription()        //사용기록 상태
                ))
                .orElse(null);
    }
}
