package project.jpa.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import project.jpa.domain.QMember;
import project.jpa.domain.QSeat;
import project.jpa.domain.QUsageHistory;
import project.jpa.domain.UsageHistory;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.dto.usagehistoryapidto.ActiveUserDto;
import project.jpa.dto.usagehistoryapidto.QActiveUserDto;
import project.jpa.enums.SpaceType;

import java.util.List;

import static project.jpa.domain.QMember.*;
import static project.jpa.domain.QSeat.*;
import static project.jpa.domain.QUsageHistory.*;

public class UsageHistoryRepositoryImpl implements UsageHistoryRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public UsageHistoryRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * FR#10. 내 이용 기록 전체 조회 API (엔티티 노출 차단, 즉시 DTO 변환)
     */
    @Override
    public Page<UsageHistory> findMyHistories(Long memberId, Pageable pageable) {

        // 1. 컨텐츠 조회 쿼리 (페이징 적용: offset, limit)
        List<UsageHistory> content = queryFactory
                .selectFrom(usageHistory)
                .where(usageHistory.member.id.eq(memberId))
                .orderBy(usageHistory.startTime.desc())    // 최신순 정렬 유지
                .offset(pageable.getOffset())              // 시작점 [cite: 8]
                .limit(pageable.getPageSize())             // 가져올 개수 [cite: 8]
                .fetch();

        // 2. 카운트 쿼리 분리 (카운트만 세기 때문에 fetchJoin이 필요 없음)
        JPAQuery<Long> countQuery = queryFactory
                .select(usageHistory.count())
                .from(usageHistory)
                .where(usageHistory.member.id.eq(memberId));

        // 3. PageableExecutionUtils를 이용한 최적화된 Page 객체 반환
        // (조건에 따라 countQuery.fetchOne()이 아예 실행되지 않을 수도 있음)
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * FR#17 [관리자] 실시간 이용자 모니터링 목록 조회 (페이징) API
     */
    @Override
    public Page<ActiveUserDto> findActiveUsersMonitoring(SeatSearchCondition condition, Pageable pageable) {

        // 1. 실제 데이터 조회 쿼리 (동적 WHERE 절 + 페이징)
        List<ActiveUserDto> content = queryFactory
                .select(new QActiveUserDto(
                        usageHistory.id,                //이용기록 PK
                        seat.id,                        //좌석 PK
                        member.name,                    //회원 이름
                        member.loginId,                 //회원 로그인 ID
                        seat.buildingName,              //좌석 건물이름
                        seat.floor,                     //좌석 층
                        seat.spaceType,                 //좌석 장소 유형
                        seat.seatNumber,                //좌석 번호
                        usageHistory.startTime          //이용기록 시작 시간
                ))
                .from(usageHistory)
                .join(usageHistory.member, member)
                .join(usageHistory.seat, seat)
                .where(
                        usageHistory.endTime.isNull(), // 현재 이용 중인 사람만
                        buildingEq(condition.getBuildingName()), // 💡 동적 필터링 메서드
                        floorEq(condition.getFloor()),
                        spaceTypeEq(condition.getSpaceType())
                )
                .orderBy(usageHistory.startTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 2. 전체 개수 카운트 쿼리 (필터링된 결과의 총 개수)
        JPAQuery<Long> countQuery = queryFactory
                .select(usageHistory.count())
                .from(usageHistory)
                .join(usageHistory.seat, seat) // 카운트 쿼리에서도 좌석 조건이 필요하므로 조인 유지
                .where(
                        usageHistory.endTime.isNull(),
                        buildingEq(condition.getBuildingName()),
                        floorEq(condition.getFloor()),
                        spaceTypeEq(condition.getSpaceType())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // ==========================================
    //  QueryDSL 동적 쿼리를 위한 BooleanExpression 메서드들
    // 값이 null 이거나 비어있으면 null을 반환하여 WHERE 절에서 무시됨 (전체 검색 효과)
    // ==========================================
    private BooleanExpression buildingEq(String buildingName) {
        return StringUtils.hasText(buildingName) ? seat.buildingName.eq(buildingName) : null;
    }

    private BooleanExpression floorEq(Integer floor) {
        return floor != null ? seat.floor.eq(floor) : null;
    }

    private BooleanExpression spaceTypeEq(SpaceType spaceType) {
        return spaceType != null ? seat.spaceType.eq(spaceType) : null;
    }
}
