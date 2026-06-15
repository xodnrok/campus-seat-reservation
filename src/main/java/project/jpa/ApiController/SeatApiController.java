package project.jpa.ApiController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import project.jpa.annotation.AdminOnly;
import project.jpa.dto.memberapidto.MemberApiResponse;
import project.jpa.dto.seatapidto.SeatDto;
import project.jpa.dto.seatapidto.SeatSearchCondition;
import project.jpa.dto.memberapidto.SessionMember;
import project.jpa.dto.seatapidto.MultipleSeatRequest;
import project.jpa.dto.seatapidto.SeatRegisterRequest;
import project.jpa.dto.usagehistoryapidto.ActiveUserDto;
import project.jpa.service.SeatService;
import project.jpa.service.query.SeatQueryService;

import java.util.List;

@Tag(name = "좌석 관리 API", description = "좌석 검색 , 좌석 예약 , 좌석 반납 , 좌석 등록 수정 삭제 , 좌석 신고 처리")
@RestController
@RequiredArgsConstructor
public class SeatApiController {

    private final SeatService seatService;
    private final SeatQueryService seatQueryService;

    // ========================================== //
    // 1. 조회 영역 (FR#6)
    // ========================================== //

    /**
     * FR#6. [사용자용] 통합공간 및 실시간 좌석 검색(건물이름, 층수 , 장소유형 , 좌석의 상태)
     */
    @Operation(summary = "회원이 통합공간 및 좌석을 검색할 수 있습니다.", description = "건물이름 , 층수 , 장소유형 , 좌석의 상태를 받아서 검색합니다.")
    @GetMapping("/api/seats")
    public MemberApiResponse<List<SeatDto>> searchSeats(@ModelAttribute SeatSearchCondition condition) {

        // GET 요청의 쿼리 파라미터(?buildingName=호천관&floor=7)를 Condition 객체로 자동 매핑
        List<SeatDto> result = seatQueryService.searchSeats(condition);
        return MemberApiResponse.success("좌석 조회가 완료되었습니다.", result);

    }

    /**
     * FR#6. [관리자용] 실시간 좌석 동적 검색 (전체 검색 차단)
     */
    @Operation(summary = "관리자 페이지에서 통합공간 및 좌석을 검색할 수 있습니다.", description = "건물이름 , 층수 , 장소유형 , 좌석의 상태를 받아서 검색합니다.")
    @GetMapping("/api/admin/seats")
    public MemberApiResponse<List<SeatDto>> searchAdminSeats(@ModelAttribute SeatSearchCondition condition) {

        //  관리자는 화면에 리스트로 렌더링되므로, 서버 과부하를 막기 위해 필수 조건을 강제합니다
        if (condition.getBuildingName() == null || condition.getBuildingName().isEmpty()
                || condition.getFloor() == null) {
            throw new IllegalArgumentException("관리자 조회 시 건물명과 층수는 필수입니다.");
        }

        List<SeatDto> result = seatQueryService.searchSeats(condition);
        return MemberApiResponse.success("관리자용 좌석 조회가 완료되었습니다.", result);
    }



    // ========================================== //
    // 2. 사용자 영역 (예약 및 반납 - FR#7, 8, 15)
    // ========================================== //

    /**
     * FR#7. 좌석 예약(단건의 경우) , 좌석 선택 기능
     */
    @Operation(summary = "좌석을 예약 합니다.", description = "URL로 예약하고자 하는 좌석의 ID를 가져옵니다.")
    @PostMapping("/api/seats/{seatId}/start")
    public MemberApiResponse<Long> startSeat(@PathVariable Long seatId,
                                             @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        //SeatService에 Member_PK , Seat_PK 값을 넘기고 좌석 사용 시작
        //이용기록 PK값 반환
        Long historyId = seatService.startUsingSeat(loginMember.getId(), seatId);

        return MemberApiResponse.success("좌석 예약이 완료되었습니다.", historyId);

    }

