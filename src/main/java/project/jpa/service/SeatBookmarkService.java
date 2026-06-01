package project.jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.domain.SeatBookmark;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatBookmarkRepository;
import project.jpa.repository.SeatRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SeatBookmarkService {

    private final SeatBookmarkRepository seatBookmarkRepository;
    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;

    /**
     * FR#14. 즐겨찾기 토글 (등록/해제)
     * @return true: 등록됨, false: 해제됨
     */
    public boolean toggleBookmark(Long memberId, Long seatId) {

        // 1. [검증] 회원과 좌석이 실제로 존재하는지 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 2. [조회] 이미 즐겨찾기 된 상태인지 확인
        Optional<SeatBookmark> existingBookmark =
                seatBookmarkRepository.findBookmarkByMemberAndSeat(memberId, seatId);

        if (existingBookmark.isPresent()) {
            // 3-1. 이미 있다면 -> 해제(삭제)
            seatBookmarkRepository.delete(existingBookmark.get());
            return false;

        } else {
            SeatBookmark bookmark = SeatBookmark.createSeatBookmark(member, seat);
            seatBookmarkRepository.save(bookmark);
            return true;
        }
    }

}
