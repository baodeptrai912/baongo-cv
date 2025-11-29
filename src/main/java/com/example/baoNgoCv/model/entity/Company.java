package com.example.baoNgoCv.model.entity;

import com.example.baoNgoCv.model.enums.BillingCycle;
import com.example.baoNgoCv.model.valueObject.SubscriptionDetails;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.IndustryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import com.example.baoNgoCv.model.dto.company.PostRegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"companyMetric", "followers", "jobs", "loginActivities", "notifications", "companySetting", "permissions"})
public class Company implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 3000)
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "website")
    private String website;

    @Column(name = "company_logo")
    private String companyLogo;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "company_followers",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<User> followers = new HashSet<>();

    @OneToMany(mappedBy = "company",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<LoginActivity> loginActivities = new ArrayList<>();
    @Column(name = "company_size")
    private Integer companySize;

    @OneToMany(mappedBy = "company")
    private List<JobPosting> jobs;

    @Embedded
    @Builder.Default
    private SubscriptionDetails subscriptionDetails = SubscriptionDetails.createFreePlan();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "company_permissions",
            joinColumns = @JoinColumn(name = "company_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @OneToMany(mappedBy = "senderCompany", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CompanySetting companySetting;

    @Enumerated(EnumType.STRING)
    @Column(name = "industry")
    private IndustryType industry;

    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CompanyMetric companyMetric;

    public void updateProfileInformation(IndustryType industry,
                                         Integer companySize,
                                         String description,
                                         String logoUrl) {
        this.industry = industry;
        this.companySize = companySize;
        this.description = description;

        if (logoUrl != null && !logoUrl.trim().isEmpty()) {
            this.companyLogo = logoUrl;
        }

    }

    public void updateContactInformation(String companyName,
                                         String location,
                                         String website,
                                         String contactPhone) {

        if (companyName != null && !companyName.trim().isEmpty()) {
            this.name = companyName.trim();
        }

        if (location != null && !location.trim().isEmpty()) {
            this.location = location.trim();
        }

        if (website != null && !website.trim().isEmpty()) {
            String formattedWebsite = website.trim().toLowerCase();
            if (!formattedWebsite.startsWith("http://") &&
                    !formattedWebsite.startsWith("https://")) {
                formattedWebsite = "https://" + formattedWebsite;
            }
            this.website = formattedWebsite;
        } else {
            this.website = null;
        }

        this.contactPhone = contactPhone != null ? contactPhone.trim() : null;

    }

    public void upgradeSubscription(AccountTier targetTier, BillingCycle billingCycle) {
        this.subscriptionDetails = SubscriptionDetails.upgradeTo(targetTier, billingCycle);
    }

    /**
     * Static factory method to create a new Company entity from a registration request.
     * This encapsulates the creation logic within the entity itself.
     *
     * @param request The registration data transfer object.
     * @param passwordEncoder The password encoder service.
     * @param companyRole The default permission for a new company.
     * @return A fully initialized Company entity, ready to be persisted.
     */
    public static Company createFromRegistrationRequest(PostRegisterRequest request, PasswordEncoder passwordEncoder, Permission companyRole) {
        CompanySetting defaultSettings = new CompanySetting();
        defaultSettings.setEmailOnNewApplicant(true);

        Company newCompany = Company.builder()
                .name(request.name())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .contactEmail(request.contactEmail())
                .location(request.location())
                .subscriptionDetails(SubscriptionDetails.createFreePlan())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .permissions(Set.of(companyRole))
                .companyLogo("/img/default/companyDefaultLogo.png")
                .build();

        newCompany.setCompanySetting(defaultSettings);
        defaultSettings.setCompany(newCompany);

        return newCompany;
    }



}
