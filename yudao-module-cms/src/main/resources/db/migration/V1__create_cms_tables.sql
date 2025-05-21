-- Create cms_category table
CREATE TABLE `cms_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `parent_id` BIGINT NULL,
    `description` TEXT NULL,
    `creator` VARCHAR(64) NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) NULL DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT 0,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_category_name_tenant` (`name`, `tenant_id`),
    UNIQUE INDEX `uk_category_slug_tenant` (`slug`, `tenant_id`),
    INDEX `idx_category_parent_id` (`parent_id`),
    CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `cms_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create cms_tag table
CREATE TABLE `cms_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `creator` VARCHAR(64) NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) NULL DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT 0,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_tag_name_tenant` (`name`, `tenant_id`),
    UNIQUE INDEX `uk_tag_slug_tenant` (`slug`, `tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create cms_article table
CREATE TABLE `cms_article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `slug` VARCHAR(255) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `author_id` BIGINT NULL,
    `category_id` BIGINT NULL,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0: draft, 1: published, 2: archived',
    `published_at` DATETIME NULL,
    `cover_image_url` VARCHAR(1024) NULL,
    `views` INT NOT NULL DEFAULT 0,
    `meta_description` VARCHAR(500) NULL,
    `meta_keywords` VARCHAR(255) NULL,
    `creator` VARCHAR(64) NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) NULL DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT 0,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_article_slug_tenant` (`slug`, `tenant_id`),
    INDEX `idx_article_category_id` (`category_id`),
    INDEX `idx_article_author_id` (`author_id`),
    INDEX `idx_article_status` (`status`),
    CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `cms_category` (`id`) ON DELETE SET NULL
    -- Assuming author_id links to a system_users table. If that table is also tenant-specific, the FK might need adjustment or be handled at the application level.
    -- For now, no direct FK on author_id to avoid issues if system_users table is not yet present or has a different tenant model.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create cms_article_tag table
CREATE TABLE `cms_article_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    `creator` VARCHAR(64) NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) NULL DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT 0,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_article_tag_article_id_tag_id_tenant` (`article_id`, `tag_id`, `tenant_id`),
    CONSTRAINT `fk_article_tag_article` FOREIGN KEY (`article_id`) REFERENCES `cms_article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_article_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `cms_tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create cms_comment table
CREATE TABLE `cms_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `user_id` BIGINT NULL,
    `author_name` VARCHAR(255) NULL,
    `author_email` VARCHAR(255) NULL,
    `content` TEXT NOT NULL,
    `parent_id` BIGINT NULL,
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0: pending, 1: approved, 2: spam',
    `creator` VARCHAR(64) NULL DEFAULT '',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` VARCHAR(64) NULL DEFAULT '',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` BIT(1) NOT NULL DEFAULT 0,
    `tenant_id` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_comment_article_id` (`article_id`),
    INDEX `idx_comment_user_id` (`user_id`),
    INDEX `idx_comment_parent_id` (`parent_id`),
    INDEX `idx_comment_status` (`status`),
    CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `cms_article` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `cms_comment` (`id`) ON DELETE CASCADE
    -- Similar to author_id in cms_article, user_id might link to a system_users table. No direct FK for now.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add indexes for frequently queried columns
ALTER TABLE `cms_category` ADD INDEX `idx_category_deleted` (`deleted`);
ALTER TABLE `cms_tag` ADD INDEX `idx_tag_deleted` (`deleted`);
ALTER TABLE `cms_article` ADD INDEX `idx_article_deleted` (`deleted`);
ALTER TABLE `cms_article` ADD INDEX `idx_article_published_at` (`published_at`);
ALTER TABLE `cms_article_tag` ADD INDEX `idx_article_tag_deleted` (`deleted`);
ALTER TABLE `cms_comment` ADD INDEX `idx_comment_deleted` (`deleted`);

-- Add indexes for tenant_id as it's part of unique keys or frequently used in queries
ALTER TABLE `cms_category` ADD INDEX `idx_category_tenant_id` (`tenant_id`);
ALTER TABLE `cms_tag` ADD INDEX `idx_tag_tenant_id` (`tenant_id`);
ALTER TABLE `cms_article` ADD INDEX `idx_article_tenant_id` (`tenant_id`);
ALTER TABLE `cms_article_tag` ADD INDEX `idx_article_tag_tenant_id` (`tenant_id`);
ALTER TABLE `cms_comment` ADD INDEX `idx_comment_tenant_id` (`tenant_id`);

-- Add indexes for slug columns as they are used for lookups
ALTER TABLE `cms_category` ADD INDEX `idx_category_slug` (`slug`);
ALTER TABLE `cms_tag` ADD INDEX `idx_tag_slug` (`slug`);
ALTER TABLE `cms_article` ADD INDEX `idx_article_slug` (`slug`);

-- Add unique constraints for name/slug to include tenant_id to ensure uniqueness *within* a tenant.
-- The previous unique indexes `uk_category_name_tenant`, `uk_category_slug_tenant`, etc. already cover this.
-- Re-checking the table definitions:
-- `cms_category`: name and slug unique constraints are `UNIQUE INDEX uk_category_name_tenant (name, tenant_id)` and `UNIQUE INDEX uk_category_slug_tenant (slug, tenant_id)`. This is correct.
-- `cms_tag`: name and slug unique constraints are `UNIQUE INDEX uk_tag_name_tenant (name, tenant_id)` and `UNIQUE INDEX uk_tag_slug_tenant (slug, tenant_id)`. This is correct.
-- `cms_article`: slug unique constraint is `UNIQUE INDEX uk_article_slug_tenant (slug, tenant_id)`. This is correct.
-- `cms_article_tag`: unique constraint on (article_id, tag_id) is `UNIQUE INDEX uk_article_tag_article_id_tag_id_tenant (article_id, tag_id, tenant_id)`. This is correct.

-- Note: The foreign key for `author_id` in `cms_article` and `user_id` in `cms_comment` to a general user table (e.g., `system_users.id`)
-- is intentionally omitted in this script. This is because the `system_users` table might have its own specific structure,
-- tenancy considerations, or might not even exist at the point this script is run.
-- These relationships are often better managed at the application layer or with separate, later migration scripts
-- once the structure of the user table is confirmed and stable.

-- Adding a comment to the end of the file to signify completion.
SELECT 1;
