package com.mupol.mupolserver.service;

import com.mupol.mupolserver.advice.exception.CUserNotFoundException;
import com.mupol.mupolserver.config.security.JwtTokenProvider;
import com.mupol.mupolserver.domain.user.SnsType;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.domain.user.UserRepository;
import com.mupol.mupolserver.service.social.SocialServiceFactory;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(CUserNotFoundException::new);
    }

    public List<User> getAllUser() {
        return userRepository.findAll();
    }

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

    public List<User> getUsersByUsername(String keyword) {
        Optional<List<User>> users = userRepository.findAllByUsernameContains(keyword);
        if (users.isEmpty()) return Collections.emptyList();
        return users.get();
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }
}
