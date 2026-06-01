package project.jpa.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.Member;
import project.jpa.domain.UsageHistory;
import project.jpa.enums.MemberRole;
import project.jpa.enums.SpaceType;
import project.jpa.repository.MemberRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class UsageHistoryServiceTest {

    @Autowired
    UsageHistoryService usageHistoryService;

    @Autowired
    SeatService seatService;

    @Autowired
    MemberRepository memberRepository;


}