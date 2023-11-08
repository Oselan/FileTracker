package com.oselan.filetracker;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter 
@ToString
public class AppProperties  implements InitializingBean{
	    
    /***
     * Allows access outside spring contxt 
     */
    private static AppProperties instance; 
    public static AppProperties getInstance() {
 
	    return instance;
	 }
   
	@Override
	public void afterPropertiesSet() throws Exception {
		 instance = this;
	}
    
	/***
	 *  Hierarchy of nest property classes 
	 */
	private FileTrackerProps fileTracker;
 
    @Getter
    @Setter
    @ToString
    public static class FileTrackerProps
    {
         private String storePath;
         private Integer retentionMonths;
         private String purgeCronExp;
         private Integer maxErrorsPerHour;
    }
 
	 
}
