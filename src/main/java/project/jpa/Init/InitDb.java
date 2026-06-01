package project.jpa.Init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatRepository;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;

    /**
     * ApplicationReadyEvent: 스프링 부트 서버가 완전히 켜지고 요청을 받을 준비가 되면 딱 한 번 실행됩니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initTestAdmin() {

        // 1. "admin"이라는 아이디가 DB에 없을 때만 생성합니다.
        // (서버를 껐다 켤 때마다 중복 가입 에러가 터지는 것을 방지)
        if (memberRepository.findByLoginId("admin").isEmpty()) {

            // 2. 권한을 ADMIN으로 세팅해서 엔티티 생성
            Member admin = Member.createMember(
                    "admin",        // 로그인 ID
                    "1234",         // 비밀번호
                    "김조교(관리자)", // 이름
                    MemberRole.ADMIN // 💡 핵심: 권한을 ADMIN으로 부여
            );

            // 3. DB에 저장
            memberRepository.save(admin);

            // 4. 콘솔창에 알림 띄우기 (잘 만들어졌는지 확인용)
            System.out.println("======================================");
            System.out.println("ID : admin / PW : 1234");
            System.out.println("======================================");
        }
        // ==========================================
       // 2. 실시간 좌석 조회용 더미 데이터 생성
       // ==========================================
       // 💡 좌석이 DB에 하나도 없을 때만 데이터를 쏟아붓습니다 (중복 생성 방지)
       if (seatRepository.count() == 0) {

          // 1) 호천관 1층 스터디 라운지 (5행 5열 = 총 25석)
           createSeatGrid("호천관", 1, SpaceType.STUDY_LOUNGE, 5, 5);

            // 2) 호천관 2층 휴게실 (3행 4열 = 총 12석)
           createSeatGrid("호천관", 2, SpaceType.REST_AREA, 3, 4);

          // 3) 배양관 1층 취식 공간 (2행 4열 = 총 8석)
           createSeatGrid("배양관", 1, SpaceType.DINING_AREA, 2, 4);

           System.out.println("======================================");
           System.out.println("테스트용 더미 좌석 " + seatRepository.count() + "개 생성 완료!");
           System.out.println("======================================");
       }
    }

    /**
     *영화관처럼 행(Row)과 열(Col)을 맞춰서 좌석을 쫙 깔아주는 헬퍼 메서드
     */
   private void createSeatGrid(String buildingName, int floor, SpaceType spaceType, int maxRow, int maxCol) {

       for (int r = 1; r <= maxRow; r++) {
           // 행 번호를 알파벳으로 변환 (1 -> A, 2 -> B ...)
           char rowChar = (char) ('A' + r - 1);

           for (int c = 1; c <= maxCol; c++) {
               // 좌석 번호 생성 (예: A-1, B-3)
               String seatNumber = rowChar + "-" + c;

               // 엔티티 생성 및 저장 (좌표값 r, c를 각각 rowIndex, colIndex로 넣음)
              Seat seat = Seat.createSeat(buildingName, floor, spaceType, seatNumber, r, c);
              seatRepository.save(seat);
           }
       }

    }
}
