package com.kvitka.spring_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kvitka.spring_api.entities.ProjectFile;
import com.kvitka.spring_api.entities.ProjectFolder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnyFileDto {
    private String path;
    private String id;
    private String contentId;
    @JsonProperty("isFolder")
    private boolean isFolder;

    public static AnyFileDto from(ProjectFile projectFile) {
        return new AnyFileDto(
                projectFile.getPath(),
                "" + projectFile.getId(),
                projectFile.getFileContentId(),
                false
        );
    }

    public static AnyFileDto from(ProjectFolder projectFolder) {
        return new AnyFileDto(
                projectFolder.getPath(),
                "" + projectFolder.getProjectFolderId(),
                null,
                true
        );
    }
}
