package project.jpa.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.util.StringUtils;

import project.jpa.dto.seatapidto.QSeatDto;
import project.jpa.dto.seatapidto.SeatDto;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;

import java.util.List;

import static project.jpa.domain.QSeat.*;

public class SeatRepositoryImpl implements SeatRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public SeatRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    /**
     * FR#6. [사용자용] 통합공간 및 실시간 좌석 검색(건물이름, 층수 , 장소유형 , 좌석의 상태)
     */
    @Override
    public List<SeatDto> search(SeatSearchCondition condition) {

       return queryFactory
                .select(new QSeatDto(
                        seat.id,                     //PK 값
                        seat.buildingName,           //건물 이름
                        seat.floor,                  //층수
                        seat.seatNumber,             //좌석 번호
                        seat.spaceType,              //장소 유형
                        seat.status,                 //좌석 상태
                        seat.rowIndex,               //행 좌표
                        seat.colIndex                //열 좌표
                        ))
                .from(seat)
                .where(
                        buildingNameEq(condition.getBuildingName()), //건물 이름으로 조건
                        floorEq(condition.getFloor()),               //층수로 조건
                        spaceTypeEq(condition.getSpaceType()),       //장소 유형으로 조건
                        statusEq(condition.getStatus())              //좌석 상태로 조건
                )
                .fetch();

    }

    /**
     * 1. [좌표 중복 검사]
     * 해당 공간(건물/층수/타입)의 특정 그리드(행/열)에 이미 좌석이 존재하는지 확인
     */
    @Override
    public boolean existsSeatAtGrid(String buildingName, Integer floor, SpaceType spaceType, Integer rowIndex, Integer colIndex) {

        Integer fetchOne = queryFactory
                .selectOne() // SELECT 1(전체 데이터를 가져오는게 아니라 조건에 맞는 데이터가 있을경우 숫자1만 반환
                .from(seat)
                .where(
                        seat.buildingName.eq(buildingName),
                        seat.floor.eq(floor),
                        seat.spaceType.eq(spaceType),
                        seat.rowIndex.eq(rowIndex),
                        seat.colIndex.eq(colIndex)
                )
                .fetchFirst(); //  LIMIT 1 (하나라도 발견하면 즉시 탐색 종료)

        return fetchOne != null; // 결과가 있으면(중복이면) true 반환
    }

    //[번호 중복 검사] 같은 공간에 똑같은 좌석 번호(예: A-1)가 있는지 확인
    @Override
    public boolean existsSeatNumber(String buildingName, Integer floor, SpaceType spaceType, String seatNumber) {

        Integer fetchOne = queryFactory
                .selectOne() // SELECT 1(전체 데이터를 가져오는게 아니라 조건에 맞는 데이터가 있을경우 숫자1만 반환
                .from(seat)
                .where(
                        seat.buildingName.eq(buildingName),
                        seat.floor.eq(floor),
                        seat.spaceType.eq(spaceType),
                        seat.seatNumber.eq(seatNumber)
                )
                .fetchFirst(); // LIMIT 1 (하나라도 발견하면 즉시 탐색 종료)

        return fetchOne != null; // 결과가 있으면(중복이면) true 반환
    }

    /**
     * 건물 이름 조건 메서드
     */
    private Predicate buildingNameEq(String buildingName) {

        if (!StringUtils.hasText(buildingName)) {
            return null;
        }
        return seat.buildingName.eq(buildingName);
    }

    /**
     * 층수 조건 메서드
     */
    private Predicate floorEq(Integer floor) {

        if (floor == null) {
            return null;
        }
        return seat.floor.eq(floor);
    }

    /**
     * 장소 유형 조건 메서드
     */
    private Predicate spaceTypeEq(SpaceType spaceType) {

        if (spaceType == null) {
            return null;
        }
        return seat.spaceType.eq(spaceType);
    }

    /**
     * 좌석 상태 조건 메서드
     */
    private Predicate statusEq(SeatStatus status) {

        if (status == null) {
            return null;
        }
        return seat.status.eq(status);
    }
}
