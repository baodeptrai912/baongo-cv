package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.entity.JobPosting;
import com.example.baoNgoCv.model.entity.JobSaved;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.repository.JobPostingRepository;
import com.example.baoNgoCv.jpa.repository.JobSavedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobSavedService {

    private final JobPostingRepository jobPostingRepository;
    private final UserServiceImpl userServiceImpl;
    private final JobSavedRepository jobSavedRepository;



    // Kiểm tra công việc đã được lưu cho người dùng
    public boolean isJobSaved(JobPosting jobPosting) {


        User currentUser = userServiceImpl.getCurrentUser();
        if (currentUser == null) {
            return false; // Không tìm thấy người dùng
        }

        // Kiểm tra nếu công việc đã được lưu
        Optional<JobSaved> existingJobSaved = jobSavedRepository.findByJobPostingAndUser(jobPosting, currentUser);
        return existingJobSaved.isPresent(); // Trả về true nếu công việc đã được lưu
    }
}
