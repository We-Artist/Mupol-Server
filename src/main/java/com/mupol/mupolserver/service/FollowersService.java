package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.followers.Followers;
import com.mupol.mupolserver.domain.followers.FollowersRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.user.FollowersResDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class FollowersService {
    private final FollowersRepository followersRepository;

    public Followers getFollowersByFromAndTo(User follower, User following) {
        return followersRepository.findFollowersByFromAndTo(follower, following)
                .orElseThrow(() -> new IllegalArgumentException("you does not follow this user"));
    }

    public boolean isAlreadyFollowed(User follower, User following) {
        return followersRepository.findFollowersByFromAndTo(follower, following).isPresent();
    }

    public List<Followers> getFollowersList(User user) {
        return followersRepository.findAllByTo(user)
                .orElseThrow(() -> new IllegalArgumentException("there is no follower"));
    }

    public List<Followers> getFollowingList(User user) {
        return followersRepository.findAllByFrom(user)
                .orElseThrow(() -> new IllegalArgumentException("there is no following"));
    }

    public List<FollowersResDto> getFollowersDtoList(List<Followers> followersList) {
        List<FollowersResDto> dtoList = new ArrayList<>();

        for(Followers f: followersList) {
            dtoList.add(FollowersResDto.builder()
                    .userId(f.getFrom().getId())
                    .username(f.getFrom().getUsername())
                    .profileImageUrl(f.getFrom().getProfileImageUrl())
                    .build());
        }

        return dtoList;
    }

    public List<FollowersResDto> getFollowingDtoList(List<Followers> followersList) {
        List<FollowersResDto> dtoList = new ArrayList<>();

        for(Followers f: followersList) {
            dtoList.add(FollowersResDto.builder()
                    .userId(f.getTo().getId())
                    .username(f.getTo().getUsername())
                    .profileImageUrl(f.getTo().getProfileImageUrl())
                    .build());
        }

        return dtoList;
    }

    public Followers save(Followers followers) {
        return followersRepository.save(followers);
    }

    public void delete(Followers followers) {
        followersRepository.delete(followers);
    }
}
