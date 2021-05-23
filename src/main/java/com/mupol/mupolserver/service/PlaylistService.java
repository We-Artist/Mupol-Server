package com.mupol.mupolserver.service;


import com.mupol.mupolserver.domain.playlist.Playlist;
import com.mupol.mupolserver.domain.playlist.PlaylistRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.playlist.PlaylistReqDto;
import com.mupol.mupolserver.dto.playlist.PlaylistResDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;

    public PlaylistResDto createPlaylist(User user, PlaylistReqDto metaData) throws IOException, InterruptedException {

        Playlist playlist = Playlist.builder()
                .user(user)
                .name(metaData.getName())
                .build();
        playlistRepository.save(playlist);

        return getSndDto(playlist);
    }

    public PlaylistResDto getSndDto(Playlist snd) {

        PlaylistResDto dto = new PlaylistResDto();

        dto.setId(snd.getId());
        dto.setCreatedAt(snd.getCreatedAt());
        dto.setUpdatedAt(snd.getModifiedDate());
        dto.setUserId(snd.getUser().getId());
        return dto;
    }

    public List<PlaylistResDto> getSndDtoList(List<Playlist> playlistList) {
        return playlistList.stream().map(this::getSndDto).collect(Collectors.toList());
    }

    public Playlist getPlaylist(Long playlistId) {
        return playlistRepository.findById(playlistId).orElseThrow(() -> new IllegalArgumentException("not exist comment"));
    }

    public List<Playlist> getPlaylists(Long userId) {
        return playlistRepository.findPlaylistByUserId(userId).orElseThrow(()->new IllegalArgumentException("not exist video"));
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

}
