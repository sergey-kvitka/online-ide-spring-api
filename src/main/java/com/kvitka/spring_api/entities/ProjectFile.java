package com.kvitka.spring_api.entities;

import jakarta.persistence.*;

@Entity(name = "project_files")
public class ProjectFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_file_id", nullable = false)
    private Long id;

    @Column(name = "path", length = 1000, nullable = false)
    private String path;

    @Column(name = "file_content_id", nullable = true)
    private String fileContentId;

    @ManyToOne
    @JoinColumn(name="project_id", nullable = false)
    private Project project;
}
