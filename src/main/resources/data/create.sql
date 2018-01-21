CREATE TABLE admin_honeypot_loginattempt
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  username    VARCHAR(255) NULL,
  ip_address  CHAR(15)     NULL,
  session_key VARCHAR(50)  NULL,
  user_agent  LONGTEXT     NULL,
  timestamp   DATETIME     NOT NULL,
  path        LONGTEXT     NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE auth_group
(
  id   INT AUTO_INCREMENT
    PRIMARY KEY,
  name VARCHAR(80) NOT NULL,
  CONSTRAINT name
  UNIQUE (name)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE auth_group_permissions
(
  id            INT AUTO_INCREMENT
    PRIMARY KEY,
  group_id      INT NOT NULL,
  permission_id INT NOT NULL,
  CONSTRAINT group_id
  UNIQUE (group_id, permission_id),
  CONSTRAINT auth_group_permission_group_id_689710a9a73b7457_fk_auth_group_id
  FOREIGN KEY (group_id) REFERENCES auth_group (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX auth_group__permission_id_1f49ccbbdc69d2fc_fk_auth_permission_id
  ON auth_group_permissions (permission_id);

CREATE TABLE auth_permission
(
  id              INT AUTO_INCREMENT
    PRIMARY KEY,
  name            VARCHAR(255) NOT NULL,
  content_type_id INT          NOT NULL,
  codename        VARCHAR(100) NOT NULL,
  CONSTRAINT content_type_id
  UNIQUE (content_type_id, codename)
)
  ENGINE = InnoDB
  CHARSET = latin1;

ALTER TABLE auth_group_permissions
  ADD CONSTRAINT auth_group__permission_id_1f49ccbbdc69d2fc_fk_auth_permission_id
FOREIGN KEY (permission_id) REFERENCES auth_permission (id);

CREATE TABLE captcha_captchastore
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  challenge  VARCHAR(32) NOT NULL,
  response   VARCHAR(32) NOT NULL,
  hashkey    VARCHAR(40) NOT NULL,
  expiration DATETIME    NOT NULL,
  CONSTRAINT hashkey
  UNIQUE (hashkey)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE common_album
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  album_id    INT          NULL,
  label       VARCHAR(150) NOT NULL,
  description LONGTEXT     NOT NULL,
  created_at  DATETIME     NOT NULL,
  updated_at  DATETIME     NULL,
  published   TINYINT(1)   NOT NULL,
  featured    TINYINT(1)   NOT NULL,
  cover_id    INT          NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_album_a39ff6ed
  ON common_album (cover_id);

CREATE TABLE common_classified
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  title       VARCHAR(100) NOT NULL,
  description LONGTEXT     NOT NULL,
  published   TINYINT(1)   NOT NULL,
  created_at  DATETIME     NOT NULL,
  updated_at  DATETIME     NOT NULL,
  meta_data   LONGTEXT     NOT NULL,
  contact_id  INT          NOT NULL,
  owner_id    INT          NOT NULL,
  type_id     INT          NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_classified_6d82f13d
  ON common_classified (contact_id);

CREATE INDEX common_classified_5e7b1936
  ON common_classified (owner_id);

CREATE INDEX common_classified_94757cae
  ON common_classified (type_id);

CREATE TABLE common_classifiedtype
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  title       VARCHAR(100) NOT NULL,
  description LONGTEXT     NOT NULL,
  published   TINYINT(1)   NOT NULL,
  created_at  DATETIME     NOT NULL,
  updated_at  DATETIME     NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

ALTER TABLE common_classified
  ADD CONSTRAINT common_class_type_id_16f5c39a504ae0c_fk_common_classifiedtype_id
FOREIGN KEY (type_id) REFERENCES common_classifiedtype (id);

CREATE TABLE common_collection
(
  id             INT AUTO_INCREMENT
    PRIMARY KEY,
  name           VARCHAR(25) NOT NULL,
  featured       TINYINT(1)  NOT NULL,
  published      TINYINT(1)  NOT NULL,
  created_at     DATETIME    NOT NULL,
  updated_at     DATETIME    NOT NULL,
  description    LONGTEXT    NULL,
  cover_album_id INT         NULL,
  CONSTRAINT common_collec_cover_album_id_6539b02bac8f646d_fk_common_album_id
  FOREIGN KEY (cover_album_id) REFERENCES common_album (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_collec_cover_album_id_6539b02bac8f646d_fk_common_album_id
  ON common_collection (cover_album_id);

CREATE TABLE common_collection_albums
(
  id            INT AUTO_INCREMENT
    PRIMARY KEY,
  collection_id INT NOT NULL,
  album_id      INT NOT NULL,
  CONSTRAINT collection_id
  UNIQUE (collection_id, album_id),
  CONSTRAINT common_co_collection_id_12fca1d9e19284cf_fk_common_collection_id
  FOREIGN KEY (collection_id) REFERENCES common_collection (id),
  CONSTRAINT common_collection_al_album_id_f7d9786a8e346ab_fk_common_album_id
  FOREIGN KEY (album_id) REFERENCES common_album (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_collection_al_album_id_f7d9786a8e346ab_fk_common_album_id
  ON common_collection_albums (album_id);

CREATE TABLE common_comment
(
  comment_id       INT AUTO_INCREMENT
    PRIMARY KEY,
  image_comment    LONGTEXT     NOT NULL,
  comment_author   VARCHAR(150) NOT NULL,
  comment_date     DATETIME     NOT NULL,
  comment_approved TINYINT(1)   NOT NULL,
  image_id         INT          NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_comment_f33175e6
  ON common_comment (image_id);

CREATE TABLE common_contact
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  first_name VARCHAR(50)  NOT NULL,
  last_name  VARCHAR(50)  NOT NULL,
  email      VARCHAR(254) NOT NULL,
  telephone  VARCHAR(50)  NOT NULL,
  owner_id   INT          NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_contact_owner_id_4a7ecaf6d4907c06_fk_common_mokouser_id
  ON common_contact (owner_id);

ALTER TABLE common_classified
  ADD CONSTRAINT common_classifi_contact_id_32f01f7f0bc8ce6f_fk_common_contact_id
FOREIGN KEY (contact_id) REFERENCES common_contact (id);

CREATE TABLE common_favourite
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  client_ip  VARCHAR(13) NULL,
  created_at DATETIME    NOT NULL,
  photo_id   INT         NOT NULL,
  user_id    INT         NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_favourite_b4e75e23
  ON common_favourite (photo_id);

CREATE INDEX common_favourite_e8701ad4
  ON common_favourite (user_id);

CREATE TABLE common_hospitality
(
  id               INT AUTO_INCREMENT
    PRIMARY KEY,
  featured         TINYINT(1)   NOT NULL,
  hospitality_type VARCHAR(20)  NOT NULL,
  name             VARCHAR(100) NOT NULL,
  description      LONGTEXT     NOT NULL,
  address          LONGTEXT     NOT NULL,
  website          VARCHAR(100) NOT NULL,
  date_added       DATETIME     NOT NULL,
  published        TINYINT(1)   NOT NULL,
  contact_id       INT          NOT NULL,
  CONSTRAINT common_hospital_contact_id_3b8ef175b66963a7_fk_common_contact_id
  FOREIGN KEY (contact_id) REFERENCES common_contact (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_hospitality_6d82f13d
  ON common_hospitality (contact_id);

CREATE TABLE common_hospitality_albums
(
  id             INT AUTO_INCREMENT
    PRIMARY KEY,
  hospitality_id INT NOT NULL,
  album_id       INT NOT NULL,
  CONSTRAINT hospitality_id
  UNIQUE (hospitality_id, album_id),
  CONSTRAINT common_h_hospitality_id_74df18b56c7c9ab_fk_common_hospitality_id
  FOREIGN KEY (hospitality_id) REFERENCES common_hospitality (id),
  CONSTRAINT common_hospitality__album_id_5736eee66ca8b470_fk_common_album_id
  FOREIGN KEY (album_id) REFERENCES common_album (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_hospitality__album_id_5736eee66ca8b470_fk_common_album_id
  ON common_hospitality_albums (album_id);

CREATE TABLE common_mokouser
(
  id           INT AUTO_INCREMENT
    PRIMARY KEY,
  password     VARCHAR(128) NOT NULL,
  last_login   DATETIME     NULL,
  is_superuser TINYINT(1)   NOT NULL,
  email        VARCHAR(254) NOT NULL,
  first_name   VARCHAR(30)  NOT NULL,
  last_name    VARCHAR(30)  NOT NULL,
  is_staff     TINYINT(1)   NOT NULL,
  is_active    TINYINT(1)   NOT NULL,
  date_joined  DATETIME     NOT NULL,
  CONSTRAINT email
  UNIQUE (email)
)
  ENGINE = InnoDB
  CHARSET = latin1;

ALTER TABLE common_classified
  ADD CONSTRAINT common_classifie_owner_id_15d97309cff7d304_fk_common_mokouser_id
FOREIGN KEY (owner_id) REFERENCES common_mokouser (id);

ALTER TABLE common_contact
  ADD CONSTRAINT common_contact_owner_id_4a7ecaf6d4907c06_fk_common_mokouser_id
FOREIGN KEY (owner_id) REFERENCES common_mokouser (id);

ALTER TABLE common_favourite
  ADD CONSTRAINT common_favourite_user_id_2025078af54fcde4_fk_common_mokouser_id
FOREIGN KEY (user_id) REFERENCES common_mokouser (id);

CREATE TABLE common_mokouser_groups
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  mokouser_id INT NOT NULL,
  group_id    INT NOT NULL,
  CONSTRAINT mokouser_id
  UNIQUE (mokouser_id, group_id),
  CONSTRAINT common_mokous_mokouser_id_77505c11062e4189_fk_common_mokouser_id
  FOREIGN KEY (mokouser_id) REFERENCES common_mokouser (id),
  CONSTRAINT common_mokouser_group_group_id_58cf30e489b7b144_fk_auth_group_id
  FOREIGN KEY (group_id) REFERENCES auth_group (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_mokouser_group_group_id_58cf30e489b7b144_fk_auth_group_id
  ON common_mokouser_groups (group_id);

CREATE TABLE common_mokouser_user_permissions
(
  id            INT AUTO_INCREMENT
    PRIMARY KEY,
  mokouser_id   INT NOT NULL,
  permission_id INT NOT NULL,
  CONSTRAINT mokouser_id
  UNIQUE (mokouser_id, permission_id),
  CONSTRAINT common_mokous_mokouser_id_6f5d5457ba786d6f_fk_common_mokouser_id
  FOREIGN KEY (mokouser_id) REFERENCES common_mokouser (id),
  CONSTRAINT common_moko_permission_id_7ce1e321ca716e71_fk_auth_permission_id
  FOREIGN KEY (permission_id) REFERENCES auth_permission (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_moko_permission_id_7ce1e321ca716e71_fk_auth_permission_id
  ON common_mokouser_user_permissions (permission_id);

CREATE TABLE common_photo
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  image_id    VARCHAR(40)  NOT NULL,
  name        VARCHAR(250) NOT NULL,
  path        VARCHAR(150) NULL,
  caption     LONGTEXT     NOT NULL,
  created_at  DATETIME     NOT NULL,
  updated_at  DATETIME     NOT NULL,
  published   TINYINT(1)   NOT NULL,
  deleted_at  DATETIME     NULL,
  cloud_image VARCHAR(255) NULL,
  owner       INT          NOT NULL,
  CONSTRAINT common_photo_owner_3de443e05ccbce09_fk_common_mokouser_id
  FOREIGN KEY (owner) REFERENCES common_mokouser (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_photo_owner_3de443e05ccbce09_fk_common_mokouser_id
  ON common_photo (owner);

ALTER TABLE common_album
  ADD CONSTRAINT common_album_cover_id_4122347c4d4e29ff_fk_common_photo_id
FOREIGN KEY (cover_id) REFERENCES common_photo (id);

ALTER TABLE common_comment
  ADD CONSTRAINT common_comment_image_id_71554b7f05d3d133_fk_common_photo_id
FOREIGN KEY (image_id) REFERENCES common_photo (id);

ALTER TABLE common_favourite
  ADD CONSTRAINT common_favourite_photo_id_b6c4fa1d5fbea24_fk_common_photo_id
FOREIGN KEY (photo_id) REFERENCES common_photo (id);

CREATE TABLE common_photo_albums
(
  id       INT AUTO_INCREMENT
    PRIMARY KEY,
  photo_id INT NOT NULL,
  album_id INT NOT NULL,
  CONSTRAINT photo_id
  UNIQUE (photo_id, album_id),
  CONSTRAINT common_photo_albums_photo_id_45067715147378a1_fk_common_photo_id
  FOREIGN KEY (photo_id) REFERENCES common_photo (id),
  CONSTRAINT common_photo_albums_album_id_52775e9acf3d9ae4_fk_common_album_id
  FOREIGN KEY (album_id) REFERENCES common_album (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_photo_albums_album_id_52775e9acf3d9ae4_fk_common_album_id
  ON common_photo_albums (album_id);

CREATE TABLE common_photo_video
(
  id       INT AUTO_INCREMENT
    PRIMARY KEY,
  photo_id INT NOT NULL,
  video_id INT NOT NULL,
  CONSTRAINT photo_id
  UNIQUE (photo_id, video_id),
  CONSTRAINT common_photo_video_photo_id_248bbd6db24bd27b_fk_common_photo_id
  FOREIGN KEY (photo_id) REFERENCES common_photo (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_photo_video_video_id_46e23a856c8da0d2_fk_common_video_id
  ON common_photo_video (video_id);

CREATE TABLE common_photostory
(
  id          INT AUTO_INCREMENT
    PRIMARY KEY,
  name        VARCHAR(150) NOT NULL,
  description LONGTEXT     NOT NULL,
  created_at  DATETIME     NOT NULL,
  published   TINYINT(1)   NOT NULL,
  album_id    INT          NOT NULL,
  CONSTRAINT common_photostory_album_id_142bf69bd2d3c232_fk_common_album_id
  FOREIGN KEY (album_id) REFERENCES common_album (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_photostory_album_id_142bf69bd2d3c232_fk_common_album_id
  ON common_photostory (album_id);

CREATE TABLE common_photoviews
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  ip_address CHAR(39) NOT NULL,
  photo_id   INT      NOT NULL,
  user_id    INT      NULL,
  CONSTRAINT common_photoviews_photo_id_293444645fd76c7e_fk_common_photo_id
  FOREIGN KEY (photo_id) REFERENCES common_photo (id),
  CONSTRAINT common_photoviews_user_id_2220ee8ec6224ebe_fk_common_mokouser_id
  FOREIGN KEY (user_id) REFERENCES common_mokouser (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX common_photoviews_b4e75e23
  ON common_photoviews (photo_id);

CREATE INDEX common_photoviews_e8701ad4
  ON common_photoviews (user_id);

CREATE TABLE common_promotion
(
  id                 INT AUTO_INCREMENT
    PRIMARY KEY,
  promo_handle       VARCHAR(50)  NOT NULL,
  promo_type         VARCHAR(20)  NOT NULL,
  promo_name         VARCHAR(150) NOT NULL,
  promo_instructions LONGTEXT     NOT NULL,
  promo_album        INT          NULL,
  static_image_path  VARCHAR(250) NOT NULL,
  start_date         DATETIME     NOT NULL,
  end_date           DATETIME     NOT NULL,
  featured           INT          NOT NULL,
  published          INT          NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE common_video
(
  id              INT AUTO_INCREMENT
    PRIMARY KEY,
  external_id     VARCHAR(150) NOT NULL,
  external_source VARCHAR(25)  NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

ALTER TABLE common_photo_video
  ADD CONSTRAINT common_photo_video_video_id_46e23a856c8da0d2_fk_common_video_id
FOREIGN KEY (video_id) REFERENCES common_video (id);

CREATE TABLE django_admin_log
(
  id              INT AUTO_INCREMENT
    PRIMARY KEY,
  action_time     DATETIME             NOT NULL,
  object_id       LONGTEXT             NULL,
  object_repr     VARCHAR(200)         NOT NULL,
  action_flag     SMALLINT(5) UNSIGNED NOT NULL,
  change_message  LONGTEXT             NOT NULL,
  content_type_id INT                  NULL,
  user_id         INT                  NOT NULL,
  CONSTRAINT django_admin_log_user_id_52fdd58701c5f563_fk_common_mokouser_id
  FOREIGN KEY (user_id) REFERENCES common_mokouser (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX djang_content_type_id_697914295151027a_fk_django_content_type_id
  ON django_admin_log (content_type_id);

CREATE INDEX django_admin_log_user_id_52fdd58701c5f563_fk_common_mokouser_id
  ON django_admin_log (user_id);

CREATE TABLE django_content_type
(
  id        INT AUTO_INCREMENT
    PRIMARY KEY,
  app_label VARCHAR(100) NOT NULL,
  model     VARCHAR(100) NOT NULL,
  CONSTRAINT django_content_type_app_label_45f3b1d93ec8c61c_uniq
  UNIQUE (app_label, model)
)
  ENGINE = InnoDB
  CHARSET = latin1;

ALTER TABLE auth_permission
  ADD CONSTRAINT auth__content_type_id_508cf46651277a81_fk_django_content_type_id
FOREIGN KEY (content_type_id) REFERENCES django_content_type (id);

ALTER TABLE django_admin_log
  ADD CONSTRAINT djang_content_type_id_697914295151027a_fk_django_content_type_id
FOREIGN KEY (content_type_id) REFERENCES django_content_type (id);

CREATE TABLE django_migrations
(
  id      INT AUTO_INCREMENT
    PRIMARY KEY,
  app     VARCHAR(255) NOT NULL,
  name    VARCHAR(255) NOT NULL,
  applied DATETIME     NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE django_session
(
  session_key  VARCHAR(40) NOT NULL
    PRIMARY KEY,
  session_data LONGTEXT    NOT NULL,
  expire_date  DATETIME    NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX django_session_de54fa62
  ON django_session (expire_date);

CREATE TABLE social_auth_association
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  server_url VARCHAR(255) NOT NULL,
  handle     VARCHAR(255) NOT NULL,
  secret     VARCHAR(255) NOT NULL,
  issued     INT          NOT NULL,
  lifetime   INT          NOT NULL,
  assoc_type VARCHAR(64)  NOT NULL
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE social_auth_code
(
  id       INT AUTO_INCREMENT
    PRIMARY KEY,
  email    VARCHAR(254) NOT NULL,
  code     VARCHAR(32)  NOT NULL,
  verified TINYINT(1)   NOT NULL,
  CONSTRAINT social_auth_code_email_75f27066d057e3b6_uniq
  UNIQUE (email, code)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX social_auth_code_c1336794
  ON social_auth_code (code);

CREATE TABLE social_auth_nonce
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  server_url VARCHAR(255) NOT NULL,
  timestamp  INT          NOT NULL,
  salt       VARCHAR(65)  NOT NULL,
  CONSTRAINT social_auth_nonce_server_url_36601f978463b4_uniq
  UNIQUE (server_url, timestamp, salt)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE TABLE social_auth_usersocialauth
(
  id         INT AUTO_INCREMENT
    PRIMARY KEY,
  provider   VARCHAR(32)  NOT NULL,
  uid        VARCHAR(255) NOT NULL,
  extra_data LONGTEXT     NOT NULL,
  user_id    INT          NOT NULL,
  CONSTRAINT social_auth_usersocialauth_provider_2f763109e2c4a1fb_uniq
  UNIQUE (provider, uid),
  CONSTRAINT social_auth_users_user_id_193b2d80880502b2_fk_common_mokouser_id
  FOREIGN KEY (user_id) REFERENCES common_mokouser (id)
)
  ENGINE = InnoDB
  CHARSET = latin1;

CREATE INDEX social_auth_users_user_id_193b2d80880502b2_fk_common_mokouser_id
  ON social_auth_usersocialauth (user_id);

