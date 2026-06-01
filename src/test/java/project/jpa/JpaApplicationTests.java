package project.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.QMember;
import project.jpa.enums.MemberRole;
import project.jpa.repository.MemberRepository;

import static project.jpa.domain.QMember.*;


@SpringBootTest
@Transactional
class JpaApplicationTests {

	@Autowired
	EntityManager em;

	@Autowired
	MemberRepository memberRepository;


//	@Test
//	@Rollback(value = false)
//	void contextLoads() {
//
//		JPAQueryFactory query = new JPAQueryFactory(em);
//
//		Member createMember = Member.createMember("why1234567", "kk0920", "권태욱", MemberRole.USER);
//
//		memberRepository.save(createMember);
//
//		Member findMember = query
//				.selectFrom(member)
//				.fetchOne();
//
//		Assertions.assertThat(createMember).isEqualTo(findMember);
//
//	}

}
