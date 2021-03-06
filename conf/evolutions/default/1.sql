# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table chat_group (
  chat_group_id                 integer auto_increment not null,
  name                          varchar(255),
  voice_room_id                 bigint default null,
  room_token                    varchar(255) default null,
  constraint pk_chat_group primary key (chat_group_id)
);

create table chat_group_user (
  chat_group_chat_group_id      integer not null,
  user_user_id                  integer not null,
  constraint pk_chat_group_user primary key (chat_group_chat_group_id,user_user_id)
);

create table country (
  country_id                    integer auto_increment not null,
  country_name                  varchar(255),
  isocode                       varchar(255),
  is_valid                      boolean default false not null,
  constraint pk_country primary key (country_id)
);

create table destination (
  destination_id                integer auto_increment not null,
  destination_name              varchar(255),
  destination_type_destination_type_id integer,
  destination_district          varchar(255),
  destination_lat               double,
  destination_lon               double,
  destination_country_country_id integer,
  destination_owner             integer,
  is_public                     boolean default false not null,
  deleted_expiry                timestamp,
  deleted                       BOOLEAN DEFAULT FALSE not null,
  constraint uq_destination_destination_name_destination_country_count_1 unique (destination_name,destination_country_country_id,destination_type_destination_type_id,is_public,destination_owner),
  constraint pk_destination primary key (destination_id)
);

create table destination_photo (
  destination_photo_id          integer auto_increment not null,
  destination_destination_id    integer,
  personal_photo_photo_id       integer,
  deleted_expiry                timestamp,
  deleted                       BOOLEAN DEFAULT FALSE not null,
  constraint pk_destination_photo primary key (destination_photo_id)
);