    /**
     * FR#7. (여러 좌석을 한 번에 예약) , 좌석 선택 기능 추후 그룹스터디방이 생긴다면 사용
     */
    @Operation(summary = "여러 좌석을 예약 합니다.", description = "URL로 예약하고자 하는 좌석의 ID를 가져옵니다.")
    @PostMapping("/api/seats/start-multiple")
    public MemberApiResponse<String> startMultipleSeats(@RequestBody @Valid MultipleSeatRequest dto,
                                                        @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        //SeatService에 Member_PK , Seat_PK리스트를 넘기고 좌석 사용 시작
        seatService.startUsingMultipleSeats(loginMember.getId(), dto.getSeatIds());

        return MemberApiResponse.success("다건 좌석 예약이 완료되었습니다.", null);
    }

    /**
     * FR#8. 좌석 반납(단건 반납)
     */
    @Operation(summary = "좌석을 반납합니다.", description = "URL로 반납하고자 하는 좌석의 ID를 가져옵니다.")
    @PostMapping("/api/seats/{seatId}/stop")
    public MemberApiResponse<String> stopSeat(@PathVariable Long seatId,
                                              @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        //SeatService에 Member_PK , Seat_PK 값을 넘기고 좌석 종료
        seatService.stopUsingSeat(loginMember.getId(), seatId);

        return MemberApiResponse.success("좌석 반납이 완료되었습니다.", null);
    }

    /**
     * FR#8. 좌석 반납(여러 좌석을 한번에 반납)
     */
    @Operation(summary = "여러 좌석을 반납합니다.", description = "URL로 반납하고자 하는 좌석의 ID를 가져옵니다.")
    @PostMapping("/api/seats/stop-multiple")
    public MemberApiResponse<String> stopMultipleSeats(@RequestBody @Valid MultipleSeatRequest dto,
                                                       @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        //SeatService에 Member_PK , Seat_PK리스트를 넘기고 좌석 사용 종료
        seatService.stopUsingMultipleSeats(loginMember.getId(), dto.getSeatIds());

        return MemberApiResponse.success("다건 좌석 반납이 완료되었습니다.", null);
    }

    // ========================================== //
    // 3. 관리자 영역 (제어 및 등록 - FR#9, 11, 12)
    // ========================================== //

    /**
     * FR#9. 고장 신고 처리,시설 상태 제어 기능(점검 중 , 점검으로 인해 좌석 사용 불가)
     */
    @Operation(summary = "관리자가 신고가 들어온 좌석의 상태를 점검중으로 변경합니다.", description = "URL로 점검하고자 하는 좌석의 ID를 가져옵니다.")
    @PutMapping("/api/seats/{seatId}/maintenance")
    @AdminOnly
    public MemberApiResponse<String> changeToMaintenance(@PathVariable Long seatId) {

        //점검해야하는 좌석의 PK값을 넘긴다.
        seatService.changeSeatToMaintenance(seatId);

        return MemberApiResponse.success("좌석이 점검 중 상태로 변경되었습니다.", null);
    }

    /**
     * FR#9. 고장 신고 처리,시설 상태 제어 기능(점검 완료 , 다시 이용 가능)
     */
    @Operation(summary = "관리자가 신고가 들어온 좌석의 상태를 점검 완료로 변경합니다.", description = "URL로 점검완료로 바꾸고자 하는 좌석의 ID를 가져옵니다.")
    @PutMapping("/api/seats/{seatId}/resolve")
    @AdminOnly
    public MemberApiResponse<String> resolveMaintenance(@PathVariable Long seatId) {

        //점검 완료로 바꿀 좌석의 PK값을 넘긴다.
        seatService.resolveSeatMaintenance(seatId);

        return MemberApiResponse.success("좌석이 정상 상태로 복구되었습니다.", null);
    }

    /**
     * FR#11.  좌석 및 시설 기초 정보 등록
     */
    @Operation(summary = "관리자가 새로운 좌석을 등록합니다.", description = "건물이름, 층수 , 공간유형 , 좌석번호 , 행 , 열 값을 받아서 좌석을 만듭니다.")
    @PostMapping("/api/seats/register")
    @AdminOnly
    public MemberApiResponse<Long> registerSeat(@RequestBody @Valid SeatRegisterRequest dto) {

        //dto에서 등록할 좌석정보를 get으로 전부다 넘긴다.
        Long seatId = seatService.registerSeat(
                dto.getBuildingName(), dto.getFloor(), dto.getSpaceType(),
                dto.getSeatNumber(), dto.getRowIndex(), dto.getColIndex()
        );

        return MemberApiResponse.success("새로운 좌석이 등록되었습니다.", seatId);
    }

