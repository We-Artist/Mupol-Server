package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.search.Search;
import com.mupol.mupolserver.domain.search.SearchRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.search.SearchResultDto;
import com.mupol.mupolserver.dto.search.SuggestionResultDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
public class SearchService {

    private final SearchRepository searchRepository;
    private final UserService userService;
    private final VideoService videoService;

    public List<HashMap<String, String>> searchUsersByName(String keyword) {
        List<HashMap<String, String>> userList = new ArrayList<>();
        List<User> users = userService.getUsersByUsername(keyword);
        for (User user : users) {
            HashMap<String, String> userMap = new HashMap<>();
            userMap.put("user_id", user.getId().toString());
            userMap.put("username", user.getUsername());
            userMap.put("profile_image", user.getProfileImageUrl());
            userList.add(userMap);
        }
        userList.sort(Comparator.comparingInt(a -> a.get("username").indexOf(keyword)));
        return userList;
    }

    // 제목으로 영상 검색
    public List<HashMap<String, String>> searchVideosByTitle(String keyword) {
        List<Video> videos = videoService.getVideoByTitle(keyword);
        List<HashMap<String, String>> videoHashMapList = getHashMapList(videos);
        videoHashMapList.sort(Comparator.comparing(a -> a.get("title").indexOf(keyword)));
        return videoHashMapList;
    }

    // 악기로 영상 검색
    public List<HashMap<String, String>> searchVideosByInstrument(String keyword) {
        Instrument instrument;
        try {
            instrument = Instrument.valueOf(keyword);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        List<Video> videos = videoService.getVideoByInstrument(instrument);
        return getHashMapList(videos);
    }

    private List<HashMap<String, String>> getHashMapList(List<Video> videos) {
        List<HashMap<String, String>> videoList = new ArrayList<>();
        for (Video video : videos) {
            HashMap<String, String> videoMap = new HashMap<>();
            videoMap.put("video_id", video.getId().toString());
            videoMap.put("title", video.getTitle());
            videoMap.put("thumbnail_url", video.getThumbnailUrl());
            videoList.add(videoMap);
        }
        return videoList;
    }

    // 자동완성: 왼쪽에서부터 일치하는 순서대로 3개 계정만 노출
    public SuggestionResultDto getSuggestion(String keyword) {
        // result.put("instrument", searchVideosByInstrument(keyword));
        List<HashMap<String, String>> users = searchUsersByName(keyword);
        if (users.size() <= 3)
            return SuggestionResultDto.builder()
                    .userList(users)
                    .videoListByTitle(searchVideosByTitle(keyword))
                    .build();
        return SuggestionResultDto.builder()
                .userList(users.subList(0, 3))
                .videoListByTitle(searchVideosByTitle(keyword))
                .build();
    }

    // 검색 결과
    public SearchResultDto getSearchResult(User user, String keyword) {
        SearchResultDto result = SearchResultDto.builder()
                .userList(searchUsersByName(keyword))
                .videoListByTitle(searchVideosByTitle(keyword))
                .build();
        // result.put("instrument", searchVideosByInstrument(keyword));
        searchRepository.save(Search.builder()
                .user(user)
                .keyword(keyword)
                .build());
        return result;
    }

    // 24시간 인기 검색어 3개 가져오기
    public List<String> getHotKeyword() {
        List<String> result = new ArrayList<>();
        List<HashMap<String, String>> resultHashmap = new ArrayList<>();
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = LocalDateTime.now().minusHours(24);
        Optional<List<Search>> searchList = searchRepository.findAllByCreatedAtBetween(start, end);
        if (searchList.isEmpty())
            return Collections.emptyList();

        HashMap<String, Integer> keywordMap = new HashMap<>();
        String keyword;
        for (Search search : searchList.get()) {
            keyword = search.getKeyword();
            keywordMap.merge(keyword, 1, Integer::sum);
        }

        keywordMap.forEach((k, v) -> {
            HashMap<String, String> word = new HashMap<>();
            word.put("keyword", k);
            word.put("count", v.toString());
            resultHashmap.add(word);
        });

        resultHashmap.sort((a, b) -> Integer.parseInt(a.get("count")) > Integer.parseInt(b.get("count")) ? 1 : 0);
        resultHashmap.forEach((a) -> result.add(a.get("keyword")));

        return result;
    }
}
