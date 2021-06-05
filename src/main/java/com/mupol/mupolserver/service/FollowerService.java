package com.mupol.mupolserver.service;

import com.mupol.mupolserver.domain.follower.Follower;
import com.mupol.mupolserver.domain.follower.FollowerRepository;
import com.mupol.mupolserver.domain.user.User;
import com.mupol.mupolserver.dto.user.FollowerResDto;
import com.mupol.mupolserver.dto.user.FollowingResDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class FollowerService {
    private final FollowerRepository followerRepository;

    public Follower getFollowerByFromAndTo(User follower, User following) {
        return followerRepository.findFollowersByFromAndTo(follower, following)
                .orElseThrow(() -> new IllegalArgumentException("you does not follow this user"));
    }

    public boolean isAlreadyFollowed(User follower, User following) {
        return followerRepository.findFollowersByFromAndTo(follower, following).isPresent();
    }

    public List<Follower> getFollowerList(User user) {
        return followerRepository.findAllByTo(user)
                .orElseThrow(() -> new IllegalArgumentException("there is no follower"));
    }

    public List<Follower> getFollowingList(User user) {
        return followerRepository.findAllByFrom(user)
                .orElseThrow(() -> new IllegalArgumentException("there is no following"));
    }

    public List<FollowerResDto> getFollowerDtoList(User user) {
        List<Follower> followerList = getFollowerList(user);
        List<FollowerResDto> dtoList = new ArrayList<>();

        for (Follower f : followerList) {
            dtoList.add(FollowerResDto.builder()
                    .userId(f.getFrom().getId())
                    .username(f.getFrom().getUsername())
                    .profileImageUrl(f.getFrom().getProfileImageUrl())
                    .isFollowing(f.isFollowEachOther())
                    .build());
        }

        return dtoList;
    }

    public List<FollowingResDto> getFollowingDtoList(User user) {
        List<Follower> followingList = getFollowingList(user);
        List<FollowingResDto> dtoList = new ArrayList<>();

        for (Follower f : followingList) {
            dtoList.add(FollowingResDto.builder()
                    .userId(f.getTo().getId())
                    .username(f.getTo().getUsername())
                    .profileImageUrl(f.getTo().getProfileImageUrl())
                    .build());
        }

        return dtoList;
    }

    // TODO: 캐싱
    public void save(Follower follower) {
        followerRepository.save(follower);
    }

    public void delete(Follower follower) {
        followerRepository.delete(follower);
    }
}
