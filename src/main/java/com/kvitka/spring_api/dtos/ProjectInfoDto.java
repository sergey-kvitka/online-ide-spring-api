package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.User;
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
public class ProjectInfoDto {
    private UserNamesDto creatorInfo;
    private String name;
    private String description;
    private ZonedDateTime created;
    private ProjectType projectType;
    private Integer projectFilesAmount;

    public static ProjectInfoDto from(Project project) {
        User creator = project.getCreator();
        return ProjectInfoDto.builder()
                .creatorInfo(new UserNamesDto(
                        creator.getUsername(),
                        creator.getFirstName(),
                        creator.getLastName()))
                .name(project.getName())
                .description(project.getDescription())
                .created(project.getCreated())
                .projectType(project.getProjectType())
                .projectFilesAmount(project.getProjectFiles().size())
                .build();
    }
}
