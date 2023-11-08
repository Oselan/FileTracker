package com.oselan;


 
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oselan.commons.exceptions.ConflictException;
import com.oselan.commons.exceptions.NotFoundException;
import com.oselan.filetracker.FileDescDTO;
import com.oselan.filetracker.FileTrackerController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name="Sample Controller",description = "Sample process to generate a file")
@RestController
@RequestMapping("/file-track/")
@Validated
@Slf4j
@AllArgsConstructor
public class SampleController  extends FileTrackerController {

    protected final SampleFileGenerator fileGenerator;
     

    @Operation(summary = "Sample Report Generations") 
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("generate/{file-type}")
    ResponseEntity<Void> startFileGenerationProcess(@PathVariable("file-type") String fileType) throws NotFoundException, ConflictException
    {
        log.info("Generating Rreports started  " ); 
        final FileDescDTO fileDescDTO = fileTrackerService.createFileTracker(fileType, "csv",Map.of("dummy_parameter",1000));
        fileGenerator.generateFile(fileDescDTO.getStream(), "Some dummy parameter value")
        .whenComplete((data,throwable)->{ 
        		log.info("File generation completed " + (throwable!=null? " with error.":" successfully. " + data));
                fileTrackerService.closeFileTracker( fileDescDTO ,throwable); 
        }); 
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
     
}
