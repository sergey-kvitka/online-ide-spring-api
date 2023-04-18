package com.kvitka.spring_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMainInfoDto {
    private UserNamesDto userNames;
    private ZonedDateTime lastChange;
}
