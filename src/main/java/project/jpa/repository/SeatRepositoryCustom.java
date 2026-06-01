package project.jpa.repository;

import project.jpa.dto.seatapidto.SeatDto;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.enums.SpaceType;

import java.util.List;

public interface SeatRepositoryCustom {

    List<SeatDto> search(SeatSearchCondition condition); //동적검색


    // 1. [좌표 중복 검사] 같은 공간에 똑같은 행/열을 가진 좌석이 있는지 확인
    boolean existsSeatAtGrid(
            String buildingName, Integer floor, SpaceType spaceType, Integer rowIndex, Integer colIndex);


    // 2. [번호 중복 검사] 같은 공간에 똑같은 좌석 번호(예: A-1)가 있는지 확인
    boolean existsSeatNumber(
            String buildingName, Integer floor, SpaceType spaceType, String seatNumber);

}
