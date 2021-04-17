package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.sound.Sound;
import com.mupol.mupolserver.domain.sound.SoundRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.sound.SoundReqDto;
import com.mupol.mupolserver.dto.sound.SoundResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class SoundService {

    private final SoundRepository soundRepository;
    private final S3Service s3Service;
    private final FFmpegService ffmpegService;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${ffmpeg.path.upload}")
    private String fileBasePath;

    public SoundResDto uploadSound(MultipartFile soundFile, User user, SoundReqDto metaData) throws IOException, InterruptedException {
        Sound sound = Sound.builder()
                .bpm(metaData.getBpm())
                .title(metaData.getTitle())
                .user(user)
                .build();
        soundRepository.save(sound);

        Long userId = user.getId();
        Long soundId = sound.getId();

        // split sound
        ffmpegService.splitMedia(soundFile, userId, soundId, FFmpegService.MediaType.Sound);

        // upload splitted sound
        File folder = new File(fileBasePath + userId + "/" + soundId);
        String fileUrl = s3Service.uploadMediaFolder(folder, userId, soundId, FFmpegService.MediaType.Sound);
        sound.setFileUrl(fileUrl);
        soundRepository.save(sound);

        // remove dir
        deleteFolder(new File(fileBasePath + userId));

        return getSndDto(sound);
    }

    public Sound getSound(Long soundId) {
        return soundRepository.findById(soundId).orElseThrow(() -> new IllegalArgumentException("not exist sound"));
    }

    public List<Sound> getSounds(Long userId) {
        return soundRepository.findSoundsByUserId(userId).orElseThrow();
    }

    public Sound updateTitle(Long soundId, String title) {
        Sound sound = getSound(soundId);
        sound.setTitle(title);
        soundRepository.save(sound);

        return sound;
    }

    public void deleteSound(Long userId, Long soundId) {
        s3Service.deleteMedia(userId, soundId, FFmpegService.MediaType.Sound);
        soundRepository.deleteById(soundId);
    }

    public List<SoundResDto> getSndDtoList(List<Sound> soundList) {
        return soundList.stream().map(this::getSndDto).collect(Collectors.toList());
    }

    public SoundResDto getSndDto(Sound snd) {
        SoundResDto dto = new SoundResDto();
        dto.setId(snd.getId());
        dto.setBpm(snd.getBpm());
        dto.setTitle(snd.getTitle());
        dto.setUserId(snd.getUser().getId());
        dto.setFileUrl(snd.getFileUrl());
        dto.setCreatedAt(snd.getCreatedAt());
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
