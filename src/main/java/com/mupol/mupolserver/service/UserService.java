package com.mupol.mupolserver.service;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.common.CacheKey;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.dto.user.UserResDto;
import com.mupol.mupolserver.service.social.SocialServiceFactory;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final SocialServiceFactory socialServiceFactory;
    private final JwtTokenProvider jwtTokenProvider;

    public boolean validateUsername(String username) {
        boolean isValidCharacter = Pattern.matches("^[가-힣0-9a-zA-Z-]*$", username);
        return 0 < username.length() && username.length() < 11 && isValidCharacter;
    }

    @Cacheable(value = CacheKey.USER_ID, key = "#id.toString()", unless = "#result == null")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(CUserNotFoundException::new);
    }

    public List<UserResDto> getAllUserDtos() {
        List<User> users = userRepository.findAll();
        List<UserResDto> dtoList = new ArrayList<>();
        for(User user: users) {
            dtoList.add(getDto(user));
        }
        return dtoList;
    }

    @Cacheable(value = CacheKey.USER_JWT, key = "#jwt", unless = "#result == null")
    public User getUserByJwt(String jwt) {
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

    public void registerAccessToken(User user, String accessToken) {
        user.setFcmToken(accessToken);
        userRepository.save(user);
    }

    public void quitUser(User user) {
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

    public UserResDto getDto(User user) {
        return UserResDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .bgImageUrl(user.getBgImageUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli())
                .email(user.getEmail())
                .favoriteInstrumentList(user.getFavoriteInstrument())
                .major(user.isMajor())
                .representativeVideoId(user.getRepresentativeVideoId())
                .build();
    }

    @Cacheable(value = CacheKey.USER_KEYWORD, key = "#keyword", unless = "#result == null")
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
}
