package project.jpa.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.SeatBookmark;
import project.jpa.dto.bookmarkdto.BookmarkDto;
import project.jpa.repository.SeatBookmarkRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SeatBookmarkQueryService {

    private final SeatBookmarkRepository seatBookmarkRepository;


    /**
     * FR#16. 내 즐겨찾기 좌석 목록 조회 (엔티티 노출 차단, 즉시 DTO 변환)
     */
    public Page<BookmarkDto> findMyBookmarks(Long memberId, Pageable pageable) {

        // 1. DB에서 엔티티를 Page로 가져옴
        Page<SeatBookmark> bookmarkPage = seatBookmarkRepository.findMyBookmarks(memberId, pageable);

        // 2. 트랜잭션이 살아있을 때 안전하게 DTO로 변환하여 반환
        return bookmarkPage.map(bookmark -> new BookmarkDto(
                bookmark.getId(),                    // 즐겨찾기 PK
                bookmark.getSeat().getId(),          // 좌석 PK
                bookmark.getSeat().getBuildingName(),// 건물명
                bookmark.getSeat().getFloor(),       // 층수
                bookmark.getSeat().getSpaceType().getDescription(), //장소 유형
                bookmark.getSeat().getSeatNumber(),  // 좌석 번호
                bookmark.getSeat().getStatus().getDescription() // 현재 좌석 상태
        ));
    }
}
