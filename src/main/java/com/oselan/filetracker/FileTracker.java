package com.oselan.filetracker;

import java.time.LocalDateTime;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Accessors(chain = true) 
public class FileTracker  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    private String fileName;
    
    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus;
 
    private String fileType;

//    @JdbcTypeCode(SqlTypes.JSON)
    @Type(JsonType.class )
    @Column(columnDefinition = "json")
    private Object params ;
 

    private String filePath;

    private String fileKey;

    private String error;

    private Long executionTime;

    private LocalDateTime creationDateTime;
    
}
