package com.kvitka.spring_api.entities;

import com.kvitka.spring_api.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "project_users")
public class ProjectUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_user_id", nullable = false)
    private Long projectUserId;

    @Column(name = "project_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectRole projectRole;

    @Column(name = "last_change")
    private ZonedDateTime lastChange;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "projectUser")
    @ToString.Exclude
    private List<CodeDifference> codeDifferences;
}
