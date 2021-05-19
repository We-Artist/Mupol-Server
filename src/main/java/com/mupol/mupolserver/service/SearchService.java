package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.common.CacheKey;
import com.mupol.mupolserver.domain.instrument.Instrument;
import com.mupol.mupolserver.domain.search.Search;
import com.mupol.mupolserver.domain.search.SearchRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.video.Video;
import com.mupol.mupolserver.dto.search.SearchResultDto;
import com.mupol.mupolserver.dto.search.SearchUserResultDto;
import com.mupol.mupolserver.dto.search.SearchVideoResultDto;
import com.mupol.mupolserver.dto.search.SuggestionResultDto;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@AllArgsConstructor
@Service
public class SearchService {

    private final SearchRepository searchRepository;
    private final UserService userService;
    private final VideoService videoService;

    public List<SearchUserResultDto> searchUsersByName(String keyword) {
        List<SearchUserResultDto> userList = new ArrayList<>();
        List<User> users = userService.getUsersByUsername(keyword);
        for (User user : users) {
            SearchUserResultDto dto = SearchUserResultDto.builder()
                    .username(user.getUsername())
                    .userId(user.getId())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
            userList.add(dto);
        }
        userList.sort(Comparator.comparingInt(a -> a.getUsername().indexOf(keyword)));
        return userList;
    }

    // 제목으로 영상 검색
    public List<SearchVideoResultDto> searchVideosByTitle(String keyword) {
        List<Video> videos = videoService.getVideoByTitle(keyword);
        List<SearchVideoResultDto> videoHashMapList = getVideoDtoList(videos);
        videoHashMapList.sort(Comparator.comparing(a -> a.getTitle().indexOf(keyword)));
        return videoHashMapList;
    }

    // 악기로 영상 검색
    public List<SearchVideoResultDto> searchVideosByInstrument(String keyword) {
        Instrument instrument;
        try {
            instrument = Instrument.valueOf(keyword);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        List<Video> videos = videoService.getVideoByInstrument(instrument);
        return getVideoDtoList(videos);
    }

    private List<SearchVideoResultDto> getVideoDtoList(List<Video> videos) {
        List<SearchVideoResultDto> videoList = new ArrayList<>();
        for (Video video : videos) {
            SearchVideoResultDto dto = SearchVideoResultDto.builder()
                    .title(video.getTitle())
                    .videoId(video.getId())
                    .thumbnailUrl(video.getThumbnailUrl())
                    .build();
            videoList.add(dto);
        }
        return videoList;
    }

    // 자동완성: 왼쪽에서부터 일치하는 순서대로 3개 계정만 노출
    public SuggestionResultDto getSuggestion(String keyword) {
        // result.put("instrument", searchVideosByInstrument(keyword));
        List<SearchUserResultDto> users = searchUsersByName(keyword);
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
        searchRepository.save(Search.builder()
                .user(user)
                .keyword(keyword)
                .build());
        return result;
    }

    // 24시간 인기 검색어 3개 가져오기
    @Cacheable(value = CacheKey.HOT_KEYWORD)
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

        resultHashmap.sort((a, b) -> Integer.compare(Integer.parseInt(b.get("count")), Integer.parseInt(a.get("count"))));
        for(int i=0; i<Math.min(3, resultHashmap.size()); i++) {
            result.add(resultHashmap.get(i).get("keyword"));
        }
        return result;
    }
}

/*

{
  "success": true,
  "msg": "성공했습니다",
  "data": {
    "userList": [
      {
        "profile_image": "https://mupol-test.s3.ap-northeast-2.amazonaws.com/img/65/profile.jpg",
        "user_id": "65",
        "username": "새로운-name1"
      }
    ],
    "videoListByTitle": [
      {
        "thumbnail_url": "https://mupol-test.s3.ap-northeast-2.amazonaws.com/img/65/profile.jpg",
        "video_id": "65",
        "title": "새로운-title"
      }
    ]
  }
}

 */
