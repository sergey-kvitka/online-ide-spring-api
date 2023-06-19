package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.enums.ProjectBuildType;
import com.kvitka.spring_api.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectChangeDto {
    private String name;
    private String description;
    private ProjectType projectType;

    private ProjectBuildType projectBuildType;
    private String groupId;
}
