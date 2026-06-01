package project.jpa.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import project.jpa.domain.QSeat;
import project.jpa.domain.QSeatBookmark;
import project.jpa.domain.SeatBookmark;

import java.util.List;

import static project.jpa.domain.QSeat.*;
import static project.jpa.domain.QSeatBookmark.*;

public class SeatBookmarkRepositoryImpl implements SeatBookmarkRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SeatBookmarkRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<SeatBookmark> findMyBookmarks(Long memberId, Pageable pageable) {

        // 1. 컨텐츠 조회 쿼리 (페이징 적용)
        List<SeatBookmark> content = queryFactory
                .selectFrom(seatBookmark)
                .join(seatBookmark.seat, seat).fetchJoin() // 좌석 정보 N+1 방지
                .where(seatBookmark.member.id.eq(memberId))
                .orderBy(seatBookmark.createdAt.desc())    // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 카운트 쿼리 (페이징 개수 계산용)
        JPAQuery<Long> countQuery = queryFactory
                .select(seatBookmark.count())
                .from(seatBookmark)
                .where(seatBookmark.member.id.eq(memberId));

        // 3. 최적화된 Page 객체 반환 (필요할 때만 count 쿼리 실행)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
