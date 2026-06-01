package project.jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.SeatBookmark;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.ReportRepository;
import project.jpa.repository.SeatBookmarkRepository;
import project.jpa.repository.UsageHistoryRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final SeatBookmarkRepository seatBookmarkRepository;
    private final UsageHistoryRepository usageHistoryRepository;

    /**
     * FR#1. 회원 가입
     */
    public Long join(Member member) {

        //1. 중복 회원(LoginId) 검증
        validateDuplicateMember(member.getLoginId());

        //2. DB에 회원 저장
        memberRepository.save(member);

        //3. 저장된 Member의 PK값 리턴
        return member.getId();
    }

    /**
     * FR#2. 회원 탈퇴
     */
    public void deleteMember(Long memberId) {

        // [필수 검증] 현재 사용 중인 좌석이 있다면 탈퇴 차단!
        long activeCount = usageHistoryRepository.countActiveUsageByMemberId(memberId);

        if (activeCount > 0) {
            throw new IllegalStateException("현재 이용 중인 좌석이 있습니다. 좌석을 반납한 후 탈퇴해주세요.");
        }

        //1. PK로 탈퇴할 회원을 조회
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다."));

        //2. member의 pk를 외래키로 사용하는 곳을 먼저 삭제하고 이후 member를 지운다.
        reportRepository.deleteByMemberId(memberId);
        seatBookmarkRepository.deleteByMemberId(memberId);
        usageHistoryRepository.deleteByMemberId(memberId);

        //3. DB에서 회원 삭제
        memberRepository.delete(findMember);
    }

    /**
     * FR#3. 회원 수정
     */
    public void updateMember(Long memberId , String password , String name) {

        //1. PK로 수정할 회원을 조회
        Member findMember = memberRepository.findById(memberId).orElseThrow(() -> new IllegalStateException("존재 하지 않는 회원입니다."));

        //2. 회원의 정보를 수정
        findMember.updateInfo(password, name);
    }

    /**
     * 회원 단건 조회
     */
    @Transactional(readOnly = true)
    public Member findMember(Long memberId) {

        //1. PK로 멤버 조회할때 없다면 에러 발생
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }



    /**
     * 회원가입시 중복 회원 검사(LoginId 중복 검사)
     */
    public void validateDuplicateMember(String loginId) {

        //1. 아이디로 조회후 그 값이 있다면 에러 발생
        memberRepository.findByLoginId(loginId).ifPresent(member -> {
            throw new IllegalStateException("이미 존재하는 로그인 아이디 입니다.");
        });
    }


}
