package project.jpa.ApiController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import project.jpa.domain.UsageHistory;
import project.jpa.dto.memberapidto.MemberApiResponse;
import project.jpa.dto.memberapidto.SessionMember;
import project.jpa.dto.usagehistoryapidto.UsageHistoryDto;
import project.jpa.service.UsageHistoryService;
import project.jpa.service.query.UsageHistoryQueryService;

import java.time.LocalDateTime;

@Tag(name = "이용기록 관리 API", description = "이용 기록 조회")
@RestController
@RequiredArgsConstructor
public class UsageHistoryApiController {

    private final UsageHistoryService usageHistoryService;
    private final UsageHistoryQueryService usageHistoryQueryService;

    /**
     * FR#10. 내 이용 기록 전체 조회 API
     */
    @Operation(summary = "회원이 좌석을 사용한 기록을 조회합니다.", description = "세션을 통해서 본인의 좌석 이용기록을 조회해 옵니다.")
    @GetMapping("/api/usage-history/me")
    public MemberApiResponse<Page<UsageHistoryDto>> getMyUsageHistory(
            @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember,
            @PageableDefault(size = 10) Pageable pageable) {

        // 쿼리 서비스에서 이미 DTO로 변환이 완료된 Page를 깔끔하게 받아옵니다.
        Page<UsageHistoryDto> resultPage = usageHistoryQueryService.findMyUsageHistories(loginMember.getId(), pageable);

        return MemberApiResponse.success("이용 기록 조회가 완료되었습니다.", resultPage);
    }

    /**
     * 마이페이지 메인: 현재 이용 중인 좌석 조회 API
     */
    @Operation(summary = "회원이 현재 사용중인 좌석을 조회합니다.", description = "세션을 통해서 현재 사용중인 좌석을 조회해 옵니다.")
    @GetMapping("/api/usage-history/active")
    public MemberApiResponse<UsageHistoryDto> getMyActiveUsage(
            @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        UsageHistoryDto activeUsage = usageHistoryQueryService.findMyActiveUsage(loginMember.getId());

        if (activeUsage == null) {
            return MemberApiResponse.success("현재 이용 중인 좌석이 없습니다.", null);
        }
        return MemberApiResponse.success("현재 이용 중인 좌석 조회 성공", activeUsage);
    }
}
