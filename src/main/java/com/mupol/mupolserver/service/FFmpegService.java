package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.common.MediaType;
import com.mupol.mupolserver.dto.video.VideoWidthHeightDto;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Service
public class FFmpegService {

    @Value("${ffmpeg.path.base}")
    private String ffmpegPath;

    @Value("${ffmpeg.path.ffprobe}")
    private String ffprobePath;

    @Value("${ffmpeg.path.upload}")
    private String fileBasePath;

    public void splitMedia(String filePath, MediaType mediaType) throws IOException, InterruptedException {
        // 파일 자르기
        ProcessBuilder builder = new ProcessBuilder();
        if (mediaType == MediaType.Video) {
            builder.command(
                    ffmpegPath, "-i", MediaType.Video.getValue(),
                    "-codec:", "copy",
                    "-bsf:v", "h264_mp4toannexb",
                    "-start_number", "0",
                    "-hls_time", "20",
                    "-hls_list_size", "0",
                    "-f", "hls",
                    "video.m3u8"
            );
        } else if (mediaType == MediaType.Sound) {
            builder.command(
                    ffmpegPath, "-i", MediaType.Sound.getValue(),
                    "-c:a", "aac",
                    "-b:a", "64k",
                    "-vn",
                    "-hls_list_size", "0",
                    "-hls_time", "4",
                    "sound.m3u8"
            );
        }
        builder.directory(new File(filePath));
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
        log.info("video split success");
    }

    public String saveTmpFile(MultipartFile mediaFile, MediaType type) throws IOException {
        // 폴더 생성
        String filePath = fileBasePath + UUID.randomUUID() + "/";
        File file = new File(filePath + type.getValue());
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
        return filePath;
    }

    public void createThumbnail(String filePath) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        //썸네일 추출
        builder.command(
                ffmpegPath, "-i", MediaType.Video.getValue(),
                "-ss", "00:00:01",
                "-vcodec", "png",
                "-vframes", "1", "thumbnail.png"
        );

        builder.directory(new File(filePath));
        Process process = builder.start();
        StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
        Executors.newSingleThreadExecutor().submit(streamGobbler);
        int exitCode = process.waitFor();
        assert exitCode == 0;
    }

    public VideoWidthHeightDto getVideoWidthAndHeight(String filePath) throws IOException, InterruptedException {
        log.info("get video ratio");

        // 비율 추출
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(
                ffprobePath,
                "-v", "error",
                "-select_streams",
                "v:0",
                "-show_entries",
                "stream=width,height",
                "-of", "default=nw=1:nk=1 ",
                MediaType.Video.getValue()
        );
        builder.directory(new File(filePath));
        Process process = builder.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        VideoWidthHeightDto dto = VideoWidthHeightDto.builder()
                .width(Long.valueOf(in.readLine()))
                .height(Long.valueOf(in.readLine()))
                .build();

        int exitCode = process.waitFor();
        assert exitCode == 0;

        return dto;
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

    public Long getMediaLength(String filePath, MediaType type) throws IOException {
        log.info(filePath + type.getValue());
        FFprobe ffprobe = new FFprobe(ffprobePath);
        FFmpegProbeResult probeResult = ffprobe.probe(filePath + type.getValue());
        FFmpegFormat format = probeResult.getFormat();
        return Math.round(format.duration);
    }
}