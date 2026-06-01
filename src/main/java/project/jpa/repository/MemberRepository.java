package project.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.jpa.domain.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

}
