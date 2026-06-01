package project.jpa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.jpa.domain.UsageHistory;
import project.jpa.repository.UsageHistoryRepository;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UsageHistoryService {

    private final UsageHistoryRepository usageHistoryRepository;

}
