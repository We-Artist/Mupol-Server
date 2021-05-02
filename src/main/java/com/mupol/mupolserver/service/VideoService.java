package com.mupol.mupolserver.service;

import com.mupol.mupolserver.advice.exception.InstrumentNotExistException;
import com.mupol.mupolserver.domain.common.MediaType;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.dto.video.VideoReqDto;
import com.mupol.mupolserver.dto.video.VideoResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final S3Service s3Service;
    private final FFmpegService ffmpegService;

    @Value("${ffmpeg.path.upload}")
    private String fileBasePath;

    public VideoResDto uploadVideo(MultipartFile videoFile, User user, VideoReqDto metaData) throws IOException, InterruptedException {

        List<String> instruments = metaData.getInstrument_list();
        System.out.println(instruments.toString());
        List<Instrument> instrumentList = new ArrayList<>();

        if (instruments != null) {
            try {
                for (String inst : instruments) instrumentList.add(Instrument.valueOf(inst));
            } catch (Exception e) {
                throw new InstrumentNotExistException();
            }
        }

        Video video = Video.builder()
                .title(metaData.getTitle())
                .origin_title(metaData.getOrigin_title())
                .detail(metaData.getDetail())
                .is_private(metaData.getIs_private())
                .instrument_list(instrumentList)
                .view_num(0)
                .user(user)
                .build();
        videoRepository.save(video);

        Long userId = user.getId();
        Long videoId = video.getId();

        // split video
        ffmpegService.splitMedia(videoFile, userId, videoId, MediaType.Video);

        // upload splitted video
        File folder = new File(fileBasePath + userId + "/" + videoId);
        String fileUrl = s3Service.uploadMediaFolder(folder, userId, videoId, MediaType.Video);
        video.setFileUrl(fileUrl);
        videoRepository.save(video);

        // remove dir
        deleteFolder(new File(fileBasePath + userId));

        return getSndDto(video);
    }

    public Video getVideo(Long videoId) {
        return videoRepository.findById(videoId).orElseThrow(() -> new IllegalArgumentException("not exist video"));
    }

    public List<Video> getVideos(Long userId) {
        return videoRepository.findVideosByUserId(userId).orElseThrow();
    }

    public Video updateTitle(Long videoId, String title) {
        Video video = getVideo(videoId);
        video.setTitle(title);
        videoRepository.save(video);

        return video;
    }

    public void deleteVideo(Long userId, Long videoId) {
        s3Service.deleteMedia(userId, videoId, MediaType.Video);
        videoRepository.deleteById(videoId);
    }

    public List<VideoResDto> getSndDtoList(List<Video> VideoList) {
        return VideoList.stream().map(this::getSndDto).collect(Collectors.toList());
    }

    public VideoResDto getSndDto(Video snd) {

        VideoResDto dto = new VideoResDto();

        dto.setId(snd.getId());
        dto.setTitle(snd.getTitle());
        dto.setOrigin_title(snd.getOrigin_title());
        dto.setDetail(snd.getDetail());
        dto.setIs_private(snd.getIs_private());
        dto.setCreatedAt(snd.getCreatedAt());
        dto.setUpdatedAt(snd.getModifiedDate());
        dto.setFileUrl(snd.getFileUrl());
        dto.setInstrument_list(snd.getInstrument_list());
        dto.setView_num(snd.getView_num());
        dto.setUserId(snd.getUser().getId());
        return dto;
    }

    static void deleteFolder(File file){
        for (File subFile : file.listFiles()) {
            if(subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }
}
