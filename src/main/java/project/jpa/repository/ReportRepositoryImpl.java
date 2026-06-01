package project.jpa.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import project.jpa.domain.QMember;
import project.jpa.domain.QReport;
import project.jpa.domain.QSeat;
import project.jpa.domain.Report;
import project.jpa.enums.ReportStatus;

import java.util.List;

import static project.jpa.domain.QMember.*;
import static project.jpa.domain.QReport.*;
import static project.jpa.domain.QSeat.*;

public class ReportRepositoryImpl implements ReportRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ReportRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 1. [사용자용] 내 신고 내역 페이징
     */
    @Override
    public Page<Report> findMyReports(Long memberId, Pageable pageable) {

        List<Report> content = queryFactory
                .selectFrom(report)
                .join(report.seat, seat).fetchJoin() // 좌석 정보 N+1 방지
                .where(report.member.id.eq(memberId))
                .orderBy(report.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(report.count())
                .from(report)
                .where(report.member.id.eq(memberId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    /**
     * 2. [관리자용] 상태별 전체 신고 내역 페이징 (status 동적 쿼리)
     */
    @Override
    public Page<Report> findReportsByStatus(ReportStatus status, Pageable pageable) {

        List<Report> content = queryFactory
                .selectFrom(report)
                .join(report.member, member).fetchJoin() // 신고자 정보 N+1 방지
                .join(report.seat, seat).fetchJoin()     // 좌석 정보 N+1 방지
                .where(statusEq(status))                 // 동적 쿼리 조건
                .orderBy(report.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(report.count())
                .from(report)
                .where(statusEq(status));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 상태(status)가 파라미터로 넘어오면 조건을 걸고, null이면 전체 조회(조건 무시)
    private BooleanExpression statusEq(ReportStatus status) {
        return status != null ? report.status.eq(status) : null;
    }
}
