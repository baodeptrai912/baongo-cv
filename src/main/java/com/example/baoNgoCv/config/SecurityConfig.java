package com.example.baoNgoCv.config;

import com.example.baoNgoCv.service.domainService.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import static org.springframework.security.config.Customizer.withDefaults;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://baongocv.info"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Handler chặn người đã đăng nhập truy cập vào trang Login/Register/Forgot Password
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException accessDeniedException) -> {

            if (response.isCommitted()) {
                return;
            }

            String uri = request.getRequestURI();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Nếu user đã login mà cố vào các trang public dành cho khách
            if (isAnonymousEndpoint(uri) && auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getName())) {

                if (hasRole(auth, "ROLE_COMPANY")) {
                    response.sendRedirect("/company/profile");
                } else if (hasRole(auth, "ROLE_USER")) {
                    response.sendRedirect("/jobseeker/profile-update");
                } else {
                    response.sendRedirect("/main/home");
                }
                return;
            }

            response.sendRedirect("/403");
        };
    }

    private boolean isAnonymousEndpoint(String uri) {
        return uri.startsWith("/user/register") ||
                uri.startsWith("/user/login") ||
                uri.startsWith("/user/forget-password") ||
                uri.startsWith("/user/register/company");
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionRegistry sessionRegistry,
                                                   CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                                                   CustomAuthenticationFailureHandler customAuthenticationFailureHandler) throws Exception {

        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http.cors(withDefaults())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/logout-action")
                        .ignoringRequestMatchers("/login-action")
                        .ignoringRequestMatchers("/api/**")
                        .ignoringRequestMatchers("/webhook/**")
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                // ========== PUBLIC ENDPOINTS (BAO GỒM CẢ LOGIN/REGISTER/FORGOT PASS) ==========
                                // Chuyển hết vào đây để tránh lỗi session khi redirect
                                .requestMatchers(
                                        "/main/home",
                                        "/status/**",
                                        "/company/companies",
                                        "/company/api/companies",
                                        "/jobseeker/job-search",
                                        "/jobseeker/load-more-job-search",
                                        "/jobseeker/job-search-live",
                                        "/jobseeker/job-search-api",
                                        "/user/test",
                                        "/company/resend-verification-code",
                                        "/notification/get-notification",
                                        "/webhook/payos",
                                        "/error",
                                        "/jobseeker/job-detail/**",
                                        "/jobseeker/company-detail/**",

                                        // ✅ [FIXED] CÁC URL AUTHENTICATION - CHO PHÉP TẤT CẢ
                                        "/user/login",
                                        "/user/register",
                                        "/user/register/**",
                                        "/user/check-username",
                                        "/user/verify-email", // Hỗ trợ cả GET và POST
                                        "/user/verify-email-for-registration",
                                        "/user/resend-verification-code",

                                        // ✅ [FIXED] FORGOT PASSWORD FLOW
                                        "/user/forget-password",
                                        "/user/forget-password/**", // verify-code, final, send-email-code nằm trong này
                                        "/user/send-email-code-forget-password"
                                ).permitAll()

                                // ========== NOTIFICATION ENDPOINTS ==========
                                .requestMatchers("/notification/**").hasAnyRole("USER", "COMPANY")

                                // ========== JOBSEEKER (USER) ENDPOINTS ==========
                                .requestMatchers(
                                        "/jobseeker/profile",
                                        "/jobseeker/profile-update",
                                        "/jobseeker/education", "/jobseeker/education/**",
                                        "/jobseeker/job-experience", "/jobseeker/job-experience/**",
                                        "/jobseeker/personal-infor-update",
                                        "/jobseeker/update-social-links",
                                        "/jobseeker/update-skills",
                                        "/jobseeker/my-application",
                                        "/jobseeker/apply-job/**",
                                        "/jobseeker/applicant-review-detail/**",
                                        "/jobseeker/withdraw-application/**",
                                        "/jobseeker/job-save",
                                        "/jobseeker/save-job/**",
                                        "/jobseeker/unsave-job/**",
                                        "/jobseeker/job-following",
                                        "/jobseeker/job-alert", "/jobseeker/job-alert/**",
                                        "/jobseeker/account-settings",
                                        "/jobseeker/notification-settings",
                                        "/jobseeker/privacy",
                                        "/jobseeker/account-settings/change-password",
                                        "/jobseeker/verify-password-change",
                                        "/jobseeker/resend-password-code",
                                        "/jobseeker/delete-account/**",
                                        "/jobseeker/export/**",
                                        "/jobseeker/download/**"
                                ).hasRole("USER")

                                // ========== COMPANY ENDPOINTS ==========
                                .requestMatchers(
                                        "/company/update-job-posting-status/",
                                        "/company/profile",
                                        "/company/profile-update",
                                        "/company/company-information-update",
                                        "/company/contact-information-update",
                                        "/company/post-a-job",
                                        "/company/jobposting-managing",
                                        "/company/job-application-list",
                                        "/company/job-application-detail/**",
                                        "/company/job-application/**",
                                        "/company/approve/**",
                                        "/company/reject/**",
                                        "/company/send-review/**",
                                        "/company/update-job-posting/**",
                                        "/company/delete-job-posting/**",
                                        "/company/applicant-viewing/**",
                                        "/company/account-settings",
                                        "/company/account/password-changing/**",
                                        "/company/account/deletion/**",
                                        "/order/**",
                                        "/api/payment/create"
                                ).hasRole("COMPANY")

                                .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler())
                )

                .formLogin(formLogin ->
                        formLogin
                                .loginProcessingUrl("/login-action")
                                .loginPage("/user/login")
                                .defaultSuccessUrl("/notification/all", true)
                                .failureHandler(customAuthenticationFailureHandler)
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .permitAll()
                )

                .logout(logout ->
                        logout
                                .logoutUrl("/logout-action")
                                .logoutSuccessUrl("/main/home")
                                .deleteCookies("JSESSIONID")
                                .invalidateHttpSession(true)
                                .permitAll()
                )

                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionFixation().migrateSession()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/user/login?expired")
                );

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/img/**",
                "/uploads/**",
                "/favicon.png",
                "/robots.txt"
        );
    }
}
