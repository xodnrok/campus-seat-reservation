package project.jpa.ApiController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import project.jpa.annotation.AdminOnly;
import project.jpa.domain.Report;
import project.jpa.dto.memberapidto.MemberApiResponse;
import project.jpa.dto.memberapidto.SessionMember;
import project.jpa.dto.reportapidto.AdminReportDto;
import project.jpa.dto.reportapidto.MyReportDto;
import project.jpa.dto.reportapidto.ReportRequest;
import project.jpa.enums.ReportStatus;
import project.jpa.service.ReportService;
import project.jpa.service.query.ReportQueryService;

import java.time.LocalDateTime;

@Tag(name = "신고 관리 API", description = "시설 고장 신고 , 신고 내역 조회 , 신고 상태 변경 관련 API")
@RestController
@RequiredArgsConstructor
public class ReportApiController {

    private final ReportService reportService;
    private final ReportQueryService reportQueryService;

    // ========================================== //
    // [사용자용] 신고 및 마이페이지 영역
    // ========================================== //

    /**
     * FR#13. 시설 고장 및 불편 신고 접수
     */
    @Operation(summary = "시설 고장 및 불편 신고 접수", description = "신고할 좌석 ID , 신고할 내용을 받아 신고를 접수합니다.")
    @PostMapping("/api/reports")
    public MemberApiResponse<Long> createReport(
            @RequestBody @Valid ReportRequest request,
            @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        Long reportId = reportService.createReport(
                loginMember.getId(),
                request.getSeatId(),
                request.getContent()
        );

        return MemberApiResponse.success("신고가 정상적으로 접수되었으며, 해당 좌석 이용이 종료되었습니다.", reportId);
    }

    /**
     * FR#17. 내 신고 내역 조회 (마이페이지용)
     */
    @Operation(summary = "나의 신고 내역 조회", description = "로그인한 회원의 신고한 내역을 조회합니다.")
    @GetMapping("/api/reports/me")
    public MemberApiResponse<Page<MyReportDto>> getMyReports(
            @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember,
            @PageableDefault(size = 10) Pageable pageable) {

        // 쿼리 서비스에서 이미 변환이 끝난 Page<MyReportDto>를 받아온다.
        Page<MyReportDto> resultPage = reportQueryService.findMyReports(loginMember.getId(), pageable);

        return MemberApiResponse.success("내 신고 내역 조회가 완료되었습니다.", resultPage);
    }

    // ========================================== //
    //  [관리자용] 시설 점검 및 제어 영역
    // ========================================== //

    /**
     * FR#18. 관리자 - 상태별 전체 신고 내역 조회 (접수됨, 수리중, 완료 등)
     */
    @Operation(summary = "관리자가 회원의 신고를 상태별로 전체 조회", description = " RECEIVED(\"접수 대기\")," +
            "    IN_PROGRESS(\"점검 중\")," +
            "    RESOLVED(\"점검 완료\") 중 하나를 받아서 검색을 합니다.")
    @GetMapping("/api/admin/reports")
    @AdminOnly
    public MemberApiResponse<Page<AdminReportDto>> getReportsForAdmin(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 10) Pageable pageable) { // ?status=PENDING 등 쿼리 파라미터 처리

        // 쿼리 서비스에서 이미 변환이 끝난 Page<AdminReportDto>를 받아온다.
        Page<AdminReportDto> resultPage = reportQueryService.findReportsByStatus(status, pageable);

        return MemberApiResponse.success("신고 내역 조회가 완료되었습니다.", resultPage);
    }

    /**
     * FR#9. 관리자 - 수리(점검) 시작 처리
     */
    @Operation(summary = "관리자가 신고에 대해 수리(점검) 시작 ", description = "URL로 신고한 ID를 가져와서 수리를 시작합니다.")
    @PatchMapping("/api/admin/reports/{reportId}/start")
    @AdminOnly
    public MemberApiResponse<String> startRepairing(@PathVariable Long reportId) {

        reportService.startRepairingReport(reportId);

        return MemberApiResponse.success("해당 좌석의 수리 작업을 시작했습니다.", null);
    }

    /**
     * FR#9. 관리자 - 신고 처리 완료 (좌석 이용 가능 원복)
     */
    @Operation(summary = "관리자가 해당 신고를 완료함", description = "URL로 신고한 ID를 가져와서 신고 처리를 완료니다.")
    @PatchMapping("/api/admin/reports/{reportId}/resolve")
    @AdminOnly
    public MemberApiResponse<String> resolveReport(@PathVariable Long reportId) {

        reportService.resolveReport(reportId);

        return MemberApiResponse.success("수리가 완료되어 좌석이 다시 개방되었습니다.", null);
    }

}
