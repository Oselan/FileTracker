package com.oselan.filetracker;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import com.oselan.commons.exceptions.ConflictException;
import com.oselan.commons.exceptions.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j 
@RequiredArgsConstructor 
class FileTrackerServiceImpl implements FileTrackerService { 
	
    private final AppProperties properties;
   
    private final FileTrackerRepository fileTrackerRepository;
 
    private final FileTrackerMapper fileTrackerMapper;
         
    @Override
    public Page<FileTrackerDTO> getFileTrackerList(  FileStatus fileStatus, String filetype, Pageable pageable) {

        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<FileTracker> fileTrackerPage = fileTrackerRepository.findByFileStatusAndFileType(  fileStatus, filetype, pageable);

        return fileTrackerPage.map(fileTrackerMapper::entityToDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileDescDTO createFileTracker( String fileType,String fileExtension, Object metaData, String uploadedFileName) throws NotFoundException, ConflictException   {

        log.info("Creating file tracker for {}  " , fileType  );
        LocalDateTime startTime = LocalDateTime.now();
        FileTracker fileTracker = new FileTracker().setCreationDateTime(startTime);
    
        // Create the file tracker record
        String fileName = generateFileName(  uploadedFileName!= null ? uploadedFileName : fileType , startTime, fileExtension);

        //check if we have the same file type generate from same userId that has status in progress
        if(fileTrackerRepository.countByFileStatusAndFileType( FileStatus.IN_PROGRESS,fileType ) > 0) {
            log.error("Previously generated report still in progress! ");
            throw new ConflictException("Previously generated report still in progress for current user! ");
        }
        //check if user quota was exceeded
        if (fileTrackerRepository.countByFileStatusAndFileType(FileStatus.ERROR ,fileType) > properties.getFileTracker().getMaxErrorsPerHour()) {
            log.error("User quota exceeded per hour.");
            throw new ConflictException("quota exceeded per hour for current user! .");
        }
         
        String fileKey = Utils.generateRandomAlphaNumericString(6);
        fileTracker
        .setFileName(fileName)
        .setFileStatus(FileStatus.IN_PROGRESS)
        .setFileType(fileType)
        .setParams(metaData)
        .setCreationDateTime(startTime)
        .setFileKey(fileKey);  
        
        
        // Create output stream to file.
        Path path = buildFilePath(fileType, fileName, startTime);
        File file = path.toFile() ; 
        fileTracker.setFilePath(file.getAbsolutePath());
        try {
          fileTracker = fileTrackerRepository.save(fileTracker);
        }catch (DataIntegrityViolationException e) {
          log.error("Error saving tracker" ,e);
          throw new ConflictException(" quota exceeded per minute or file already exists");
        }
        
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            log.error("Failed to create folder for this file type: " + file.getParent());
            throw new ConflictException("Failed to create Report! ");
        }
        
        
        log.info("Opening file {} for writing." , file.getAbsolutePath() );
        try { 
          if (!file.createNewFile()) {
              log.error("Failed to create file {}, already exist: " + file.getAbsolutePath());
              throw new ConflictException("Failed to create Report! ");
          }
          
          FileOutputStream fileOutputStream = new FileOutputStream(file);
          FileDescDTO descDTO = FileDescDTO.builder().fileTrackerId(fileTracker.getId()).fileName(fileName).stream(fileOutputStream).build();
          return descDTO; 
        }catch (IOException  e) {
            log.error("Failed to write to file  " + file.getAbsoluteFile() ,e );
            throw new ConflictException("Failed to create Report!");
        }  
        
    }
    
    @Override
    public FileDescDTO createFileTracker( String fileType, String fileExtension, Object metaData) throws NotFoundException, ConflictException {
        return createFileTracker( fileType, fileExtension, metaData, null);
    }

    @Override
    @Transactional
    @SneakyThrows(NotFoundException.class)
    public void closeFileTracker(  FileDescDTO fileDescDTO, Throwable throwable) 
    { 
       FileTracker  fileTracker  = fileTrackerRepository.findByFileName( fileDescDTO.getFileName())
           .orElseThrow(()->new NotFoundException("File tracker record for file "+ fileDescDTO.getFileName() +" was not found"));
       
       Long executionTimeInSeconds = ChronoUnit.SECONDS.between(fileTracker.getCreationDateTime(), LocalDateTime.now());
       
       FileStatus finalStatus =  (throwable==null ? FileStatus.FINISHED: FileStatus.ERROR) ;

       fileTracker.setExecutionTime(executionTimeInSeconds)
              .setFileStatus(finalStatus);
      
       if (throwable==null)
         fileTrackerRepository.save(fileTracker); 
       else
       {   
          log.warn("Closing file tracker for file {} with error in {} secs", fileDescDTO.getFileName() , executionTimeInSeconds);
          log.error("Closing file tracker for file: " + fileDescDTO.getFileName() , ExceptionUtils.getStackTrace(throwable));
          fileTracker.setError(ExceptionUtils.getStackTrace(throwable)); 
          fileTrackerRepository.save(fileTracker); 
          
          //cleanup close and delete the file
          try {
            if (fileDescDTO.getStream()!=null)
            { 
              fileDescDTO.getStream().close();
            }
            //get file Path
            Path path = buildFilePath( fileTracker.getFileType(),fileTracker.getFileName(),fileTracker.getCreationDateTime()) ;
            // check if the file exists
            Files.deleteIfExists(path);
          }catch (IOException e) {
            log.warn("Failed to close the stream");
          }
         
       } 
       
    }

 
    /**
     * Check if the process is already executed with success status
     * @param fileType
     * @return
     */
    @Override
    public Integer checkForExistingFinishedProcess(String fileType) throws NotFoundException { 
        return fileTrackerRepository.countByFileStatusAndFileType(FileStatus.FINISHED,fileType);
    }

    /**
     *  Purge existing files
     */ 
    @Scheduled(cron = "${app.file-tracker.purge-cron-exp}")
    void purgeGeneratedFile() throws IOException, NotFoundException {
        log.info("purge existing files process start");
        //get retention time, all files that were created after will be deleted
        Integer retentionMonths =  properties.getFileTracker().getRetentionMonths();
        if(retentionMonths==null || retentionMonths == 0){
            log.error("No file retention time was found!");
            return;
        }
        LocalDateTime retentionDateTime = LocalDateTime.now().minusMonths(retentionMonths);
        // get all the files that must be deleted
        List<FileTracker> filesToDelete = fileTrackerRepository.findFilesToPurge(FileStatus.FINISHED,retentionDateTime);
        if(filesToDelete.size() ==0) {
            log.info("No files found to delete, retention Time "+retentionMonths);
            return;
        }

        List<FileTracker> filesDeleted =  new ArrayList<>();
        //delete physical file
        for(FileTracker file : filesToDelete){
            //delete
            if(Files.deleteIfExists(Path.of(file.getFilePath()))) {
                log.info("File successfully deleted, File Name: " + file.getFileName());
                filesDeleted.add(file);
            }
            else
                log.error("File {} could not be deleted, file Path {}",file.getFileName(),Path.of(file.getFilePath()));
        }

        if(filesDeleted.size() != filesToDelete.size()) {
            log.error("Not all files were deleted, delete files {}, total files {}",filesDeleted.size() ,filesToDelete.size());
        }
        // remove rows from db only successful one
        fileTrackerRepository.deleteAll(filesDeleted);

        log.info("purge existing files process end !");

    }

    /***
     * Generates and cleans file name based on file Type
     * @param userId
     * @param reportName
     * @param dateTime
     * @param extension
     * @return
     */
    private String generateFileName(  String reportName, LocalDateTime dateTime , String extension)
    {
      
      String fileName =reportName.trim().replaceAll("\\W","_");
      fileName = fileName.length()> 200 ? fileName.substring(0,200) : fileName;
      String currentDateTime = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
      fileName += currentDateTime ;  
      fileName +=  "." +extension ;
      return fileName;
    }
    
    /***
     * Create the full path including the file name
     * @param fileType
     * @param fileName
     * @param fileDate
     * @return
     */
    private Path buildFilePath(String fileType, String fileName, LocalDateTime fileDate)
    {
      String year = fileDate.format(DateTimeFormatter.ofPattern("yyyy"));
      Path path = Path.of(properties.getFileTracker().getStorePath(),year,fileType,fileName) ;
      return path;
    }
    

    @Override
    public FileResponseStreamDTO downloadFile(Long fileId  ) throws  NotFoundException, ConflictException  {
        FileTracker fileTracker = fileTrackerRepository.findById(fileId )
                .orElseThrow(()-> new NotFoundException("File not found for the current with id " + fileId ));

        //validate file status (should be finished to be able to download)
        validateFileStatusBeforeDownload(fileTracker );
        FileResponseStreamDTO frsDTO = new FileResponseStreamDTO(); 
            
        frsDTO.setFileName(fileTracker.getFileName());
    
        //get file Path
        Path path = buildFilePath( fileTracker.getFileType(),fileTracker.getFileName(),fileTracker.getCreationDateTime()) ;

        // check if the file exists
        if(!Files.exists(path))
            throw new NotFoundException("File "+ path.toString() + " could not be found.");
        
        try{ 
          InputStream targetStream =
                  new DataInputStream(new FileInputStream(path.toFile()));  
          frsDTO.setStream(outputStream->FileCopyUtils.copy(targetStream, outputStream)); 
        }catch (IOException e){
           throw new ConflictException("Failed to read file " + fileTracker.getFileName());
        }
        return frsDTO;

    }
 
    /***
     * Checks if the fileTracker is inprogress or has errors
     * @param fileTracker
     * @param userId
     * @throws NotFoundException
     * @throws NotAllowedException
     */
    private void  validateFileStatusBeforeDownload(FileTracker fileTracker ) throws NotFoundException, ConflictException {
 
        if(fileTracker.getFileStatus() == FileStatus.IN_PROGRESS){
            throw new ConflictException("File " + fileTracker.getFileName() + " not available for download, generation still in progress");
        }

        if(fileTracker.getFileStatus() == FileStatus.ERROR){
            throw new ConflictException("File " + fileTracker.getFileName() + " not available for download, errors on generation: " + fileTracker.getError());
        }

    }
 
}
