package com.oselan.filetracker;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseStreamDTO {

    private StreamingResponseBody stream;
    private String fileName;
    private String contentType;  
}
