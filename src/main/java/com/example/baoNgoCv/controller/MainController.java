package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.model.dto.homepage.GetHomePageResponse;
import com.example.baoNgoCv.model.entity.Notification;
import com.example.baoNgoCv.jpa.repository.NotificationRepository;
import com.example.baoNgoCv.service.domainService.JobPostingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;
@Slf4j
@Controller
@RequestMapping("/main")
public class MainController {

    private final JobPostingService jobPostingService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public MainController(JobPostingService jobPostingService, NotificationRepository notificationRepository) {
        this.jobPostingService = jobPostingService;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        log.debug("📞 [HOME] Calling jobPostingService.getHomePageData()");

        GetHomePageResponse homeData = jobPostingService.getHomePageData();

        log.debug("homeData {}",homeData );
        model.addAttribute("homeData", homeData);
        return "main/home";
    }

    @GetMapping("/about")
    public String about() {
        return "main/about";
    }

    @GetMapping("/logout")
    public String logout() {
        return "main/about";
    }

    @GetMapping("/password-changing-success")
    public String passwordChangingSuccess(@RequestParam("idNoti") Long id,Model model) {
        if (id == null) {
            return "status/error";
        }
        Optional<Notification> notifi = notificationRepository.findById(id);
        if (!notifi.isPresent()) {
            return "status/error";
        } else {
            notifi.get().setRead(true);
            notificationRepository.save(notifi.get());
            model.addAttribute("notification", notifi.get());
            return "status/password-changing-success";
        }
    }

}
