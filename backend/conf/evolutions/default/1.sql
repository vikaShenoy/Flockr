# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

-- init script create procs
-- Inital script to create stored procedures etc for mysql platform
DROP PROCEDURE IF EXISTS usp_ebean_drop_foreign_keys;

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_foreign_keys TABLE, COLUMN
-- deletes all constraints and foreign keys referring to TABLE.COLUMN
--
CREATE PROCEDURE usp_ebean_drop_foreign_keys(IN p_table_name VARCHAR(255), IN p_column_name VARCHAR(255))
BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE c_fk_name CHAR(255);
  DECLARE curs CURSOR FOR SELECT CONSTRAINT_NAME from information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE() and TABLE_NAME = p_table_name and COLUMN_NAME = p_column_name
      AND REFERENCED_TABLE_NAME IS NOT NULL;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  OPEN curs;

  read_loop: LOOP
    FETCH curs INTO c_fk_name;
    IF done THEN
      LEAVE read_loop;
    END IF;
    SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP FOREIGN KEY ', c_fk_name);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
  END LOOP;

  CLOSE curs;
END
$$

DROP PROCEDURE IF EXISTS usp_ebean_drop_column;

delimiter $$
--
-- PROCEDURE: usp_ebean_drop_column TABLE, COLUMN
-- deletes the column and ensures that all indices and constraints are dropped first
--
CREATE PROCEDURE usp_ebean_drop_column(IN p_table_name VARCHAR(255), IN p_column_name VARCHAR(255))
BEGIN
  CALL usp_ebean_drop_foreign_keys(p_table_name, p_column_name);
  SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP COLUMN ', p_column_name);
  PREPARE stmt FROM @sql;
  EXECUTE stmt;
END
$$
create table country (
  country_id                    integer auto_increment not null,
  country_name                  varchar(255),
  isocode                       varchar(255),
  is_valid                      tinyint(1) default 0 not null,
  constraint pk_country primary key (country_id)
);

create table destination (
  destination_id                integer auto_increment not null,
  destination_name              varchar(255),
  destination_type_destination_type_id integer,
  destination_district_district_id integer,
  destination_lat               double,
  destination_lon               double,
  destination_country_country_id integer,
  destination_owner             integer,
  is_public                     tinyint(1) default 0 not null,
  constraint pk_destination primary key (destination_id)
);

create table destination_photo (
  destination_photo_id          integer auto_increment not null,
  destination_destination_id    integer,
  personal_photo_photo_id       integer,
  constraint pk_destination_photo primary key (destination_photo_id)
);

create table destination_proposal (
  destination_proposal_id       integer auto_increment not null,
  destination_destination_id    integer,
  constraint pk_destination_proposal primary key (destination_proposal_id)
);

create table destination_proposal_traveller_type (
  destination_proposal_destination_proposal_id integer not null,
  traveller_type_traveller_type_id integer not null,
  constraint pk_destination_proposal_traveller_type primary key (destination_proposal_destination_proposal_id,traveller_type_traveller_type_id)
);

create table destination_type (
  destination_type_id           integer auto_increment not null,
  destination_type_name         varchar(255),
  constraint pk_destination_type primary key (destination_type_id)
);

create table district (
  district_id                   integer auto_increment not null,
  district_name                 varchar(255),
  country_country_id            integer,
  constraint pk_district primary key (district_id)
);

create table nationality (
  nationality_id                integer auto_increment not null,
  nationality_country_country_id integer,
  nationality_name              varchar(255),
  constraint uq_nationality_nationality_country_country_id unique (nationality_country_country_id),
  constraint pk_nationality primary key (nationality_id)
);

create table nationality_user (
  nationality_nationality_id    integer not null,
  user_user_id                  integer not null,
  constraint pk_nationality_user primary key (nationality_nationality_id,user_user_id)
);

create table passport (
  passport_id                   integer auto_increment not null,
  passport_country              varchar(255),
  country_country_id            integer,
  constraint uq_passport_country_country_id unique (country_country_id),
  constraint pk_passport primary key (passport_id)
);

