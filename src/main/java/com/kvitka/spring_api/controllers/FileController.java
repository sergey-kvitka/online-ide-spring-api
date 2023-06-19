package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.AnyFileDto;
import com.kvitka.spring_api.dtos.FileCreationDto;
import com.kvitka.spring_api.dtos.FileDto;
import com.kvitka.spring_api.dtos.FolderCreationDto;
import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectFile;
import com.kvitka.spring_api.entities.ProjectFolder;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestController
@RequestMapping("/file")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class FileController {

    @Value("${api.node-mongo.port}")
    private String modeMongoURL;

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProjectServiceImpl projectService;
    private final ProjectUserServiceImpl projectUserService;
    private final ProjectFileServiceImpl fileService;
    private final ProjectFolderServiceImpl folderService;
    private final CodeDifferenceServiceImpl codeDifferenceService;

    @GetMapping("/{projectUUID}/allFilesAndFolders")
    public List<AnyFileDto> getAllFilesAndFolders(@RequestHeader("Authorization") String bearerToken,
                                                  @PathVariable String projectUUID) {
        Project project = projectService.findByUUID(projectUUID);
        return Stream.concat(
                project.getProjectFiles().stream().map(AnyFileDto::from),
                project.getProjectFolders().stream().map(AnyFileDto::from)
        ).toList();
    }

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

    @Transactional
    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createFile(@RequestHeader("Authorization") String bearerToken,
                                                          @RequestBody FileCreationDto fileCreationDto) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        String path = fileCreationDto.getPath();
        if (fileService.existsByPath(path))
            return ResponseEntity.badRequest().body(Map.of("message", "Такой файл уже существует"));
        ProjectFile projectFile = new ProjectFile();
        String projectUUID = fileCreationDto.getProjectUUID();
        projectFile.setProject(projectService.findByUUID(projectUUID));
        projectFile.setPath(path);
        projectFile.setFileContentId(fileCreationDto.getContentId());

        projectUserService.updateLastChange(projectUserService
                .findByUserIdAndProjectUUID(user.getUserId(), projectUUID)
                .getProjectUserId());
        fileService.save(projectFile);
        return ResponseEntity.ok(new HashMap<>());
    }

    @Transactional
    @PutMapping("/createFileByUpload/{projectUUID}")
    public ResponseEntity<Map<String, String>> createFileByUpload(@RequestHeader("Authorization") String bearerToken,
                                                                  @PathVariable("projectUUID") String projectUUID,
                                                                  @RequestParam("file") MultipartFile file,
                                                                  @RequestParam("filepath") String path
    ) {
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8).replaceAll("\r\n", "\n");
        } catch (IOException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Ошибка чтения файла. Возможно, файл имеет неверный формат"));
        }

        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        if (fileService.existsByPath(path))
            return ResponseEntity.badRequest().body(Map.of("message", "Такой файл уже существует"));
        ProjectFile projectFile = new ProjectFile();
        projectFile.setProject(projectService.findByUUID(projectUUID));
        projectFile.setPath(path);
        String contentId = UUID.randomUUID().toString();
        projectFile.setFileContentId(contentId);

        log.info("before send");
        try {
            restTemplate.put(
                    "%s/saveFile".formatted(modeMongoURL),
                    Map.of("id", contentId, "content", content));
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (!e.getStatusCode().is2xxSuccessful()) return ResponseEntity.badRequest()
                    .body(Map.of("message", "Произошла ошибка при сохранении файла"));
        }
        log.info("after send");

        try {
            projectUserService.updateLastChange(projectUserService
                    .findByUserIdAndProjectUUID(user.getUserId(), projectUUID)
                    .getProjectUserId());
            fileService.save(projectFile);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Произошла ошибка при сохранении файла"));
        }

        return ResponseEntity.ok(Map.of("contentId", contentId));
    }

    @Transactional
    @PostMapping("/createFolder")
    public ResponseEntity<Map<String, String>> createFolder(@RequestHeader("Authorization") String bearerToken,
                                                            @RequestBody FolderCreationDto folderCreationDto) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        String path = folderCreationDto.getPath();
        if (folderService.existsByPath(path))
            return ResponseEntity.badRequest().body(Map.of("message", "Такая папка уже существует"));
        ProjectFolder projectFolder = new ProjectFolder();
        String projectUUID = folderCreationDto.getProjectUUID();
        projectFolder.setProject(projectService.findByUUID(projectUUID));
        projectFolder.setPath(path);

        projectUserService.updateLastChange(projectUserService
                .findByUserIdAndProjectUUID(user.getUserId(), projectUUID)
                .getProjectUserId());
        folderService.save(projectFolder);
        return ResponseEntity.ok(new HashMap<>());
    }

    @Transactional
    @PostMapping("/deleteFile/{projectFileId}")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestHeader("Authorization") String bearerToken,
                                                          @PathVariable("projectFileId") Long projectFileId) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        String projectUUID = fileService.findById(projectFileId).getProject().getProjectUUID();

        projectUserService.updateLastChange(projectUserService
                .findByUserIdAndProjectUUID(user.getUserId(), projectUUID)
                .getProjectUserId());
        codeDifferenceService.deleteByFileId(projectFileId);
        fileService.deleteById(projectFileId);
        return ResponseEntity.ok(new HashMap<>());
    }

    @Transactional
    @PostMapping("/deleteFolder/{projectFolderId}")
    public ResponseEntity<Map<String, String>> deleteFolder(@RequestHeader("Authorization") String bearerToken,
                                                            @PathVariable("projectFolderId") Long projectFolderId) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);

        ProjectFolder projectFolder = folderService.findById(projectFolderId);
        String path = projectFolder.getPath();

        Project project = projectFolder.getProject();
        List<Long> fileIdsToDelete = project.getProjectFiles()
                .stream()
                .filter(file -> file.getPath().startsWith(path + "/"))
                .map(ProjectFile::getId)
                .toList();
        List<Long> folderIdsToDelete = project.getProjectFolders()
                .stream()
                .filter(folder -> folder.getPath().startsWith(path + "/"))
                .map(ProjectFolder::getProjectFolderId)
                .collect(Collectors.toList());
        folderIdsToDelete.add(projectFolderId);

        projectUserService.updateLastChange(projectUserService
                .findByUserIdAndProjectUUID(
                        user.getUserId(),
                        folderService.findById(projectFolderId).getProject().getProjectUUID())
                .getProjectUserId());
        codeDifferenceService.deleteByListOfFileIds(fileIdsToDelete);
        fileService.deleteByListOfId(fileIdsToDelete);
        folderService.deleteByListOfId(folderIdsToDelete);

        return ResponseEntity.ok(new HashMap<>());
    }

    @Transactional
    @PostMapping("/editFile/{projectFileId}")
    public ResponseEntity<Map<String, String>> editFile(@RequestHeader("Authorization") String bearerToken,
                                                        @PathVariable("projectFileId") Long projectFileId,
                                                        @RequestBody Map<String, String> body) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        String projectUUID = fileService.findById(projectFileId).getProject().getProjectUUID();

        ProjectFile projectFile = fileService.findById(projectFileId);
        String newPath = body.get("path");
        if (fileService.existsByPath(newPath))
            return ResponseEntity.badRequest().body(Map.of("message", "Такой файл уже существует"));
        projectFile.setPath(newPath);

        projectUserService.updateLastChange(projectUserService
                .findByUserIdAndProjectUUID(user.getUserId(), projectUUID)
                .getProjectUserId());
        fileService.save(projectFile);
        return ResponseEntity.ok(new HashMap<>());
    }

    @Transactional
    @PostMapping("/editFolder/{projectFolderId}")
    public ResponseEntity<Map<String, String>> editFolder(@RequestHeader("Authorization") String bearerToken,
                                                          @PathVariable("projectFolderId") Long projectFolderId,
                                                          @RequestBody Map<String, String> body) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        String projectUUID = folderService.findById(projectFolderId).getProject().getProjectUUID();

        ProjectFolder projectFolder = folderService.findById(projectFolderId);
        String newPath = body.get("path");
        if (folderService.existsByPath(newPath))
            return ResponseEntity.badRequest().body(Map.of("message", "Такой файл уже существует"));
        String oldPath = projectFolder.getPath();

        Project project = projectFolder.getProject();
        List<ProjectFile> filesToSave = project.getProjectFiles()
                .stream()
                .map(file -> {
                    String path = file.getPath();
                    if (!path.startsWith(oldPath + "/")) return null;
                    file.setPath(path.replace(oldPath + "/", newPath + "/"));
                    return file;
                })
                .filter(Objects::nonNull)
                .toList();
        List<ProjectFolder> foldersToSave = project.getProjectFolders()
                .stream()
                .map(folder -> {
                    String path = folder.getPath();
                    if (!path.startsWith(oldPath + "/")) return null;
                    folder.setPath(path.replace(oldPath + "/", newPath + "/"));
                    return folder;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        projectFolder.setPath(newPath);
        foldersToSave.add(projectFolder);

        projectUserService.updateLastChange(projectUserService
                .findByUserIdAndProjectUUID(user.getUserId(), projectUUID)
                .getProjectUserId());
        fileService.saveAll(filesToSave);
        folderService.saveAll(foldersToSave);

        return ResponseEntity.ok(new HashMap<>());
    }

    @GetMapping(value = "/getMultipartFile/{contentId}")
    public ResponseEntity<Map<String, String>> getMultipartFromProjectFile(@PathVariable String contentId) {
        String content = "";
        try {
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(
                    "%s/getFile/%s".formatted(modeMongoURL, contentId),
                    String.class);
            content = Optional.ofNullable(responseEntity.getBody()).orElse("");
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println(e.getStatusCode());
            if (!e.getStatusCode().is2xxSuccessful()) return ResponseEntity.badRequest()
                    .body(Map.of("message", "Произошла ошибка при попытке загрузки файла"));
        }

        return ResponseEntity.ok(Map.of("content", content));
    }
}
