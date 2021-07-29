package com.mupol.mupolserver.domain.follower;

import com.mupol.mupolserver.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    Optional<Follower> findFollowersByFromAndTo(User from, User to);
    Optional<List<Follower>> findAllByFrom(User from);
    Optional<List<Follower>> findAllByTo(User to);
    Optional<List<Follower>> findToIdByFromId(Long from);
}
