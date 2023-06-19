package com.kvitka.spring_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "code_diffs")
public class CodeDifference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_diff_id", nullable = false)
    private Long differenceId;

    @Column(name = "code_before", columnDefinition = "text", nullable = false)
    private String codeBefore;

    @Column(name = "code_after", columnDefinition = "text", nullable = false)
    private String codeAfter;

    @Column(name = "date_before", nullable = false)
    private ZonedDateTime dateBefore;

    @Column(name = "date_after", nullable = false)
    private ZonedDateTime dateAfter;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "project_user_id", nullable = false)
    private ProjectUser projectUser;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "file_id")
    private Long fileId;

}
