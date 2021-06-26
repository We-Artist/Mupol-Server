package com.mupol.mupolserver.service;


import com.mupol.mupolserver.domain.playlist.Playlist;
import com.mupol.mupolserver.domain.playlist.PlaylistRepository;
import com.mupol.mupolserver.domain.playlist.PlaylistVideo;
import com.mupol.mupolserver.domain.playlist.PlaylistVideoRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.domain.video.VideoRepository;
import com.mupol.mupolserver.dto.playlist.PlaylistResDto;
import com.mupol.mupolserver.dto.playlist.PlaylistVideoDto;
import com.mupol.mupolserver.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final PlaylistVideoRepository playlistVideoRepository;
    private final VideoRepository videoRepository;

    public PlaylistResDto createPlaylist(User user, String name) {
        Playlist playlist = Playlist.builder()
                .user(user)
                .name(name)
                .build();
        playlistRepository.save(playlist);

        return getSndDto(playlist);
    }

    public Playlist getPlaylist(Long playlistId) {
        return playlistRepository.findById(playlistId).orElseThrow(() -> new IllegalArgumentException("not exist playlist"));
    }

    public List<Playlist> getPlaylists(Long userId) {
        return playlistRepository.findPlaylistByUserId(userId).orElseThrow(() -> new IllegalArgumentException("not exist user"));
    }

    public Playlist updateName(Long playlistId, String name) {
        Playlist playlist = getPlaylist(playlistId);
        playlist.setName(name);
        playlistRepository.save(playlist);

        return playlist;
    }

    public void deletePlaylist(Long playlistId) {
        playlistRepository.deleteById(playlistId);
    }

    public List<Video> getPlaylistVideoes(Long playlistId) {
        List<PlaylistVideo> playlistVideoList = new ArrayList<>();
        playlistVideoList = playlistVideoRepository.findByPlaylistId(playlistId).orElseThrow(() -> new IllegalArgumentException("not exist playlist"));

        List<Video> videoList = new ArrayList<>();
        for (int i = 0; i < playlistVideoList.size(); i++) {
            videoList.add(playlistVideoList.get(i).getVideo());
        }

        return videoList;
    }

    public PlaylistVideoDto addPlaylistVideo(Long playlistId, Long videoId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();

        PlaylistVideo playlistVideo = PlaylistVideo.builder()
                .playlist(playlist)
                .video(video)
                .build();
        playlistVideoRepository.save(playlistVideo);

        return getSndDto(playlistVideo);
    }

    @Transactional
    public void deletePlaylistVideo(Long playlistId, Long videoId) {
        Playlist playlist = playlistRepository.findById(playlistId).orElseThrow();
        Video video = videoRepository.findById(videoId).orElseThrow();
        playlistVideoRepository.deleteByPlaylistIdAndVideoId(playlistId, videoId);
    }

    public PlaylistResDto getSndDto(Playlist snd) {
        PlaylistResDto dto = new PlaylistResDto();

        dto.setId(snd.getId());
        dto.setCreatedAt(TimeUtils.getUnixTimestamp(snd.getCreatedAt()));
        dto.setUpdatedAt(TimeUtils.getUnixTimestamp(snd.getModifiedDate()));
        dto.setUserId(snd.getUser().getId());
        dto.setName(snd.getName());
        dto.setVideoNum(playlistVideoRepository.countByPlaylistId(snd.getId()).orElseThrow());
        return dto;
    }

    public List<PlaylistResDto> getSndDtoList(List<Playlist> playlistList) {
        return playlistList.stream().map(this::getSndDto).collect(Collectors.toList());
    }

    public PlaylistVideoDto getSndDto(PlaylistVideo snd) {
        PlaylistVideoDto dto = new PlaylistVideoDto();

        dto.setId(snd.getId());
        dto.setPlaylistId(snd.getPlaylist().getId());
        dto.setVideoId(snd.getVideo().getId());
        dto.setCreatedAt(TimeUtils.getUnixTimestamp(snd.getCreatedAt()));

        return dto;
    }

    public Integer getSavedVideoCount(Video video) {
        Optional<List<PlaylistVideo>> playlistVideoList = playlistVideoRepository.findAllByVideo(video);
        if (playlistVideoList.isEmpty())
            return 0;
        return playlistVideoList.get().size();
    }

    // TODO: 최적화 될거 같다
    public boolean amISavedVideo(User user, Video video) {
        Optional<List<Playlist>> playlistList = playlistRepository.findPlaylistByUserId(user.getId());
        if (playlistList.isEmpty())
            return false;
        for (Playlist playlist : playlistList.get()) {
            if (playlistVideoRepository.existsPlaylistVideoByPlaylistAndVideo(playlist, video))
                return true;
        }
        return false;
    }

}
