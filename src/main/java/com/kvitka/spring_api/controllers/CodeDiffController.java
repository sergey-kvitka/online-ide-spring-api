package com.kvitka.spring_api.controllers;

import com.kvitka.spring_api.dtos.CodeDiffDto;
import com.kvitka.spring_api.dtos.SaveDiffDto;
import com.kvitka.spring_api.entities.*;
import com.kvitka.spring_api.security.jwt.JwtTokenProvider;
import com.kvitka.spring_api.services.impl.CodeDifferenceServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectFileServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectServiceImpl;
import com.kvitka.spring_api.services.impl.ProjectUserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/codeDiff")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequiredArgsConstructor
public class CodeDiffController {

    private final JwtTokenProvider jwtTokenProvider;

    private final CodeDifferenceServiceImpl codeDifferenceService;
    private final ProjectServiceImpl projectService;
    private final ProjectUserServiceImpl projectUserService;
    private final ProjectFileServiceImpl fileService;

    @GetMapping("/get/{fileId}")
    public List<CodeDiffDto> getDiffsByFile(@RequestHeader("Authorization") String bearerToken,
                                            @PathVariable Long fileId) {
        ProjectFile projectFile = fileService.findById(fileId);
        Project project = projectFile.getProject();
        return codeDifferenceService.findByProjectAndProjectUser(project, fileId)
                .stream()
                .map(CodeDiffDto::from)
                .sorted(Comparator.comparing(CodeDiffDto::getDateAfter).reversed())
                .toList();
    }


    @PutMapping("/save")
    public Object saveDiff(@RequestHeader("Authorization") String bearerToken,
                           @RequestBody SaveDiffDto saveDiffDto) {
        User user = jwtTokenProvider.getUserByBearerToken(bearerToken);
        String projectUUID = saveDiffDto.getProjectUUID();
        Project project = projectService.findByUUID(projectUUID);
        ProjectUser projectUser = projectUserService.findByUserIdAndProjectUUID(user.getUserId(), projectUUID);

        CodeDifference codeDifference = new CodeDifference();
        codeDifference.setCodeBefore(saveDiffDto.getCodeBefore());
        codeDifference.setCodeAfter(saveDiffDto.getCodeAfter());
        codeDifference.setDateBefore(saveDiffDto.getDateBefore());
        codeDifference.setDateAfter(saveDiffDto.getDateAfter());
        codeDifference.setProjectUser(projectUser);
        codeDifference.setProject(project);
        codeDifference.setFileId(fileService.findByContentId(saveDiffDto.getFileContentId()).getId());

        codeDifferenceService.save(codeDifference);
        projectUserService.updateLastChange(projectUser.getProjectUserId());
        return null;
    }
}
