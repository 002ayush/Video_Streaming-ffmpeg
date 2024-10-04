package com.stream.app.controllers;


import com.stream.app.Services.VideoServices;
import com.stream.app.Utility;
import com.stream.app.entities.Video;
import com.stream.app.payload.CustomMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.xml.transform.sax.SAXResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {

    @Autowired
    private VideoServices videoServices;


    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());
        System.out.println("Hello World");

        Video savedVideo = videoServices.save(video, file);
        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomMessage.builder()
                    .message("Video not uploaded").success(false).build());
        }

    }


    @GetMapping
    public ResponseEntity<List<Video>> getAll() {
        List<Video> allVideos = videoServices.getAll();
        return ResponseEntity.ok().body(allVideos);

    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable("videoId") String videoId) {
        Video video = videoServices.getById(videoId);
        String contentType = video.getContentType();
        String description = video.getDescription();
        String filePath = video.getFilePath();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);


    }

    //stream the video in chunks
    @GetMapping("/stream/range/{videoId}")
    public ResponseEntity<Resource> streamVideoRange(
            @PathVariable String videoId,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        System.out.println("Ranges " + range);
        Video getVideo = videoServices.getById(videoId);
        Path path = Paths.get(getVideo.getFilePath());
        Resource resource = new FileSystemResource(path);
        //Total file length
        long fileLength = path.toFile().length();
        String contentType = getVideo.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        if (range == null) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
        }
        long rangeStart, rangeEnd;
        String[] ranges = range.replace("bytes=", "").split("-");
        rangeStart = Long.parseLong(ranges[0]);
        if (ranges.length > 1) {

            rangeEnd = Long.parseLong(ranges[1]);
        } else {
            rangeEnd = rangeStart + Utility.CHUNK_SIZE;
        }
        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }
        System.out.println("Range Start = " + rangeStart);
        System.out.println("Range End = " + rangeEnd);
        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);
            long contentLength = rangeEnd - rangeStart + 1;
            byte[] data = new byte[(int) contentLength];
            int read = inputStream.read(data, 0, data.length);
            System.out.println("Range(no of bytes read) in bytes = " + read);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).headers(headers).contentType(MediaType.parseMediaType(contentType)).body(new ByteArrayResource(data));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }


//Using HLS(HTTP Live Streaming) and ffmpeg
//Serve HLS playlist

    @Value("${file.upload.hls}")
    private String HLS_DIR;

    @GetMapping("/{videoId}/master.m3u8")
    public ResponseEntity<Resource> serveMasterFile(@PathVariable String videoId) {
        //Creating Path
        Path path = Paths.get(HLS_DIR, videoId, "master.m3u8");
        System.out.println(path);

        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl").body(resource);
    }

    //Serve the segments
    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegements(
            @PathVariable String videoId,
            @PathVariable String segment

    ) {
        Path path = Paths.get(HLS_DIR, videoId, segment+ ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "video/mp2t").body(resource);
    }
}