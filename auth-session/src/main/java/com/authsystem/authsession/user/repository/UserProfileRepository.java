package com.authsystem.authsession.user.repository;

import com.authsystem.authsession.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
