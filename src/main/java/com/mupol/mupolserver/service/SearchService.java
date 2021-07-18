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
    private final PlaylistService playlistService;
    private final FollowerService followerService;
    private final CommentService commentService;
    private final LikeService likeService;

    public List<SearchUserResultDto> searchUsersByName(User searchUser, String keyword) {
        List<SearchUserResultDto> userList = new ArrayList<>();
        List<User> users = userService.getUsersByUsername(keyword);
        for (User user : users) {
            SearchUserResultDto dto = SearchUserResultDto.builder()
                    .username(user.getUsername())
                    .id(user.getId())
                    .profileImageUrl(user.getProfileImageUrl())
                    .favoriteInstrumentList(user.getFavoriteInstrument())
                    .followerNumber(user.getFollowers().size())
                    .isFollowing(searchUser != null && followerService.isFollowingUser(searchUser, user))
                    .build();
            userList.add(dto);
        }
        userList.sort(Comparator.comparingInt(a -> a.getUsername().indexOf(keyword)));
        return userList;
    }

    // 제목으로 영상 검색
    public List<SearchVideoResultDto> searchVideosByTitle(User user, String keyword) {
        List<Video> videos = videoService.getVideoByTitle(keyword);
        List<SearchVideoResultDto> videoHashMapList = getVideoDtoList(user, videos);
        videoHashMapList.sort(Comparator.comparing(a -> a.getTitle().indexOf(keyword)));
        return videoHashMapList;
    }

    // 악기로 영상 검색
    public List<SearchVideoResultDto> searchVideosByInstrument(User user, String keyword) {
        Instrument instrument;
        try {
            instrument = Instrument.valueOf(keyword);
        } catch (Exception e) {
            return Collections.emptyList();
        }
        List<Video> videos = videoService.getVideoByInstrument(instrument);
        return getVideoDtoList(user, videos);
    }

    private List<SearchVideoResultDto> getVideoDtoList(User user, List<Video> videos) {
        List<SearchVideoResultDto> videoList = new ArrayList<>();
        for (Video video : videos) {
            SearchVideoResultDto dto = SearchVideoResultDto.builder()
                    .title(video.getTitle())
                    .id(video.getId())
                    .userId(video.getUser().getId())
                    .thumbnailUrl(video.getThumbnailUrl())
                    .likeNum(likeService.getVideoLikeNum(video))
                    .commentNum(commentService.getComments(video.getId()).size())
                    .isLiked(user != null && likeService.isLiked(user, video))
                    .isSaved(user != null && playlistService.amISavedVideo(user, video))
                    .build();
            videoList.add(dto);
        }
        return videoList;
    }

    // 자동완성: 왼쪽에서부터 일치하는 순서대로 3개 계정만 노출
    public SuggestionResultDto getSuggestion(String keyword) {
        // result.put("instrument", searchVideosByInstrument(keyword));
        List<SearchUserResultDto> users = searchUsersByName(null, keyword);
        if (users.size() <= 3)
            return SuggestionResultDto.builder()
                    .userList(users)
                    .videoListByTitle(searchVideosByTitle(null, keyword))
                    .build();
        return SuggestionResultDto.builder()
                .userList(users.subList(0, 3))
                .videoListByTitle(searchVideosByTitle(null, keyword))
                .build();
    }

    // 검색 결과
    public SearchResultDto getSearchResult(User user, String keyword) {
        SearchResultDto result = SearchResultDto.builder()
                .userList(searchUsersByName(user, keyword))
                .videoListByTitle(searchVideosByTitle(user, keyword))
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
        for (int i = 0; i < Math.min(3, resultHashmap.size()); i++) {
            result.add(resultHashmap.get(i).get("keyword"));
        }
        return result;
    }

    public List<String> getHistory(User user) {
        if(user == null) return Collections.emptyList();

        Optional<List<Search>> searchList = searchRepository.findAllByUserOrderByCreatedAt(user);
        if(searchList.isEmpty()) return Collections.emptyList();

        List<String> keywords = new ArrayList<>();
        for (Search s: searchList.get()) {
           keywords.add(s.getKeyword());
           if(keywords.size() == 5) break;
        }
        return keywords;
    }
}
