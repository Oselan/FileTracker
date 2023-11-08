package com.oselan.filetracker;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.oselan.commons.exceptions.ConflictException;
import com.oselan.commons.exceptions.NotFoundException;

public interface FileTrackerService   {

	 /***
	  * Gets a pageable list of files 
	  * @param fileStatus
	  * @param filetype
	  * @param Page
	  * @return
	  */
    Page<FileTrackerDTO> getFileTrackerList( FileStatus fileStatus, String filetype, Pageable Page);
    
    /***
     * Creates a new tracker
     * @param fileType
     * @param fileExtension
     * @param metaData
     * @return
     * @throws NotFoundException
     * @throws ConflictException
     */
    FileDescDTO createFileTracker(  String fileType,String fileExtension, Object metaData) throws NotFoundException, ConflictException  ;
    
    /***
     * Creates a new tracker from a file upload
     * @param fileType
     * @param fileExtension
     * @param metaData
     * @param uploadedFileName
     * @return
     * @throws NotFoundException
     * @throws ConflictException
     */
    FileDescDTO createFileTracker(  String fileType,String fileExtension, Object metaData, String uploadedFileName) throws NotFoundException, ConflictException  ;
 
    /**
     * Retrieves a file for download
     * @param fileId
     * @return
     * @throws IOException
     * @throws NotFoundException
     * @throws ConflictException
     */
    FileResponseStreamDTO downloadFile(Long fileId ) throws IOException, NotFoundException, ConflictException ;
    
    /***
     * Closes an file tracker reporting any exceptions
     * @param fileDescDTO
     * @param ex
     * @throws NotFoundException
     */
    void closeFileTracker(  FileDescDTO fileDescDTO, Throwable ex) ;
 
    /***
     * 
     * @param fileType
     * @return
     * @throws NotFoundException
     */
    Integer checkForExistingFinishedProcess(String fileType) throws NotFoundException;

    

}
