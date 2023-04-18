package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRoleDto {
    private ProjectRole projectRole;
    private List<ProjectRole.Permission> permissions;
}
