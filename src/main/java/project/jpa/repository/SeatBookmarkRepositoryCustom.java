package project.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import project.jpa.domain.SeatBookmark;

public interface SeatBookmarkRepositoryCustom {

    //  FR#16. 내 즐겨찾기 좌석 목록 페이징 조회
    Page<SeatBookmark> findMyBookmarks(Long memberId, Pageable pageable);
}
