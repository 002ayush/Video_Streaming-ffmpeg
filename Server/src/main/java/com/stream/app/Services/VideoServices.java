package com.stream.app.Services;

import com.stream.app.entities.Video;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface VideoServices  {
    //save
    Video save(Video video, MultipartFile file);
    Video getById(String videoId);
    List<Video> getAll();
    void processVideo(String videoId);
}
