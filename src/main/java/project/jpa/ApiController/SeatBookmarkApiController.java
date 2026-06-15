package project.jpa.ApiController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import project.jpa.domain.SeatBookmark;
import project.jpa.dto.bookmarkdto.BookmarkDto;
import project.jpa.dto.memberapidto.MemberApiResponse;
import project.jpa.dto.memberapidto.SessionMember;
import project.jpa.service.SeatBookmarkService;
import project.jpa.service.query.SeatBookmarkQueryService;

@Tag(name = "즐겨찾기 관리 API", description = "즐겨찾기 등록 해제 , 즐겨찾기 목록 조회")
@RestController
@RequiredArgsConstructor
public class SeatBookmarkApiController {

    private final SeatBookmarkService seatBookmarkService;
    private final SeatBookmarkQueryService seatBookmarkQueryService;

    /**
     * FR#14. 자주 찾는 좌석 즐겨찾기 추가  토글(등록 및 해제)
     * 프론트엔드에서 하트 버튼을 누를 때마다 이 API 하나만 호출하면 됩니다.
     */
    @Operation(summary = "회원이 마음에 드는 좌석을 즐겨찾기 가능합니다.", description = "URL로 즐겨찾기할 좌석 ID를 받아옵니다")
    @PostMapping("/api/bookmarks/{seatId}")
    public MemberApiResponse<Boolean> toggleBookmark(
            @PathVariable Long seatId,
            @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember) {

        // 1. 서비스 호출 (알아서 등록이거나 해제로 처리됨)
        boolean isAdded = seatBookmarkService.toggleBookmark(loginMember.getId(), seatId);

        // 2. 결과에 따라 프론트엔드에 띄워줄 알림 메시지를 다르게 설정
        String message = isAdded ? "즐겨찾기에 등록되었습니다." : "즐겨찾기에서 해제되었습니다.";

        // 3. 응답 (data에는 하트 상태를 업데이트할 수 있게 boolean 값을 넘겨줌)
        return MemberApiResponse.success(message, isAdded);
    }

    /**
     * FR#15. 내 즐겨찾기 좌석 목록 조회
     */
    @Operation(summary = "회원이 본인이 등록한 즐겨찾기 좌석을 조회 가능합니다.", description = "세션으로 본인의 즐겨찾기 내역을 조회해 옵니다.")
    @GetMapping("/api/bookmarks/me")
    public MemberApiResponse<Page<BookmarkDto>> getMyBookmarks(
            @SessionAttribute(name = MemberApiController.LOGIN_MEMBER) SessionMember loginMember,
            @PageableDefault(size = 10) Pageable pageable) { // 💡 페이징 파라미터 추가!

        // 쿼리 서비스에서 이미 DTO로 변환이 완료된 Page를 받아온다.
        Page<BookmarkDto> resultPage = seatBookmarkQueryService.findMyBookmarks(loginMember.getId(), pageable);

        return MemberApiResponse.success("즐겨찾기 목록 조회가 완료되었습니다.", resultPage);
    }

}
