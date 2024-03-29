package com.mupol.mupolserver.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySnsIdAndProvider(String snsId, SnsType provider);
    Optional<List<User>> findAllByUsernameContains(String username);
}
