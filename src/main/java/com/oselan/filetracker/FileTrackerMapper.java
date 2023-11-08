package com.oselan.filetracker;


import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",  builder = @Builder(disableBuilder = true))
abstract class FileTrackerMapper {
 
    @Mapping(target="message",source = "error")
    public abstract FileTrackerDTO entityToDTO(FileTracker entity);
}
