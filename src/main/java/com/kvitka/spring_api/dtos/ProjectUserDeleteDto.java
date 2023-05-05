package com.kvitka.spring_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserDeleteDto {
    private String projectUUID;
    private String username;
}
