package com.kvitka.spring_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveDiffDto {
    private String codeBefore;
    private String codeAfter;
    private ZonedDateTime dateBefore;
    private ZonedDateTime dateAfter;
    private String projectUUID;
    private String fileContentId;
}
