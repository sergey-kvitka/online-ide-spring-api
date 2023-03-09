package com.kvitka.spring_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNamesDto {
    private String username;
    private String firstName;
    private String lastName;
}
