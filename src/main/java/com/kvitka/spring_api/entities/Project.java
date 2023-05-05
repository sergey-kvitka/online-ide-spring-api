package com.kvitka.spring_api.entities;

import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.enums.ProjectType;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "project_uuid", nullable = false, unique = true, length = 36)
    private String projectUUID = UUID.randomUUID().toString();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created",
            columnDefinition = "timestamp(6) default = now() not null")
    private ZonedDateTime created;

    @Column(name = "project_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    @Column(name = "description", length = 5000) // * nullable
    private String description;

    @OneToMany(mappedBy = "project")
    @ToString.Exclude
    private List<ProjectUser> projectUsers;

    @OneToMany(mappedBy = "project")
    @ToString.Exclude
    private List<ProjectFile> projectFiles;

    public User getCreator() {
        return getProjectUsers().stream()
                .filter(projectUser -> projectUser.getProjectRole() == ProjectRole.CREATOR)
                .map(ProjectUser::getUser)
                .findFirst()
                .orElse(null);
    }
}
