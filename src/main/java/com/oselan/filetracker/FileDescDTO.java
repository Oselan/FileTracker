package com.oselan.filetracker;

import java.io.FileOutputStream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDescDTO {

    private Long fileTrackerId;
    private FileOutputStream stream;
    private String fileName;    

}
