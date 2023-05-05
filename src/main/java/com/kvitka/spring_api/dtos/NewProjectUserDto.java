package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewProjectUserDto {
    private String username;
    private String projectUUID;
    private ProjectRole role;
}