create table destination_proposal (
  destination_proposal_id       integer auto_increment not null,
  destination_destination_id    integer,
  deleted_expiry                timestamp,
  user_user_id                  integer,
  deleted                       BOOLEAN DEFAULT FALSE not null,
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

create table message (
  message_id                    integer auto_increment not null,
  chat_group_chat_group_id      integer,
  contents                      varchar(255),
  user_user_id                  integer,
  timestamp                     timestamp,
  constraint pk_message primary key (message_id)
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
  is_public                     boolean default false not null,
  is_primary                    boolean default false not null,
  is_cover                      boolean default false not null,
  filename_hash                 varchar(255),
  thumbnail_name                varchar(255),
  deleted_expiry                timestamp,
  deleted                       BOOLEAN DEFAULT FALSE not null,
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

create table treasure_hunt (
  treasure_hunt_id              integer auto_increment not null,
  treasure_hunt_name            varchar(255),
  treasure_hunt_destination_destination_id integer,
  owner_user_id                 integer,
  riddle                        varchar(255),
  start_date                    timestamp,
  end_date                      timestamp,
  deleted_expiry                timestamp,
  deleted                       BOOLEAN DEFAULT FALSE not null,
  constraint pk_treasure_hunt primary key (treasure_hunt_id)
);

create table trip_node (
  dtype                         varchar(31) not null,
  trip_node_id                  integer auto_increment not null,
  deleted_expiry                timestamp,
  deleted                       BOOLEAN DEFAULT FALSE not null,
  name                          varchar(255),
  destination_destination_id    integer,
  arrival_date                  timestamp,
  arrival_time                  integer,
  departure_date                timestamp,
  departure_time                integer,
  constraint pk_trip_node primary key (trip_node_id)
);

create table trip_node_user_role (
  trip_node_trip_node_id        integer not null,
  user_role_user_role_id        integer not null,
  constraint pk_trip_node_user_role primary key (trip_node_trip_node_id,user_role_user_role_id)
);

create table trip_node_parent (
  trip_node_child_id            integer not null,
  trip_node_parent_id           integer not null,
  constraint pk_trip_node_parent primary key (trip_node_child_id,trip_node_parent_id)
);

create table trip_node_user (
  trip_node_trip_node_id        integer not null,
  user_user_id                  integer not null,
  constraint pk_trip_node_user primary key (trip_node_trip_node_id,user_user_id)
);

create table user (
  user_id                       integer auto_increment not null,
  first_name                    varchar(255),
  middle_name                   varchar(255),
  last_name                     varchar(255),
  date_of_birth                 timestamp,
  gender                        varchar(255),
  email                         varchar(255),
  profile_photo_photo_id        integer,
  cover_photo_photo_id          integer,
  password_hash                 varchar(255),
  token                         varchar(255),
  deleted_expiry                timestamp,
  timestamp                     timestamp not null,
  deleted                       BOOLEAN DEFAULT FALSE not null,
  constraint uq_user_email unique (email),
  constraint uq_user_profile_photo_photo_id unique (profile_photo_photo_id),
  constraint uq_user_cover_photo_photo_id unique (cover_photo_photo_id),
  constraint uq_user_user_id unique (user_id),
  constraint pk_user primary key (user_id)
);

create table user_chat_group (
  user_user_id                  integer not null,
  chat_group_chat_group_id      integer not null,
  constraint pk_user_chat_group primary key (user_user_id,chat_group_chat_group_id)
);

create table user_role (
  user_role_id                  integer auto_increment not null,
  user_user_id                  integer,
  role_role_id                  integer,
  constraint pk_user_role primary key (user_role_id)
);

create index ix_destination_destination_name on destination (destination_name);
create index ix_chat_group_user_chat_group on chat_group_user (chat_group_chat_group_id);
alter table chat_group_user add constraint fk_chat_group_user_chat_group foreign key (chat_group_chat_group_id) references chat_group (chat_group_id) on delete restrict on update restrict;

create index ix_chat_group_user_user on chat_group_user (user_user_id);
alter table chat_group_user add constraint fk_chat_group_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_destination_destination_type_destination_type_id on destination (destination_type_destination_type_id);
alter table destination add constraint fk_destination_destination_type_destination_type_id foreign key (destination_type_destination_type_id) references destination_type (destination_type_id) on delete restrict on update restrict;

create index ix_destination_destination_country_country_id on destination (destination_country_country_id);
alter table destination add constraint fk_destination_destination_country_country_id foreign key (destination_country_country_id) references country (country_id) on delete restrict on update restrict;

create index ix_destination_photo_destination_destination_id on destination_photo (destination_destination_id);
alter table destination_photo add constraint fk_destination_photo_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_destination_photo_personal_photo_photo_id on destination_photo (personal_photo_photo_id);
alter table destination_photo add constraint fk_destination_photo_personal_photo_photo_id foreign key (personal_photo_photo_id) references personal_photo (photo_id) on delete restrict on update restrict;

create index ix_destination_proposal_destination_destination_id on destination_proposal (destination_destination_id);
alter table destination_proposal add constraint fk_destination_proposal_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_destination_proposal_user_user_id on destination_proposal (user_user_id);
alter table destination_proposal add constraint fk_destination_proposal_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_destination_proposal_traveller_type_destination_proposal on destination_proposal_traveller_type (destination_proposal_destination_proposal_id);
alter table destination_proposal_traveller_type add constraint fk_destination_proposal_traveller_type_destination_proposal foreign key (destination_proposal_destination_proposal_id) references destination_proposal (destination_proposal_id) on delete restrict on update restrict;

create index ix_destination_proposal_traveller_type_traveller_type on destination_proposal_traveller_type (traveller_type_traveller_type_id);
alter table destination_proposal_traveller_type add constraint fk_destination_proposal_traveller_type_traveller_type foreign key (traveller_type_traveller_type_id) references traveller_type (traveller_type_id) on delete restrict on update restrict;

create index ix_message_chat_group_chat_group_id on message (chat_group_chat_group_id);
alter table message add constraint fk_message_chat_group_chat_group_id foreign key (chat_group_chat_group_id) references chat_group (chat_group_id) on delete restrict on update restrict;

create index ix_message_user_user_id on message (user_user_id);
alter table message add constraint fk_message_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

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

create index ix_treasure_hunt_treasure_hunt_destination_destination_id on treasure_hunt (treasure_hunt_destination_destination_id);
alter table treasure_hunt add constraint fk_treasure_hunt_treasure_hunt_destination_destination_id foreign key (treasure_hunt_destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_treasure_hunt_owner_user_id on treasure_hunt (owner_user_id);
alter table treasure_hunt add constraint fk_treasure_hunt_owner_user_id foreign key (owner_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_trip_node_destination_destination_id on trip_node (destination_destination_id);
alter table trip_node add constraint fk_trip_node_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

create index ix_trip_node_user_role_trip_node on trip_node_user_role (trip_node_trip_node_id);
alter table trip_node_user_role add constraint fk_trip_node_user_role_trip_node foreign key (trip_node_trip_node_id) references trip_node (trip_node_id) on delete restrict on update restrict;

create index ix_trip_node_user_role_user_role on trip_node_user_role (user_role_user_role_id);
alter table trip_node_user_role add constraint fk_trip_node_user_role_user_role foreign key (user_role_user_role_id) references user_role (user_role_id) on delete restrict on update restrict;

create index ix_trip_node_parent_trip_node_1 on trip_node_parent (trip_node_child_id);
alter table trip_node_parent add constraint fk_trip_node_parent_trip_node_1 foreign key (trip_node_child_id) references trip_node (trip_node_id) on delete restrict on update restrict;

create index ix_trip_node_parent_trip_node_2 on trip_node_parent (trip_node_parent_id);
alter table trip_node_parent add constraint fk_trip_node_parent_trip_node_2 foreign key (trip_node_parent_id) references trip_node (trip_node_id) on delete restrict on update restrict;

create index ix_trip_node_user_trip_node on trip_node_user (trip_node_trip_node_id);
alter table trip_node_user add constraint fk_trip_node_user_trip_node foreign key (trip_node_trip_node_id) references trip_node (trip_node_id) on delete restrict on update restrict;

create index ix_trip_node_user_user on trip_node_user (user_user_id);
alter table trip_node_user add constraint fk_trip_node_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

alter table user add constraint fk_user_profile_photo_photo_id foreign key (profile_photo_photo_id) references personal_photo (photo_id) on delete restrict on update restrict;

alter table user add constraint fk_user_cover_photo_photo_id foreign key (cover_photo_photo_id) references personal_photo (photo_id) on delete restrict on update restrict;

create index ix_user_chat_group_user on user_chat_group (user_user_id);
alter table user_chat_group add constraint fk_user_chat_group_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_user_chat_group_chat_group on user_chat_group (chat_group_chat_group_id);
alter table user_chat_group add constraint fk_user_chat_group_chat_group foreign key (chat_group_chat_group_id) references chat_group (chat_group_id) on delete restrict on update restrict;

create index ix_user_role_user_user_id on user_role (user_user_id);
alter table user_role add constraint fk_user_role_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_user_role_role_role_id on user_role (role_role_id);
alter table user_role add constraint fk_user_role_role_role_id foreign key (role_role_id) references role (role_id) on delete restrict on update restrict;


# --- !Downs

alter table chat_group_user drop constraint if exists fk_chat_group_user_chat_group;
drop index if exists ix_chat_group_user_chat_group;

alter table chat_group_user drop constraint if exists fk_chat_group_user_user;
drop index if exists ix_chat_group_user_user;

alter table destination drop constraint if exists fk_destination_destination_type_destination_type_id;
drop index if exists ix_destination_destination_type_destination_type_id;

alter table destination drop constraint if exists fk_destination_destination_country_country_id;
drop index if exists ix_destination_destination_country_country_id;

alter table destination_photo drop constraint if exists fk_destination_photo_destination_destination_id;
drop index if exists ix_destination_photo_destination_destination_id;

alter table destination_photo drop constraint if exists fk_destination_photo_personal_photo_photo_id;
drop index if exists ix_destination_photo_personal_photo_photo_id;

alter table destination_proposal drop constraint if exists fk_destination_proposal_destination_destination_id;
drop index if exists ix_destination_proposal_destination_destination_id;

alter table destination_proposal drop constraint if exists fk_destination_proposal_user_user_id;
drop index if exists ix_destination_proposal_user_user_id;

alter table destination_proposal_traveller_type drop constraint if exists fk_destination_proposal_traveller_type_destination_proposal;
drop index if exists ix_destination_proposal_traveller_type_destination_proposal;

alter table destination_proposal_traveller_type drop constraint if exists fk_destination_proposal_traveller_type_traveller_type;
drop index if exists ix_destination_proposal_traveller_type_traveller_type;

alter table message drop constraint if exists fk_message_chat_group_chat_group_id;
drop index if exists ix_message_chat_group_chat_group_id;

alter table message drop constraint if exists fk_message_user_user_id;
drop index if exists ix_message_user_user_id;

alter table nationality drop constraint if exists fk_nationality_nationality_country_country_id;

alter table nationality_user drop constraint if exists fk_nationality_user_nationality;
drop index if exists ix_nationality_user_nationality;

alter table nationality_user drop constraint if exists fk_nationality_user_user;
drop index if exists ix_nationality_user_user;

alter table passport drop constraint if exists fk_passport_country_country_id;

alter table passport_user drop constraint if exists fk_passport_user_passport;
drop index if exists ix_passport_user_passport;

alter table passport_user drop constraint if exists fk_passport_user_user;
drop index if exists ix_passport_user_user;

alter table personal_photo drop constraint if exists fk_personal_photo_user_user_id;
drop index if exists ix_personal_photo_user_user_id;

alter table role_user drop constraint if exists fk_role_user_role;
drop index if exists ix_role_user_role;

alter table role_user drop constraint if exists fk_role_user_user;
drop index if exists ix_role_user_user;

alter table traveller_type_user drop constraint if exists fk_traveller_type_user_traveller_type;
drop index if exists ix_traveller_type_user_traveller_type;

alter table traveller_type_user drop constraint if exists fk_traveller_type_user_user;
drop index if exists ix_traveller_type_user_user;

alter table traveller_type_destination drop constraint if exists fk_traveller_type_destination_traveller_type;
drop index if exists ix_traveller_type_destination_traveller_type;

alter table traveller_type_destination drop constraint if exists fk_traveller_type_destination_destination;
drop index if exists ix_traveller_type_destination_destination;

alter table treasure_hunt drop constraint if exists fk_treasure_hunt_treasure_hunt_destination_destination_id;
drop index if exists ix_treasure_hunt_treasure_hunt_destination_destination_id;

alter table treasure_hunt drop constraint if exists fk_treasure_hunt_owner_user_id;
drop index if exists ix_treasure_hunt_owner_user_id;

alter table trip_node drop constraint if exists fk_trip_node_destination_destination_id;
drop index if exists ix_trip_node_destination_destination_id;

alter table trip_node_user_role drop constraint if exists fk_trip_node_user_role_trip_node;
drop index if exists ix_trip_node_user_role_trip_node;

alter table trip_node_user_role drop constraint if exists fk_trip_node_user_role_user_role;
drop index if exists ix_trip_node_user_role_user_role;

alter table trip_node_parent drop constraint if exists fk_trip_node_parent_trip_node_1;
drop index if exists ix_trip_node_parent_trip_node_1;

alter table trip_node_parent drop constraint if exists fk_trip_node_parent_trip_node_2;
drop index if exists ix_trip_node_parent_trip_node_2;

alter table trip_node_user drop constraint if exists fk_trip_node_user_trip_node;
drop index if exists ix_trip_node_user_trip_node;

alter table trip_node_user drop constraint if exists fk_trip_node_user_user;
drop index if exists ix_trip_node_user_user;

alter table user drop constraint if exists fk_user_profile_photo_photo_id;

alter table user drop constraint if exists fk_user_cover_photo_photo_id;

alter table user_chat_group drop constraint if exists fk_user_chat_group_user;
drop index if exists ix_user_chat_group_user;

alter table user_chat_group drop constraint if exists fk_user_chat_group_chat_group;
drop index if exists ix_user_chat_group_chat_group;

alter table user_role drop constraint if exists fk_user_role_user_user_id;
drop index if exists ix_user_role_user_user_id;

alter table user_role drop constraint if exists fk_user_role_role_role_id;
drop index if exists ix_user_role_role_role_id;

drop table if exists chat_group;

drop table if exists chat_group_user;

drop table if exists country;

drop table if exists destination;

drop table if exists destination_photo;

drop table if exists destination_proposal;

drop table if exists destination_proposal_traveller_type;

drop table if exists destination_type;

drop table if exists message;

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

drop table if exists treasure_hunt;

drop table if exists trip_node;

drop table if exists trip_node_user_role;

drop table if exists trip_node_parent;

drop table if exists trip_node_user;

drop table if exists user;

drop table if exists user_chat_group;

drop table if exists user_role;

drop index if exists ix_destination_destination_name;
