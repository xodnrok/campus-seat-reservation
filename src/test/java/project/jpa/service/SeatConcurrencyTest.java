package project.jpa.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import project.jpa.domain.Member;
import project.jpa.domain.Seat;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;
import project.jpa.repository.SeatRepository;
import project.jpa.repository.UsageHistoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class SeatConcurrencyTest {

    @Autowired
    private SeatService seatService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private UsageHistoryRepository usageHistoryRepository;

    // 주의: 동시성 테스트는 여러 스레드가 각자의 트랜잭션을 가져야 하므로
    // 테스트 클래스나 메서드에 @Transactional을 붙이면 절대 안됨

    @AfterEach
    void tearDown() {
        // 테스트가 끝난 후 다음 테스트에 영향을 주지 않도록 데이터 깔끔하게 삭제
        usageHistoryRepository.deleteAllInBatch();
        seatRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
//    @DisplayName("100명의 학생이 동시에 동일한 1개의 좌석을 예약하면, 단 1명만 성공하고 99명은 실패(튕김)해야 한다.")
    void optimisticLock_ConcurrencyTest() throws InterruptedException {

        // 1. [Given] 테스트를 위한 더미 데이터 세팅

        // 타겟 좌석 1개 생성 및 저장
        Seat targetSeat = Seat.createSeat("호천관", 1, SpaceType.STUDY_LOUNGE, "A-1", 1, 1);
        seatRepository.save(targetSeat);
        Long targetSeatId = targetSeat.getId();

        // 100명의 학생 회원 생성 및 저장
        int threadCount = 100;
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= threadCount; i++) {
            members.add(Member.createMember(
                    "user" + i, "password", "학생" + i, MemberRole.USER
            ));
        }
        memberRepository.saveAll(members);

        // 2. [When] 멀티스레드를 이용한 동시성 요청 실행

        // 32개의 스레드(일꾼)를 가진 풀(Pool) 생성
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 100개의 요청이 모두 끝날 때까지 메인 스레드를 기다리게 하는 래치(대기표)
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 멀티스레드 환경에서 안전하게 숫자를 세기 위한 카운터 객체
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 100명의 학생이 동시에 예약 버튼을 누르는 상황 시뮬레이션
        for (Member member : members) {
            executorService.submit(() -> {
                try {
                    // 좌석 예약 서비스 로직 호출
                    seatService.startUsingSeat(member.getId(), targetSeatId);

                    // 예외가 터지지 않고 여기까지 코드가 도달했다면 예약 성공
                    successCount.incrementAndGet();

                } catch (ObjectOptimisticLockingFailureException e) {
                    // JPA 낙관적 락(@Version) 충돌이 발생하면 이 예외가 터짐 (튕겨나간 99명)
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    // 다른 이유(예: 이미 이용 중인 좌석 등)로 실패한 경우
                    failCount.incrementAndGet();
                } finally {
                    // 성공하든 실패하든, 현재 스레드의 작업이 끝났음을 래치에 알림 (count - 1)
                    latch.countDown();
                }
            });
        }

        // 100개의 스레드 작업이 모두 끝날 때(latch가 0이 될 때)까지 메인 스레드는 대기
        latch.await();

        // 3. [Then] 결과 검증 (Assert)

        System.out.println("======================================");
        System.out.println(" 예약 성공 횟수 = " + successCount.get());
        System.out.println(" 예약 실패 횟수 (락 충돌) = " + failCount.get());
        System.out.println("======================================");

        // 100명이 동시에 접근했지만 락(Lock) 덕분에 정확히 1명만 성공해야 함
        Assertions.assertThat(successCount.get()).isEqualTo(1);

        // 나머지 99명은 무조건 실패(예외 발생)해야 함
        Assertions.assertThat(failCount.get()).isEqualTo(99);
    }
}