create table passport_user (
  passport_passport_id          integer not null,
  user_user_id                  integer not null,
  constraint pk_passport_user primary key (passport_passport_id,user_user_id)
);

create table personal_photo (
  photo_id                      integer auto_increment not null,
  user_user_id                  integer,
  is_public                     tinyint(1) default 0 not null,
  is_primary                    tinyint(1) default 0 not null,
  filename_hash                 varchar(255),
  thumbnail_name                varchar(255),
  constraint pk_personal_photo primary key (photo_id)
);

create table role (
  role_id                       integer auto_increment not null,
  role_type                     varchar(255),
  constraint uq_role_role_type unique (role_type),
  constraint pk_role primary key (role_id)
);

create table role_user (
  role_role_id                  integer not null,
  user_user_id                  integer not null,
  constraint pk_role_user primary key (role_role_id,user_user_id)
);

create table traveller_type (
  traveller_type_id             integer auto_increment not null,
  traveller_type_name           varchar(255),
  constraint pk_traveller_type primary key (traveller_type_id)
);

create table traveller_type_user (
  traveller_type_traveller_type_id integer not null,
  user_user_id                  integer not null,
  constraint pk_traveller_type_user primary key (traveller_type_traveller_type_id,user_user_id)
);

create table traveller_type_destination (
  traveller_type_traveller_type_id integer not null,
  destination_destination_id    integer not null,
  constraint pk_traveller_type_destination primary key (traveller_type_traveller_type_id,destination_destination_id)
);

create table trip (
  trip_id                       integer auto_increment not null,
  user_user_id                  integer,
  trip_name                     varchar(255),
  constraint pk_trip primary key (trip_id)
);

create table trip_destination (
  trip_destination_id           integer auto_increment not null,
  trip_trip_id                  integer,
  destination_destination_id    integer,
  arrival_date                  datetime(6),
  arrival_time                  integer,
  departure_date                datetime(6),
  departure_time                integer,
  constraint pk_trip_destination primary key (trip_destination_id)
);

create table user (
  user_id                       integer auto_increment not null,
  first_name                    varchar(255),
  middle_name                   varchar(255),
  last_name                     varchar(255),
  date_of_birth                 datetime(6),
  gender                        varchar(255),
  email                         varchar(255),
  profile_photo_photo_id        integer,
  password_hash                 varchar(255),
  token                         varchar(255),
  timestamp                     datetime(6) not null,
  constraint uq_user_email unique (email),
  constraint uq_user_profile_photo_photo_id unique (profile_photo_photo_id),
  constraint pk_user primary key (user_id)
);

create table user_role (
  user_role_id                  integer auto_increment not null,
  role_id                       integer not null,
  user_id                       integer not null,
  constraint pk_user_role primary key (user_role_id)
);

create index ix_destination_destination_type_destination_type_id on destination (destination_type_destination_type_id);
alter table destination add constraint fk_destination_destination_type_destination_type_id foreign key (destination_type_destination_type_id) references destination_type (destination_type_id) on delete restrict on update restrict;

create index ix_destination_destination_district_district_id on destination (destination_district_district_id);
alter table destination add constraint fk_destination_destination_district_district_id foreign key (destination_district_district_id) references district (district_id) on delete restrict on update restrict;

create index ix_destination_destination_country_country_id on destination (destination_country_country_id);
alter table destination add constraint fk_destination_destination_country_country_id foreign key (destination_country_country_id) references country (country_id) on delete restrict on update restrict;

create index ix_destination_photo_destination_destination_id on destination_photo (destination_destination_id);
alter table destination_photo add constraint fk_destination_photo_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_destination_photo_personal_photo_photo_id on destination_photo (personal_photo_photo_id);
alter table destination_photo add constraint fk_destination_photo_personal_photo_photo_id foreign key (personal_photo_photo_id) references personal_photo (photo_id) on delete restrict on update restrict;

