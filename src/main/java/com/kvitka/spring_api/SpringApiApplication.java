package com.kvitka.spring_api;

import com.kvitka.spring_api.entities.Project;
import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import com.kvitka.spring_api.enums.ProjectType;
import com.kvitka.spring_api.repositories.ProjectFileRepository;
import com.kvitka.spring_api.repositories.ProjectRepository;
import com.kvitka.spring_api.repositories.ProjectUserRepository;
import com.kvitka.spring_api.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.ZonedDateTime;

@Slf4j
@SpringBootApplication
public class SpringApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringApiApplication.class, args);
    }

    @Bean
    public int a(
            ProjectFileRepository projectFileRepository,
            ProjectUserRepository projectUserRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {

//        System.out.println(userRepository.findLastChangeByUserId(2L).orElse(ZonedDateTime.now()));

        if (true) return 1;
        User sergey = userRepository.findByUsername("sergey_kvitka");
        User alla = userRepository.findByUsername("alla_s");

        Project project1 = Project.builder()
                .projectType(ProjectType.PRIVATE)
                .created(ZonedDateTime.now())
                .name("Kvitka Java Project")
                .description("my project (descr.)")
                .build();

        Project project2 = Project.builder()
                .projectType(ProjectType.PRIVATE)
                .created(ZonedDateTime.now())
                .name("Alla java proj")
                .description("meow (description... :) ...)")
                .build();

        project1 = projectRepository.save(project1);
        project2 = projectRepository.save(project2);

        ProjectUser sergeyPU1 = ProjectUser.builder()
                .projectRole(ProjectRole.CREATOR)
                .project(project1)
                .user(sergey)
                .build();
        ProjectUser sergeyPU2 = ProjectUser.builder()
                .projectRole(ProjectRole.EDITOR)
                .project(project2)
                .user(sergey)
                .build();
        ProjectUser allaPU1 = ProjectUser.builder()
                .projectRole(ProjectRole.WATCHER)
                .project(project1)
                .user(alla)
                .build();
        ProjectUser allaPU2 = ProjectUser.builder()
                .projectRole(ProjectRole.CREATOR)
                .project(project2)
                .user(alla)
                .build();
        sergeyPU1 = projectUserRepository.save(sergeyPU1);
        sergeyPU2 = projectUserRepository.save(sergeyPU2);
        allaPU1 = projectUserRepository.save(allaPU1);
        allaPU2 = projectUserRepository.save(allaPU2);

//        assert project1 != null;
//        assert project2 != null;
//        project1.getProjectUsers().add(sergeyPU1);
//        project1.getProjectUsers().add(allaPU1);
//        project2.getProjectUsers().add(sergeyPU2);
//        project2.getProjectUsers().add(allaPU2);
//
//        sergey.getProjectUsers().add(sergeyPU1);
//        alla.getProjectUsers().add(allaPU1);
//        sergey.getProjectUsers().add(sergeyPU2);
//        alla.getProjectUsers().add(allaPU2);
//
//        projectRepository.save(project1);
//        projectRepository.save(project2);
//
//        userRepository.save(sergey);
//        userRepository.save(alla);

        return 1;
    }
}
