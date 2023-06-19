package com.kvitka.spring_api.dtos;

import com.kvitka.spring_api.entities.CodeDifference;
import com.kvitka.spring_api.entities.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeDiffDto {
    private String codeBefore;
    private String codeAfter;
    private ZonedDateTime dateBefore;
    private ZonedDateTime dateAfter;
    private UserNamesDto userNames;

    public static CodeDiffDto from(CodeDifference codeDifference) {
        User user = codeDifference.getProjectUser().getUser();
        return new CodeDiffDto(
                codeDifference.getCodeBefore(),
                codeDifference.getCodeAfter(),
                codeDifference.getDateBefore(),
                codeDifference.getDateAfter(),
                new UserNamesDto(
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName()
                ));
    }
}
