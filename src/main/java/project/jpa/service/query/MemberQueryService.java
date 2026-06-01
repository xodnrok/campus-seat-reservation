package project.jpa.service.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.dto.memberapidto.LoginResponse;
import project.jpa.repository.MemberRepository;

@Service
@Transactional(readOnly = true) // 💡 OSIV false 환경에서 읽기 전용 성능 최적화
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;


    /**
     * FR#4. 로그인 검증 및 DTO 즉시 변환 (엔티티 외부 노출 차단)
     */
    public LoginResponse loginCheck(String loginId, String password) {

        // 1. DB에서 아이디와 비밀번호 검증
        Member member = memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 맞지 않습니다."));

        // 2. 트랜잭션이 살아있을 때 완벽하게 DTO로 변환해서 반환
        return new LoginResponse(member.getId(), member.getName(), member.getRole());
    }

    /**
     * 마이페이지/새로고침용 최신 회원 정보 단건 조회
     */
    public LoginResponse findMyLatestInfo(Long memberId) {

        // 1. 세션의 과거 스냅샷이 아닌, DB의 가장 최신 회원 데이터를 꺼냄
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 2. 💡 즉시 DTO로 변환해서 반환
        return new LoginResponse(member.getId(), member.getName(), member.getRole());
    }
}
