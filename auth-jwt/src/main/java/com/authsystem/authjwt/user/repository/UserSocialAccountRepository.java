package com.authsystem.authjwt.user.repository;

import com.authsystem.authjwt.user.entity.UserSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, Long> {
    @Query("""
    SELECT usa
    FROM UserSocialAccount usa
    JOIN FETCH usa.user u
    JOIN FETCH UserProfile p ON p.user = u 
    WHERE usa.provider = :provider
    AND usa.providerId = :providerId
""")
    Optional<UserSocialAccount> findByProviderAndProviderId(String provider, String providerId);
}
