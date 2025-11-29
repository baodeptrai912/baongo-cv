package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.entity.Education;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.projection.user.EducationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByUser(User user);

    long countByUser(User user);

    @Query("""
                SELECT new com.example.baoNgoCv.jpa.projection.user.EducationDTO(
                    e.id,
                    e.degree,
                    e.institution,
                    e.startDate,
                    e.endDate,
                    e.notes,
                    e.graduated
                )
                FROM Education e
                WHERE e.user.id = :userId
                ORDER BY e.startDate DESC
            """)
    List<EducationDTO> findEducationsByUserId(@Param("userId") Long userId);


}
