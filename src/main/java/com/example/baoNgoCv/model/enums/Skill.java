package com.example.baoNgoCv.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum định nghĩa một danh sách cố định các kỹ năng trong hệ thống.
 * Mỗi kỹ năng bao gồm tên hiển thị và một lớp CSS icon tương ứng.
 */
@Getter
@RequiredArgsConstructor
public enum Skill {
    JAVASCRIPT("JavaScript", "fab fa-js-square"),
    REACT("React", "fab fa-react"),
    NODE_JS("Node.js", "fab fa-node-js"),
    PYTHON("Python", "fab fa-python"),
    JAVA("Java", "fab fa-java"),
    AWS("AWS", "fab fa-aws"),
    DOCKER("Docker", "fab fa-docker"),
    GIT("Git", "fab fa-git-alt"),
    MYSQL("MySQL", "fas fa-database"),
    BOOTSTRAP("Bootstrap", "fab fa-bootstrap"),
    HTML5("HTML5", "fab fa-html5"),
    CSS3("CSS3", "fab fa-css3-alt"),
    SPRING_BOOT("Spring Boot", "fas fa-leaf"), // Ví dụ thêm

    // Kỹ năng Công nghệ khác
    ANGULAR("Angular", "fab fa-angular"),
    VUE_JS("Vue.js", "fab fa-vuejs"),
    KUBERNETES("Kubernetes", "fas fa-dharmachakra"), // Icon thường dùng cho K8s
    POSTGRESQL("PostgreSQL", "fas fa-database"),

    // Kỹ năng Thiết kế (Design)
    FIGMA("Figma", "fab fa-figma"),
    ADOBE_XD("Adobe XD", "fab fa-adobe"),
    PHOTOSHOP("Photoshop", "fas fa-paint-brush"),

    // Kỹ năng Marketing & Business
    SEO("SEO", "fas fa-chart-line"),
    GOOGLE_ANALYTICS("Google Analytics", "fas fa-chart-pie"),
    PROJECT_MANAGEMENT("Project Management", "fas fa-project-diagram"),
    AGILE("Agile", "fas fa-sync-alt");

    /**
     * Tên hiển thị của kỹ năng (ví dụ: "JavaScript").
     */
    private final String displayName;

    /**
     * Lớp CSS của icon FontAwesome (ví dụ: "fab fa-js-square").
     */
    private final String iconClass;

    public String getName() {
        return this.name(); // Trả về tên của hằng số enum (ví dụ: JAVASCRIPT)
    }
}