package com.mupol.mupolserver.service;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.common.CacheKey;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.user.UserResDto;
import com.mupol.mupolserver.service.social.SocialServiceFactory;
import com.mupol.mupolserver.util.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SocialServiceFactory socialServiceFactory;
    private final VideoService videoService;
    private final FollowerService followerService;
    private final JwtTokenProvider jwtTokenProvider;

    public boolean validateUsername(String username) {
        boolean isValidCharacter = Pattern.matches("^[가-힣0-9a-zA-Z-]*$", username);
        return 0 < username.length() && username.length() < 11 && isValidCharacter;
    }

//    @Cacheable(value = CacheKey.USER_ID, key = "#id.toString()", unless = "#result == null")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(CUserNotFoundException::new);
    }

    public List<UserResDto> getAllUserDtos() {
        List<User> users = userRepository.findAll();
        List<UserResDto> dtoList = new ArrayList<>();
        for(User user: users) {
            dtoList.add(getDto(null, user));
            log.info(dtoList.get(dtoList.size()-1).toString());
        }
        return dtoList;
    }

//    @Cacheable(value = CacheKey.USER_JWT, key = "#jwt", unless = "#result == null")
    public User getUserByJwt(String jwt) {
        if(jwt == null) return null;
        String userId = jwtTokenProvider.getUserPk(jwt);
        return userRepository.findById(Long.valueOf(userId)).orElseThrow(CUserNotFoundException::new);
    }

    public User getUserByProviderAndToken(SnsType provider, String accessToken) {
        String snsId = this.getSnsId(provider, accessToken);
        return userRepository.findBySnsIdAndProvider(snsId, provider).orElseThrow(CUserNotFoundException::new);
    }

    public boolean isUserExist(SnsType provider, String accessToken) {
        String snsId = this.getSnsId(provider, accessToken);
        Optional<User> user = userRepository.findBySnsIdAndProvider(snsId, provider);
        return user.isPresent();
    }

    public String getSnsId(SnsType provider, String accessToken) {
        return socialServiceFactory.getService(provider).getSnsId(accessToken);
    }

    public String getEmailFromSocialProfile(SnsType provider, String accessToken) {
        return socialServiceFactory.getService(provider).getEmail(accessToken);
    }

    public void registerFcmToken(User user, String accessToken) {
        user.setFcmToken(accessToken);
        userRepository.save(user);
    }

    public void quitUser(User user) {
        System.out.println(user.getId());
        userRepository.deleteById(user.getId());
    }

    public User setRepresentativeVideoId(Long userId, Long videoId){
        User user = userRepository.findById(userId).orElseThrow();
        user.setRepresentativeVideoId(videoId);
        userRepository.save(user);

        return user;
    }

    public User deleteRepresentativeVideoId(Long userId){
        User user = userRepository.findById(userId).orElseThrow();
        user.setRepresentativeVideoId(null);
        userRepository.save(user);

        return user;
    }

    public UserResDto getDto(User user, User target) {
        return UserResDto.builder()
                .id(target.getId())
                .username(target.getUsername())
                .profileImageUrl(target.getProfileImageUrl())
                .bgImageUrl(target.getBgImageUrl())
                .bio(target.getBio())
                .createdAt(TimeUtils.getUnixTimestamp(target.getCreatedAt()))
                .email(target.getEmail())
                .favoriteInstrumentList(target.getFavoriteInstrument())
                .major(target.isMajor())
                .representativeVideoId(target.getRepresentativeVideoId())
                .videoCount(videoService.getVideos(target.getId()).size())
                .followerCount(followerService.getFollowerList(target).size())
                .followingCount(followerService.getFollowingList(target).size())
                .isFollowing(user != null && followerService.isFollowingUser(user, target))
                .build();
    }

//    @Cacheable(value = CacheKey.USER_KEYWORD, key = "#keyword", unless = "#result == null")
    public List<User> getUsersByUsername(String keyword) {
        Optional<List<User>> users = userRepository.findAllByUsernameContains(keyword);
        if (users.isEmpty()) return Collections.emptyList();
        return users.get();
    }

    @CacheEvict(value = CacheKey.USER_ID, key = "#user.getId().toString()")
    public User save(User user) {
        return userRepository.save(user);
    }

    @CacheEvict(value = CacheKey.USER_ID, key = "#user.getId().toString()")
    public void delete(User user) {
        userRepository.delete(user);
    }

    public void signout(User user) {
        user.setFcmToken(null);
    }
}
