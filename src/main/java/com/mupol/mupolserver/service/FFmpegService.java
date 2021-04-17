package com.mupol.mupolserver.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
public class FFmpegService {

    @Value("${ffmpeg.path.upload}")
    private String fileBasePath;

    public void splitMedia(
            MultipartFile mediaFile,
            Long userId,
            Long mediaId,
            MediaType mediaType
    ) throws IOException, InterruptedException {
        // user_id/(mediaType)_id/ 폴더 생성
        String filePath = fileBasePath + userId + "/" + mediaId + "/";
        File file = new File(filePath + mediaFile.getOriginalFilename());
        log.info(file.getPath());
        if (!file.exists()) {
            if (file.mkdirs()) {
                log.info("File is created!");
            } else {
                log.info("Failed to create File!");
            }
        }

        // 파일 로컬에 저장
        mediaFile.transferTo(file);

        // 파일 자르기
        ProcessBuilder builder = new ProcessBuilder();
        if (mediaType == MediaType.Video) {
            builder.command(
                    "ffmpeg", "-i", mediaFile.getOriginalFilename(),
                    "-codec:", "copy",
                    "-bsf:v", "h264_mp4toannexb",
                    "-start_number", "0",
                    "-hls_time", "20",
                    "-hls_list_size", "0",
                    "-f", "hls",
                    "v.m3u8"
            );
        } else if (mediaType == MediaType.Sound) {
            builder.command(
                    "ffmpeg", "-i", mediaFile.getOriginalFilename(),
                    "-c:a", "aac",
                    "-b:a", "64k",
                    "-vn",
                    "-hls_list_size", "0",
                    "-hls_time", "4",
                    "s.m3u8"
            );
        }
        builder.directory(new File(filePath));
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(consumer);
        }
    }

    @AllArgsConstructor
    public enum MediaType {
        Video("video"),
        Sound("sound");

        private final String value;
    }
}