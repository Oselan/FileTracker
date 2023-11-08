package com.oselan.filetracker;

import lombok.Getter;

@Getter 
public enum FileStatus {
  
      IN_PROGRESS("IN_PROGRESS"), FINISHED("FINISHED"), ERROR("ERROR");
      
      private String value; 
      private FileStatus(String value) {
          this.value = value;
      }
      
   
}
