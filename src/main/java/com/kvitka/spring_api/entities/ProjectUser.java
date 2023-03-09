package com.kvitka.spring_api.entities;

import com.kvitka.spring_api.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "project_users")
public class ProjectUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_user_id", nullable = false)
    private Long projectUserId;

    @Column(name = "project_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    @Column(name = "is_online")
    private Boolean isOnline;

    @Column(name = "last_online")
    private ZonedDateTime lastOnline;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
