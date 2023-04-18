package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleDto {
    private String username;
    private String projectUUID;
    private ProjectRole newProjectRole;

    public boolean nonNullFields() {
        return username != null && projectUUID != null && newProjectRole != null;
    }
}
