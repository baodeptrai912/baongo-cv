package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.JobAlertDTO;
import com.example.baoNgoCv.model.dto.JobAlertResponse;
import com.example.baoNgoCv.model.entity.JobAlert;
import com.example.baoNgoCv.model.entity.User;

import java.util.List;

public interface JobAlertService {

    JobAlert save(JobAlert jobAlert);

    JobAlertResponse createJobAlert(JobAlertDTO dto);

    List<JobAlert> getUserJobAlerts(User user);

    void deleteJobAlert(Long alertId);


    JobAlertResponse updateJobAlert(Long id, JobAlertDTO dto) ;
}