    /**
     * FR#20. 등록한 좌석을 수정 , 좌석 기초 정보 및 레이아웃 수정 관리자가 실수로 등록한 정보나 좌석의 위치(행/열)를 수정할 때 사용
     */
    @Operation(summary = "관리자가 등록한 좌석을 수정 합니다.", description = "좌석 ID , 건물이름, 층수 , 공간유형 , 좌석번호 , 행 , 열 값을 받아서 좌석을 만듭니다.")
    @PutMapping("/api/seats/{seatId}")
    @AdminOnly
    public MemberApiResponse<String> updateSeat(@PathVariable Long seatId, @RequestBody @Valid SeatRegisterRequest dto) {

        //dto에서 수정할 좌석에 대한 정보를 get으로 넘긴다.
        seatService.updateSeatInfo(
                seatId, dto.getBuildingName(), dto.getFloor(), dto.getSpaceType(),
                dto.getSeatNumber(), dto.getRowIndex(), dto.getColIndex()
        );

        return MemberApiResponse.success("좌석 정보가 수정되었습니다.", null);
    }

    /**
     * FR#21. 등록한 좌석 삭제 기능 (소프트 삭제)
     */
    @Operation(summary = "관리자가 등록한 좌석을 삭제합니다.(소프트삭제)", description = "삭제할 좌석 ID를 URL로 받아옵니다.")
    @DeleteMapping("/api/seats/{seatId}")
    @AdminOnly
    public MemberApiResponse<String> deleteSeat(@PathVariable Long seatId) {

        // 서비스 계층으로 삭제할 좌석의 PK를 넘긴다.
        seatService.deleteSeat(seatId);

        return MemberApiResponse.success("해당 좌석이 성공적으로 삭제되었습니다.", null);
    }

    /**
     * FR#12 [관리자] 다건 좌석 그리드 일괄 등록 API
     * 프론트엔드에서 계산된 좌석 배열(List)을 받아 한 번에 저장
     */
    @Operation(summary = "관리자가 여러개의 좌석을 한번에 등록합니다.", description = "건물이름, 층수 , 공간유형 , 좌석번호 , 행 , 열 값을 받아서 좌석을 만듭니다.")
    @PostMapping("/api/seats/bulk")
    @AdminOnly
    public MemberApiResponse<String> registerSeatsBulk(@RequestBody @Valid List<SeatRegisterRequest> requests) {

        // 1. 방금 만든 서비스 메서드로 리스트를 통째로 넘깁니다.
        seatService.registerSeatsBulk(requests);

        // 2. 성공 메시지에 몇 개의 좌석이 등록되었는지 사이즈를 보여주면 좋습니다.
        String message = "총 " + requests.size() + "개의 좌석이 성공적으로 일괄 등록되었습니다.";
        return MemberApiResponse.success(message, null);
    }

    /**
     * FR#17. [관리자] 실시간 이용자 모니터링 목록 조회 (페이징) API
     */
    @Operation(summary = "관리자가 해당공간에서 좌석을 사용중인 사용자를 확인가능합니다.",
            description = "건물이름, 층수 , 공간유형 , 좌석상태를 입력해서 검색합니다.")
    @GetMapping("/api/admin/monitoring/active-users")
    @AdminOnly
    public MemberApiResponse<Page<ActiveUserDto>> getActiveUsers(
            @ModelAttribute SeatSearchCondition condition ,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<ActiveUserDto> result = seatQueryService.getActiveUsers(condition , pageable);
        return MemberApiResponse.success("실시간 이용자 목록 조회가 완료되었습니다.", result);
    }

    /**
     * FR#17. [관리자] 강제 퇴실(반납) 처리 API
     */
    @Operation(summary = "관리자가 회원의 사용중인 좌석을 강제 종료 시킵니다.", description = "이용기록 ID를 가져와서 처리합니다.")
    @PostMapping("/api/admin/monitoring/{historyId}/force-stop")
    @AdminOnly
    public MemberApiResponse<String> forceStopUsage(@PathVariable Long historyId) {

        seatService.forceStopUsage(historyId);
        return MemberApiResponse.success("해당 좌석이 관리자 권한으로 강제 퇴실 처리되었습니다.", null);
    }


}
