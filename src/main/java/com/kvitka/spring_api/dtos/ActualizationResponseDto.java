package com.kvitka.spring_api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizationResponseDto {

    private boolean projectExists;

    @JsonProperty("isParticipant")
    private boolean isParticipant;

    private boolean roleChanged;

    private boolean filesAndFoldersChanged;

    private List<ChangeInfoDto> lastChangesInfo;
}
