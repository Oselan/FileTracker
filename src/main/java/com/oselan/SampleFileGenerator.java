package com.oselan;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j 
class SampleFileGenerator { 
	 
	@Async
	public CompletableFuture<String>  generateFile(FileOutputStream stream, String param1)  
	{
		log.info("Writing data to file stream");
		try {
			stream.write("'Record 1','This is a sample file'".getBytes());
		} catch (IOException e) {
			return CompletableFuture.failedFuture(e);
		}
		return CompletableFuture.completedFuture(param1);
	}
}