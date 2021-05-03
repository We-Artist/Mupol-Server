package com.mupol.mupolserver.domain.followers;

import com.mupol.mupolserver.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowersRepository extends JpaRepository<Followers, Long> {
    Optional<Followers> findFollowersByFromAndTo(User from, User to);
    Optional<List<Followers>> findAllByFrom(User from);
    Optional<List<Followers>> findAllByTo(User to);
}
