package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.ProfileVisibilityUpdateDTO;
import com.example.baoNgoCv.exception.jobseekerException.UserNotFoundException;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.model.entity.UserSettings;
import com.example.baoNgoCv.jpa.repository.UserRepository;
import com.example.baoNgoCv.jpa.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;


    public void updateProfileVisibility(String username, ProfileVisibilityUpdateDTO dto) {
        User currentUser = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);


        UserSettings settings = userSettingsRepository.findByUser(currentUser)
                .orElseGet(() -> {

                    UserSettings newSettings = new UserSettings(currentUser);

                    newSettings.setEmailOnApplicationUpdate(true);
                    newSettings.setProfilePublic(true);
                    return newSettings;
                });

        if ("PUBLIC".equalsIgnoreCase(dto.getProfileVisibility())) {
            settings.setProfilePublic(true);
        } else if ("PRIVATE".equalsIgnoreCase(dto.getProfileVisibility())) {
            settings.setProfilePublic(false);
        } else {

            throw new IllegalArgumentException("Invalid profileVisibility value: " + dto.getProfileVisibility() + ". Expected PUBLIC or PRIVATE.");
        }

        userSettingsRepository.save(settings);
    }


    public UserSettings getCurrentUserSettings(String username) {
        User currentUser = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);



        return userSettingsRepository.findByUser(currentUser)
                .orElseGet(() -> {
                    UserSettings newSettings = new UserSettings(currentUser);
                    newSettings.setEmailOnApplicationUpdate(true);
                    newSettings.setProfilePublic(true);
                    return userSettingsRepository.save(newSettings);
                });
    }
}