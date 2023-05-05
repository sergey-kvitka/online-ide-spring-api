package com.kvitka.spring_api.repositories;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectUserRepository extends JpaRepository<ProjectUser, Long> {

    boolean existsByProjectAndUser(Project project, User user);

    @Query("select pu from project_users pu where pu.project.projectUUID = :project_uuid and pu.user.userId = :uid")
    ProjectUser findByUserIdAndProjectUUID(@Param("uid") Long userId, @Param("project_uuid") String projectUUID);

    void deleteByProjectUserId(Long projectUserId);

    void deleteByProjectUserIdIn(List<Long> projectUserIds);
}