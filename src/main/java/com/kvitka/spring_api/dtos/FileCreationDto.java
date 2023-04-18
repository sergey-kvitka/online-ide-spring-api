package com.kvitka.spring_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileCreationDto {
    private String projectUUID;
    private String contentId;
    private String path;
}
