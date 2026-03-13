package com.authsystem.authjwt.user.repository;

import com.authsystem.authjwt.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