create index ix_destination_proposal_destination_destination_id on destination_proposal (destination_destination_id);
alter table destination_proposal add constraint fk_destination_proposal_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_destination_proposal_traveller_type_destination_proposal on destination_proposal_traveller_type (destination_proposal_destination_proposal_id);
alter table destination_proposal_traveller_type add constraint fk_destination_proposal_traveller_type_destination_proposal foreign key (destination_proposal_destination_proposal_id) references destination_proposal (destination_proposal_id) on delete restrict on update restrict;

create index ix_destination_proposal_traveller_type_traveller_type on destination_proposal_traveller_type (traveller_type_traveller_type_id);
alter table destination_proposal_traveller_type add constraint fk_destination_proposal_traveller_type_traveller_type foreign key (traveller_type_traveller_type_id) references traveller_type (traveller_type_id) on delete restrict on update restrict;

create index ix_district_country_country_id on district (country_country_id);
alter table district add constraint fk_district_country_country_id foreign key (country_country_id) references country (country_id) on delete restrict on update restrict;

alter table nationality add constraint fk_nationality_nationality_country_country_id foreign key (nationality_country_country_id) references country (country_id) on delete restrict on update restrict;

create index ix_nationality_user_nationality on nationality_user (nationality_nationality_id);
alter table nationality_user add constraint fk_nationality_user_nationality foreign key (nationality_nationality_id) references nationality (nationality_id) on delete restrict on update restrict;

create index ix_nationality_user_user on nationality_user (user_user_id);
alter table nationality_user add constraint fk_nationality_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

alter table passport add constraint fk_passport_country_country_id foreign key (country_country_id) references country (country_id) on delete restrict on update restrict;

create index ix_passport_user_passport on passport_user (passport_passport_id);
alter table passport_user add constraint fk_passport_user_passport foreign key (passport_passport_id) references passport (passport_id) on delete restrict on update restrict;

create index ix_passport_user_user on passport_user (user_user_id);
alter table passport_user add constraint fk_passport_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_personal_photo_user_user_id on personal_photo (user_user_id);
alter table personal_photo add constraint fk_personal_photo_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_role_user_role on role_user (role_role_id);
alter table role_user add constraint fk_role_user_role foreign key (role_role_id) references role (role_id) on delete restrict on update restrict;

create index ix_role_user_user on role_user (user_user_id);
alter table role_user add constraint fk_role_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_traveller_type_user_traveller_type on traveller_type_user (traveller_type_traveller_type_id);
alter table traveller_type_user add constraint fk_traveller_type_user_traveller_type foreign key (traveller_type_traveller_type_id) references traveller_type (traveller_type_id) on delete restrict on update restrict;

create index ix_traveller_type_user_user on traveller_type_user (user_user_id);
alter table traveller_type_user add constraint fk_traveller_type_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_traveller_type_destination_traveller_type on traveller_type_destination (traveller_type_traveller_type_id);
alter table traveller_type_destination add constraint fk_traveller_type_destination_traveller_type foreign key (traveller_type_traveller_type_id) references traveller_type (traveller_type_id) on delete restrict on update restrict;

create index ix_traveller_type_destination_destination on traveller_type_destination (destination_destination_id);
alter table traveller_type_destination add constraint fk_traveller_type_destination_destination foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_trip_user_user_id on trip (user_user_id);
alter table trip add constraint fk_trip_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_trip_destination_trip_trip_id on trip_destination (trip_trip_id);
alter table trip_destination add constraint fk_trip_destination_trip_trip_id foreign key (trip_trip_id) references trip (trip_id) on delete restrict on update restrict;

create index ix_trip_destination_destination_destination_id on trip_destination (destination_destination_id);
alter table trip_destination add constraint fk_trip_destination_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

alter table user add constraint fk_user_profile_photo_photo_id foreign key (profile_photo_photo_id) references personal_photo (photo_id) on delete restrict on update restrict;


# --- !Downs

alter table destination drop foreign key fk_destination_destination_type_destination_type_id;
drop index ix_destination_destination_type_destination_type_id on destination;

