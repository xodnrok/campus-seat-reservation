package project.jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.domain.UsageHistory;
import project.jpa.dto.seatapidto.SeatRegisterRequest;
import project.jpa.enums.SeatStatus;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final UsageHistoryRepository usageHistoryRepository;
    private final MemberRepository memberRepository;

    /**
     * FR#11.  좌석 및 시설 기초 정보 등록(단건)
     */
    public Long registerSeat(String buildingName, Integer floor, SpaceType spaceType,
                             String seatNumber, Integer rowIndex, Integer colIndex) {

        // DB에 저장하기 전에 가장 먼저 중복 검증을 실행
        validateDuplicateSeat(buildingName, floor, spaceType, seatNumber, rowIndex, colIndex);

        // 엔티티에 만들어둔 생성 메서드 호출
        Seat seat = Seat.createSeat(buildingName, floor, spaceType, seatNumber, rowIndex, colIndex);

        // DB에 저장
        seatRepository.save(seat);

        return seat.getId();

    }


    /**
     * FR#11, #12. 좌석 기초 정보 및 레이아웃 수정
     * 관리자가 실수로 등록한 정보나 좌석의 위치(행/열)를 수정할 때 사용
     */
    public void updateSeatInfo(Long seatId, String buildingName, Integer floor,SpaceType spaceType,
                               String seatNumber, Integer rowIndex, Integer colIndex) {

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 엔티티에 비즈니스 메서드를 추가하여 더티 체킹으로 업데이트 진행
        seat.updateInformation(buildingName, floor, spaceType, seatNumber, rowIndex, colIndex);
    }

    /**
     * FR#9. 시설 상태 제어 기능(점검 중 , 점검으로 인해 좌석 사용 불가)
     */
    public void changeSeatToMaintenance(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 좌석 상태를 MAINTENANCE(점검중)로 강제 변경
        seat.changeToMaintenance();
    }

    /**
     * FR#9. 시설 상태 제어 기능(점검 완료 , 다시 이용 가능)
     */
    public void resolveSeatMaintenance(Long seatId) {

        //PK 값으로 해당 좌석을 찾기 , 없다면 에러 발생
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 좌석 상태를 다시 AVAILABLE(이용 가능)로 원복
        seat.resolveMaintenance();
    }

    /**
     * FR#7 ,  FR#15 . 좌석 사용 시작(단건의 경우 , 스터디 라운지) , 좌석 선택 기능
     */
    public Long startUsingSeat(Long memberId, Long seatId) {

        // 1. 사용하는 회원과 사용할 좌석 엔티티를 DB에서 꺼낸다.
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 2. 사용자가 현재 '사용 중(USING)'인 전체 좌석 개수를 가져온다.
        long activeCount = usageHistoryRepository.countActiveUsageByMemberId(memberId);

        // 현재 이용 중인 좌석이 단 1개라도 있다면 무조건 예약 차단!
        if (activeCount > 0) {
            throw new IllegalStateException("이미 이용 중인 좌석이 있습니다. 기존 좌석을 반납한 후 새로운 좌석을 예약해주세요.");
        }


        // 3. 좌석 사용 처리
        // 만약 이미 누가 쓰고 있거나 고장 났다면 여기서 우리가 엔티티에 적어둔 에러가 발생 (더티채킹으로 상태 변경)
        seat.assignUser();

        // 4. 사용 기록(History)을 생성하고 DB에 저장
        UsageHistory history = UsageHistory.createHistory(member, seat);
        usageHistoryRepository.save(history);

        // 5. 생성된 이용 기록의 ID를 반환
        return history.getId();

    }

    /**
     * FR#7 ,  FR#15. 좌석 사용 시작 (여러 좌석을 한 번에 예약 , 휴게실 , 취식공간) , 좌석 선택 기능
     */
    public void startUsingMultipleSeats(Long memberId, List<Long> seatIds) {

        // 1.예약하려는 주체(Member)를 DB에서 꺼내옵니다. (for문 밖에서 1번만)
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 사용자가 현재 '사용 중(USING)'인 전체 좌석 개수를 가져온다.
        long activeCount = usageHistoryRepository.countActiveUsageByMemberId(memberId);

        // 3. 현재 이용 중인 좌석이 1개라도 있다면 무조건 예약 차단
        if (activeCount > 0) {
            throw new IllegalStateException("이미 이용 중인 좌석이 있습니다. 기존 좌석을 반납한 후 예약해주세요.");
        }

        // 4. 다건 예약은 한 번에 최대 4개까지만 가능
        if (seatIds.size() > 4) {
            throw new IllegalStateException("한 번에 최대 4개의 좌석까지만 예약 가능합니다.");
        }

        //첫번째 좌석의 장소 유형을 저장
        SpaceType firstSpaceType = null;

        // 5. 리스트를 돌면서 순차적으로 예약 처리
        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalStateException("없는 좌석입니다."));

            //  5-1. 스터디 라운지는 다건 예약 불가 (도중에 스터디 라운지 섞이는거 방지)
            if (seat.getSpaceType() == SpaceType.STUDY_LOUNGE) {
                throw new IllegalStateException("다건 예약에는 스터디 라운지를 포함할 수 없습니다.");
            }

            //  5-2.  선택한 좌석들의 장소 유형이 모두 같은지 검증!
            if (firstSpaceType == null) {
                firstSpaceType = seat.getSpaceType(); // 첫 좌석의 유형을 저장
            } else if (firstSpaceType != seat.getSpaceType()) {
                throw new IllegalStateException("서로 다른 장소 유형(휴게실, 취식공간 등)의 좌석을 동시에 예약할 수 없습니다.");
            }

            seat.assignUser();

            UsageHistory history = UsageHistory.createHistory(member, seat);
            usageHistoryRepository.save(history);
        }
    }

    /**
     * FR#8. 좌석 사용 종료(단건 반납 , 스터디 라운지)
     */
    public void stopUsingSeat(Long memberId, Long seatId) {

        // 1. 사용자가 반납하려는 좌석을 꺼낸다.
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));


        // 2. "이 회원이 지금 이 좌석에서 사용 중(USING)인 기록"을 DB에서 찾아옵니다.
        UsageHistory activeHistory = usageHistoryRepository.findActiveHistoryByMemberAndSeat(memberId, seatId)
                .orElseThrow(() -> new IllegalStateException("현재 해당 좌석을 사용 중인 기록이 없습니다."));

        // 3. 좌석 상태를 다시 이용 가능(AVAILABLE)으로 원복
        seat.releaseUser(); // 좌석: USING -> AVAILABLE

        // 4. 기록에 종료 시간을 찍고 상태를 COMPLETED로 바꿉니다. (더티 체킹 발동)
        activeHistory.completeUsage(); // 기록: USING -> COMPLETED 및 종료 시간 기록
    }


    /**
     * FR#8. 좌석 사용 종료(여러 좌석을 한번에 반납 , 휴게실 , 취식공간)
     */
    public void stopUsingMultipleSeats(Long memberId, List<Long> seatIds) {

        for (Long seatId : seatIds) {

            // 1. [검증] 반납하려는 좌석 조회
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

            // 💡 2. [검증] 예약 API와의 일관성: 스터디 라운지는 다건 반납 API로 처리 불가!
            if (seat.getSpaceType() == SpaceType.STUDY_LOUNGE) {
                throw new IllegalStateException("스터디 라운지는 단건 반납을 이용해주세요.");
            }

            // 3. [검증] 해당 회원이 이 좌석에서 사용 중인 기록 조회
            UsageHistory activeHistory = usageHistoryRepository.findActiveHistoryByMemberAndSeat(memberId, seatId)
                    .orElseThrow(() -> new IllegalStateException("현재 해당 좌석을 사용 중인 기록이 없습니다."));

            // 4. [실행] 모든 검증을 통과했으므로 좌석과 기록 상태를 원복 (더티 체킹 발동)
            seat.releaseUser(); // 좌석: USING -> AVAILABLE
            activeHistory.completeUsage(); // 기록: USING -> COMPLETED 및 종료 시간 기록
        }
    }

    /**
     * 좌석 소프트 삭제 기능 (관리자 전용)
     * 엔티티의 @SQLDelete 설정에 의해 물리적 삭제(DELETE) 대신 상태 변경(UPDATE is_deleted = true)으로 동작합니다.
     */
    @Transactional
    public void deleteSeat(Long seatId) {

        // 1. 삭제할 좌석이 DB에 존재하는지 조회 (이미 is_deleted = true 인 것은 @SQLRestriction 덕분에 여기서 안 찾아집니다)
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 삭제된 좌석입니다."));

        // 2. 삭제 실행 (실제로는 UPDATE 쿼리가 날아감)
        seatRepository.delete(seat);
    }


    /**
     * [관리자] 좌석 다중 일괄 등록
     * 여러 개의 좌석 정보를 한 번의 트랜잭션으로 DB에 저장합니다.
     */
    @Transactional
    public void registerSeatsBulk(List<SeatRegisterRequest> requests) {

        // 1. 저장 전 모든 좌석에 대해 검증 먼저 실행!
        // (단 하나라도 중복이 있으면 예외가 터지면서 전체 롤백되어 안전합니다)
        for (SeatRegisterRequest req : requests) {
            validateDuplicateSeat(
                    req.getBuildingName(), req.getFloor(), req.getSpaceType(),
                    req.getSeatNumber(), req.getRowIndex(), req.getColIndex()
            );
        }

        // 2. DTO 리스트를 Seat 엔티티 리스트로 변환
        List<Seat> seatsToSave = requests.stream()
                .map(dto -> Seat.createSeat(
                        dto.getBuildingName(), dto.getFloor(), dto.getSpaceType(),
                        dto.getSeatNumber(), dto.getRowIndex(), dto.getColIndex()
                ))
                .collect(Collectors.toList());

        // 3. 변환된 엔티티 리스트를 DB에 한 번에 꽂아 넣기
        seatRepository.saveAll(seatsToSave);
    }

    /**
     * [관리자] 특정 이용자의 좌석 강제 퇴실(반납) 처리
     */
    public void forceStopUsage(Long historyId) {

        // 1. 강제 종료할 이용 기록 조회
        UsageHistory history = usageHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이용 기록입니다."));

        // 2. 이미 종료된 기록인지 방어 로직 (동시성 및 중복 클릭 방지)
        if (history.getEndTime() != null) {
            throw new IllegalStateException("이미 종료 처리된 이용 기록입니다.");
        }

        // 3. 기록과 연결된 좌석 엔티티 조회
        Seat seat = history.getSeat();

        // 4. 더티 체킹을 활용한 강제 상태 변경
        seat.releaseUser();       // 좌석: USING -> AVAILABLE 로 전환
        history.completeUsage();  // 기록: USING -> COMPLETED 및 종료 시간 스탬프 기록
    }





    /**
     * 좌석 등록 검증 로직
     */
    private void validateDuplicateSeat(String buildingName, Integer floor, SpaceType spaceType, String seatNumber, Integer rowIndex, Integer colIndex) {

        // 좌표(행, 열) 중복 검사 (QueryDSL 호출)
        if (seatRepository.existsSeatAtGrid(buildingName, floor, spaceType, rowIndex, colIndex)) {
            throw new IllegalArgumentException(String.format("해당 위치(%d행 %d열)에는 이미 등록된 좌석이 존재합니다.", rowIndex, colIndex));
        }

        // 좌석 번호(예: A-1) 중복 검사 (QueryDSL 호출)
        if (seatRepository.existsSeatNumber(buildingName, floor, spaceType, seatNumber)) {
            throw new IllegalArgumentException(String.format("해당 공간에 '%s' 번호가 이미 존재합니다.", seatNumber));
        }
    }



}
