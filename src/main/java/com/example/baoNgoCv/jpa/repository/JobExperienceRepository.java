package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.dto.user.JobExperienceDTO;
import com.example.baoNgoCv.model.entity.JobExperience;
import com.example.baoNgoCv.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface JobExperienceRepository extends JpaRepository<JobExperience, Long> {

 @Query("""
    SELECT new com.example.baoNgoCv.model.dto.user.JobExperienceDTO(
        je.id,
        je.jobTitle,
        je.companyName,
        je.startDate,
        je.endDate,
        je.description
    )
    FROM JobExperience je
    WHERE je.user.id = :userId
    ORDER BY je.startDate DESC
""")
 List<JobExperienceDTO> findJobExperiencesByUserId(@Param("userId") Long userId);

  List<JobExperience>  findByUser (User currentUser);

}