alter table destination drop foreign key fk_destination_destination_district_district_id;
drop index ix_destination_destination_district_district_id on destination;

alter table destination drop foreign key fk_destination_destination_country_country_id;
drop index ix_destination_destination_country_country_id on destination;

alter table destination_photo drop foreign key fk_destination_photo_destination_destination_id;
drop index ix_destination_photo_destination_destination_id on destination_photo;

alter table destination_photo drop foreign key fk_destination_photo_personal_photo_photo_id;
drop index ix_destination_photo_personal_photo_photo_id on destination_photo;

alter table destination_proposal drop foreign key fk_destination_proposal_destination_destination_id;
drop index ix_destination_proposal_destination_destination_id on destination_proposal;

alter table destination_proposal_traveller_type drop foreign key fk_destination_proposal_traveller_type_destination_proposal;
drop index ix_destination_proposal_traveller_type_destination_proposal on destination_proposal_traveller_type;

alter table destination_proposal_traveller_type drop foreign key fk_destination_proposal_traveller_type_traveller_type;
drop index ix_destination_proposal_traveller_type_traveller_type on destination_proposal_traveller_type;

alter table district drop foreign key fk_district_country_country_id;
drop index ix_district_country_country_id on district;

alter table nationality drop foreign key fk_nationality_nationality_country_country_id;

alter table nationality_user drop foreign key fk_nationality_user_nationality;
drop index ix_nationality_user_nationality on nationality_user;

alter table nationality_user drop foreign key fk_nationality_user_user;
drop index ix_nationality_user_user on nationality_user;

alter table passport drop foreign key fk_passport_country_country_id;

alter table passport_user drop foreign key fk_passport_user_passport;
drop index ix_passport_user_passport on passport_user;

alter table passport_user drop foreign key fk_passport_user_user;
drop index ix_passport_user_user on passport_user;

alter table personal_photo drop foreign key fk_personal_photo_user_user_id;
drop index ix_personal_photo_user_user_id on personal_photo;

alter table role_user drop foreign key fk_role_user_role;
drop index ix_role_user_role on role_user;

alter table role_user drop foreign key fk_role_user_user;
drop index ix_role_user_user on role_user;

alter table traveller_type_user drop foreign key fk_traveller_type_user_traveller_type;
drop index ix_traveller_type_user_traveller_type on traveller_type_user;

alter table traveller_type_user drop foreign key fk_traveller_type_user_user;
drop index ix_traveller_type_user_user on traveller_type_user;

alter table traveller_type_destination drop foreign key fk_traveller_type_destination_traveller_type;
drop index ix_traveller_type_destination_traveller_type on traveller_type_destination;

alter table traveller_type_destination drop foreign key fk_traveller_type_destination_destination;
drop index ix_traveller_type_destination_destination on traveller_type_destination;

alter table trip drop foreign key fk_trip_user_user_id;
drop index ix_trip_user_user_id on trip;

alter table trip_destination drop foreign key fk_trip_destination_trip_trip_id;
drop index ix_trip_destination_trip_trip_id on trip_destination;

alter table trip_destination drop foreign key fk_trip_destination_destination_destination_id;
drop index ix_trip_destination_destination_destination_id on trip_destination;

alter table user drop foreign key fk_user_profile_photo_photo_id;

drop table if exists country;

drop table if exists destination;

drop table if exists destination_photo;

drop table if exists destination_proposal;

drop table if exists destination_proposal_traveller_type;

drop table if exists destination_type;

drop table if exists district;

drop table if exists nationality;

drop table if exists nationality_user;

drop table if exists passport;

drop table if exists passport_user;

drop table if exists personal_photo;

drop table if exists role;

drop table if exists role_user;

drop table if exists traveller_type;

drop table if exists traveller_type_user;

drop table if exists traveller_type_destination;

drop table if exists trip;

drop table if exists trip_destination;

drop table if exists user;

drop table if exists user_role;

