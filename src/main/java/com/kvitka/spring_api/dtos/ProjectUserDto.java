package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import com.kvitka.spring_api.enums.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserDto {
    private Long userId;
    private Long projectUserId;
    private UserNamesDto userInfo;
    private ProjectRole projectRole;
    private boolean isOnline;
    private ZonedDateTime lastOnline;

    public static ProjectUserDto from(ProjectUser projectUser) {
        User user = projectUser.getUser();
        return ProjectUserDto.builder()
                .userId(user.getUserId())
                .projectUserId(projectUser.getProjectUserId())
                .userInfo(new UserNamesDto(
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName()))
                .projectRole(projectUser.getProjectRole())
                .isOnline(user.getIsOnline())
                .lastOnline(user.getLastOnline())
                .build();
    }
}
