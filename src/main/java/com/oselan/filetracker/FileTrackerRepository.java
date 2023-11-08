package com.oselan.filetracker;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface FileTrackerRepository extends JpaRepository<FileTracker,Long> {

   
    Page<FileTracker> findByFileStatusAndFileType( FileStatus fileStatus , String fileType, Pageable page);
   
    @Query(value = "select count(1) from FileTracker f where fileStatus <> :errorStatus and creationDateTime > :createdAfter")
    Integer countByFileStatusNot(FileStatus errorStatus, LocalDateTime createdAfter );
 
    Integer countByFileStatusAndFileType(FileStatus fileStatus,String fileType);
 
    @Query(value = "select f From FileTracker f where fileStatus = :fileStatus and creationDateTime < :createdBefore " )
    List<FileTracker> findFilesToPurge(FileStatus fileStatus,LocalDateTime createdBefore);

	Optional<FileTracker> findByFileName(String fileName);
    
}
