package com.oselan.filetracker;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.oselan.commons.exceptions.ConflictException;
import com.oselan.commons.exceptions.NotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

//@Tag(name = "FileTrackerController", description = "Operations for Files Tracker Status")
@Validated
@Slf4j
public class FileTrackerController {

	@Autowired
	protected FileTrackerService fileTrackerService;

	@Operation(summary = "Retrieve Reports Process details List ")
	@ResponseStatus(HttpStatus.OK)
	@GetMapping()
	ResponseEntity<Page<FileTrackerDTO>> getFileTrackerList(
			@RequestParam(name = "fileStatus", required = false) FileStatus fileStatus,
			@RequestParam(name = "fileType") String fileType, @PageableDefault(page = 0, size = 10) Pageable pageable)
			throws NotFoundException {
		log.info("GET REPORT STATUS LIST  - retrieving Reports process details   ");
		Page<FileTrackerDTO> fileTrackerList = fileTrackerService.getFileTrackerList(fileStatus, fileType, pageable);
		log.info("GET REPORT STATUS LIST  - retrieved  Reports process details ");
		return new ResponseEntity<>(fileTrackerList, HttpStatus.OK);
	}

	@GetMapping("/download/{fileId}")
	@Operation(summary = "Download Generated Report")
	public ResponseEntity<StreamingResponseBody> downloadReport(@PathVariable Long fileId)
			throws NotFoundException, ConflictException, IOException {

		log.info("Download report started report File Id {} , User Id {}- START", fileId);

		FileResponseStreamDTO strBody = fileTrackerService.downloadFile(fileId);

		log.info("Report download finished -  report Name {}. ", strBody.getFileName());

		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + strBody.getFileName())
				.header("filename", strBody.getFileName()).body(strBody.getStream());
	}

}
