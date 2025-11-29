package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.entity.ApplicationStatusHistory;
import com.example.baoNgoCv.jpa.repository.ApplicantRepository;
import com.example.baoNgoCv.jpa.repository.ApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationStatusHistoryService {
    private ApplicationStatusHistoryRepository statusHistoryRepository;
    private ApplicantRepository applicantRepository;


    public List<ApplicationStatusHistory> getStatusHistory(Long applicantId) {
        return statusHistoryRepository.findByApplicantIdOrderByStatusDateAsc(applicantId);
    }

    public ApplicationStatusHistory getCurrentStatus(Long applicantId) {
        return statusHistoryRepository.findByApplicantIdAndIsCurrent(applicantId, true)
                .orElse(null);
    }
}
