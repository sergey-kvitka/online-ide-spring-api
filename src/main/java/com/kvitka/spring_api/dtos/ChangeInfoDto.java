package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.entities.ProjectUser;
import com.kvitka.spring_api.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeInfoDto {
    private UserNamesDto userNames;
    private ZonedDateTime lastChange;

    public static ChangeInfoDto from(ProjectUser projectUser) {
        User user = projectUser.getUser();
        return new ChangeInfoDto(
                new UserNamesDto(
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName()),
                projectUser.getLastChange());
    }
}
