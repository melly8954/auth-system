CREATE TABLE `user` (
                        `id` bigint(20) NOT NULL AUTO_INCREMENT,
                        `username` varchar(255) DEFAULT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        `email` varchar(50) NOT NULL,
                        `role` varchar(20) NOT NULL DEFAULT 'USER',
                        `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
                        `last_login_at` datetime DEFAULT NULL,
                        `created_at` datetime NOT NULL,
                        `updated_at` datetime NOT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_username` (`username`),
                        UNIQUE KEY `uk_user_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `user_profile` (
                                `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                `user_id` bigint(20) NOT NULL,
                                `name` varchar(20) DEFAULT NULL,
                                `nickname` varchar(20) DEFAULT NULL,
                                `image_url` varchar(255) DEFAULT NULL,
                                `created_at` datetime NOT NULL,
                                `updated_at` datetime NOT NULL,
                                PRIMARY KEY (`id`),
                                KEY `fk_user_profile_user` (`user_id`),
                                CONSTRAINT `fk_user_profile_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

CREATE TABLE `user_social_account` (
                                       `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                       `user_id` bigint(20) NOT NULL,
                                       `provider` varchar(20) NOT NULL,
                                       `provider_id` varchar(255) NOT NULL,
                                       `provider_email` varchar(50) DEFAULT NULL,
                                       `created_at` datetime NOT NULL,
                                       `updated_at` datetime NOT NULL,
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_provider_account` (`provider`,`provider_id`),
                                       KEY `fk_social_user` (`user_id`),
                                       CONSTRAINT `fk_social_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;