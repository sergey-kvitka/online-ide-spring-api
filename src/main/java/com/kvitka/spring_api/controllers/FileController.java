package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.FileCreationDto;
import com.kvitka.spring_api.dtos.FileDto;
import com.kvitka.spring_api.entities.ProjectFile;
import com.kvitka.spring_api.services.impl.ProjectFileServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class FileController {

    private final ProjectServiceImpl projectService;
    private final ProjectFileServiceImpl fileService;

    @GetMapping("/{projectUUID}/files")
    public List<FileDto> getFilesByProject(@RequestHeader("Authorization") String bearerToken,
                                           @PathVariable String projectUUID) {
        return projectService.findByUUID(projectUUID)
                .getProjectFiles()
                .stream()
                .map(projectFile -> new FileDto(
                        projectFile.getId(),
                        projectFile.getFileContentId(),
                        projectFile.getPath()))
                .toList();
    }

    @GetMapping("/{projectUUID}/fileContentIds")
    public List<String> getFileContentIdsByProject(@RequestHeader("Authorization") String bearerToken,
                                                   @PathVariable String projectUUID) {
        return projectService.findByUUID(projectUUID)
                .getProjectFiles()
                .stream()
                .map(ProjectFile::getFileContentId)
                .toList();
    }

    @PutMapping("/create")
    public ResponseEntity<Void> createFile(@RequestHeader("Authorization") String bearerToken,
                                           @RequestBody FileCreationDto fileCreationDto) {
        ProjectFile projectFile = new ProjectFile();
        projectFile.setProject(projectService.findByUUID(fileCreationDto.getProjectUUID()));
        projectFile.setPath(fileCreationDto.getPath());
        projectFile.setFileContentId(fileCreationDto.getContentId());
        fileService.save(projectFile);
        return ResponseEntity.ok().build();
    }
}
