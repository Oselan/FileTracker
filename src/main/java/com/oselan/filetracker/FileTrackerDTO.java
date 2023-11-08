package com.oselan.filetracker;


import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileTrackerDTO  {

    private Long id;

    private String fileName;

    private FileStatus fileStatus;

    private String fileType;

    private LocalDate creationDateTime;

    private long executionTime;

    private String message;
}
