package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListItemDto {
    private String projectUUID;
    private String name;
    private ProjectType projectType;
    private String description;
    private ProjectRole projectRole;
    private ZonedDateTime lastProjectOnline;
    private String yourUsername;
    private UserNamesDto creatorInfo;

    public static ProjectListItemDto from(ProjectUser projectUser) {
        Project project = projectUser.getProject();
        User creator = project.getCreator();

        return ProjectListItemDto.builder()
                .projectUUID(project.getProjectUUID())
                .name(project.getName())
                .projectType(project.getProjectType())
                .description(project.getDescription())
                .projectRole(projectUser.getProjectRole())
                .lastProjectOnline(projectUser.getLastOnline())
                .yourUsername(projectUser.getUser().getUsername())
                .creatorInfo(creator == null ? null : new UserNamesDto(
                        creator.getUsername(),
                        creator.getFirstName(),
                        creator.getLastName()))
                .build();
    }
}
