package com.stream.app.Services;

import com.stream.app.entities.Video;
import com.stream.app.repositories.VideoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoServices{
    @Value("${file.upload}")
    String DIR;




    @Value("${file.upload.hls}")
    String HLS_DIR;

    @Autowired

    private VideoRepository videoRepository;



    @PostConstruct
    public void init() throws IOException {
        File file = new File(DIR);
        Files.createDirectories(Paths.get(HLS_DIR));
        if (!file.exists()){
            file.mkdir();
            System.out.println("File created");
        }else{
            System.out.println("File is already created");
        }
    }
    @Override
    public Video save(Video video, MultipartFile file) {

        Path path;
        Video saveVideo;
        try {
            String filename = file.getOriginalFilename();
            String contentType = file.getContentType();
            String name = file.getName();

            InputStream inputStream = file.getInputStream();


            String cleanFileName = StringUtils.cleanPath(filename);
            String cleanFolder = StringUtils.cleanPath(DIR);
            path = Paths.get(cleanFolder, cleanFileName);
            System.out.println("Content Type = " + contentType);
            System.out.println(path);
            System.out.println(inputStream);
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            video.setContentType(contentType);
            video.setFilePath(path.toString());

            saveVideo = videoRepository.save(video);

            //Process the savedVideo and convert it into the segments
            processVideo(saveVideo.getVideoId());
            return saveVideo;

        }
       /* catch (IOException ex) {
            throw  new RuntimeException("Error occurred in Input and Ouput");
        }
        catch (RuntimeException) {
            Files.delete(path);
            videoRepository.deleteById(saveVideo.getVideoId());
        }*/



        catch (IOException e) {
            e.printStackTrace();
            return null;
        }



    }

    @Override
    public Video getById(String videoId) {
        Video byId = videoRepository.getById(videoId);
        return byId;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public void processVideo(String videoId) {
        Video video = this.getById(videoId);
        String filePath = video.getFilePath();

        //Http live streaming directory and it is added....

        Path videoPath = Paths.get(filePath);

        String output360p = HLS_DIR+videoId+"/360p/";
        String output720p = HLS_DIR+videoId+"/720p/";
        String output1080p = HLS_DIR+videoId+"/1080p/";
        try{
            /*Files.createDirectory(Paths.get(output360p));
            Files.createDirectory(Paths.get(output720p));
            Files.createDirectory(Paths.get(output1080p));*/

            //Writing ffmpeg command
            //StringBuilder ffmpegCmd = new StringBuilder();
            /*ffmpegCmd.append("ffmpeg -i ")
                    .append(videoPath.toString())
                    .append(" ")
                    .append()*/
            Path outputPath = Files.createDirectory(Paths.get(HLS_DIR,videoId));
            System.out.println("Uploaded");
           /* String ffmpegCmd = String.format("ffmpeg -i \"%s\" -codec: copy -start_number 0 -hls_time 10 -hls_list_size 0 -hls_segment_filename  \"%s/segment_%03d.ts\"  \"%s/master.m3u8\" ",videoPath,outputPath,outputPath);*/
            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -vf \"scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2,setsar=1:1\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );

            System.out.println("Not Uploaded");
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("cmd.exe","/c",ffmpegCmd);
            //This inheritIO means that the subprocess means the ffmpegCmd  will inherit the standard IO and output and standard error streams from the parent current Process that is Java program application which is created by Java Virtual Machine(JVM) before the execution of the program....
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            // if exitCode = 0 that means the process is successfully executed properly.
            if (exitCode != 0){
                throw  new RuntimeException("Video is not processed properly and cannot be created in the segments....");
            }
        }catch (IOException e){
            throw new RuntimeException("IO exception is occurred");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }






    }
}
