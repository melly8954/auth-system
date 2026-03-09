package com.authsystem.authsession.user.entity;

import com.authsystem.authsession.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfile extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String name;
    private String nickname;

    @Column(name = "image_url")
    private String imageUrl;
}
