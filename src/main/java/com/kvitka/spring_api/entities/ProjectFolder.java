package com.kvitka.spring_api.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = "project_folders")
public class ProjectFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_folder_id", nullable = false)
    private Long projectFolderId;

    @Column(name = "path", length = 10000, nullable = false)
    private String path;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}
