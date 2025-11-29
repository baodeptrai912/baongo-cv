package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUser(User user);

}