# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table company (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint pk_company primary key (id)
);

create table computer (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  introduced                    timestamp,
  discontinued                  timestamp,
  company_id                    bigint,
  constraint pk_computer primary key (id)
);

create table country (
  country_id                    integer auto_increment not null,
  country_name                  varchar(255),
  constraint pk_country primary key (country_id)
);

create table destination (
  dest_id                       integer auto_increment not null,
  dest_name                     varchar(255),
  dest_type_dest_type_id        integer,
  dest_district_district_id     integer,
  dest_lat                      double,
  dest_lon                      double,
  dest_country_country_id       integer,
  constraint uq_destination_dest_type_dest_type_id unique (dest_type_dest_type_id),
  constraint uq_destination_dest_district_district_id unique (dest_district_district_id),
  constraint uq_destination_dest_country_country_id unique (dest_country_country_id),
  constraint pk_destination primary key (dest_id)
);

create table destination_type (
  dest_type_id                  integer auto_increment not null,
  dest_type_name                varchar(255),
  constraint pk_destination_type primary key (dest_type_id)
);

create table district (
  district_id                   integer auto_increment not null,
  district_name                 varchar(255),
  constraint pk_district primary key (district_id)
);

create table gender (
  gender_id                     integer auto_increment not null,
  gender_name                   varchar(255),
  constraint pk_gender primary key (gender_id)
);

create table nationality (
  nationality_id                integer auto_increment not null,
  user_user_id                  integer not null,
  nationality_country           varchar(255),
  constraint pk_nationality primary key (nationality_id)
);

create table passport (
  passport_id                   integer auto_increment not null,
  user_user_id                  integer not null,
  passport_country              varchar(255),
  constraint pk_passport primary key (passport_id)
);

create table role (
  role_id                       integer auto_increment not null,
  role_type                     varchar(255),
  constraint pk_role primary key (role_id)
);

create table traveller_type (
  traveller_type_id             integer auto_increment not null,
  user_user_id                  integer not null,
  traveller_type_name           varchar(255),
  constraint pk_traveller_type primary key (traveller_type_id)
);

create table trip (
  trip_id                       integer auto_increment not null,
  user_user_id                  integer,
  constraint uq_trip_user_user_id unique (user_user_id),
  constraint pk_trip primary key (trip_id)
);

create table trip_destination (
  trip_destination_id           integer auto_increment not null,
  trip_trip_id                  integer not null,
  destination_dest_id           integer,
  trip_dest_arrival             timestamp,
  trip_dest_departure           timestamp,
  constraint uq_trip_destination_destination_dest_id unique (destination_dest_id),
  constraint pk_trip_destination primary key (trip_destination_id)
);

create table user (
  user_id                       integer auto_increment not null,
  first_name                    varchar(255),
  middle_name                   varchar(255),
  last_name                     varchar(255),
  date_of_birth                 timestamp,
  gender                        varchar(255),
  email                         varchar(255),
  timestamp                     timestamp,
  password                      varchar(255),
  token                         varchar(255),
  constraint pk_user primary key (user_id)
);

create table user_nationality (
  user_nationality_id           integer auto_increment not null,
  nationality_id                integer not null,
  user_id                       integer not null,
  constraint pk_user_nationality primary key (user_nationality_id)
);

create table user_passport (
  user_passport_id              integer auto_increment not null,
  passport_id                   integer not null,
  user_id                       integer not null,
  constraint pk_user_passport primary key (user_passport_id)
);

create table user_role (
  user_role_id                  integer auto_increment not null,
  role_id                       integer not null,
  user_id                       integer not null,
  constraint pk_user_role primary key (user_role_id)
);

create table user_traveller_types (
  user_traveller_type_id        integer auto_increment not null,
  traveller_type_id             integer not null,
  user_id                       integer not null,
  constraint pk_user_traveller_types primary key (user_traveller_type_id)
);

create index ix_computer_company_id on computer (company_id);
alter table computer add constraint fk_computer_company_id foreign key (company_id) references company (id) on delete restrict on update restrict;

alter table destination add constraint fk_destination_dest_type_dest_type_id foreign key (dest_type_dest_type_id) references destination_type (dest_type_id) on delete restrict on update restrict;

alter table destination add constraint fk_destination_dest_district_district_id foreign key (dest_district_district_id) references district (district_id) on delete restrict on update restrict;

alter table destination add constraint fk_destination_dest_country_country_id foreign key (dest_country_country_id) references country (country_id) on delete restrict on update restrict;

create index ix_nationality_user_user_id on nationality (user_user_id);
alter table nationality add constraint fk_nationality_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_passport_user_user_id on passport (user_user_id);
alter table passport add constraint fk_passport_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_traveller_type_user_user_id on traveller_type (user_user_id);
alter table traveller_type add constraint fk_traveller_type_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

alter table trip add constraint fk_trip_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_trip_destination_trip_trip_id on trip_destination (trip_trip_id);
alter table trip_destination add constraint fk_trip_destination_trip_trip_id foreign key (trip_trip_id) references trip (trip_id) on delete restrict on update restrict;

alter table trip_destination add constraint fk_trip_destination_destination_dest_id foreign key (destination_dest_id) references destination (dest_id) on delete restrict on update restrict;


# --- !Downs

alter table computer drop constraint if exists fk_computer_company_id;
drop index if exists ix_computer_company_id;

alter table destination drop constraint if exists fk_destination_dest_type_dest_type_id;

alter table destination drop constraint if exists fk_destination_dest_district_district_id;

alter table destination drop constraint if exists fk_destination_dest_country_country_id;

alter table nationality drop constraint if exists fk_nationality_user_user_id;
drop index if exists ix_nationality_user_user_id;

alter table passport drop constraint if exists fk_passport_user_user_id;
drop index if exists ix_passport_user_user_id;

alter table traveller_type drop constraint if exists fk_traveller_type_user_user_id;
drop index if exists ix_traveller_type_user_user_id;

alter table trip drop constraint if exists fk_trip_user_user_id;

alter table trip_destination drop constraint if exists fk_trip_destination_trip_trip_id;
drop index if exists ix_trip_destination_trip_trip_id;

alter table trip_destination drop constraint if exists fk_trip_destination_destination_dest_id;

drop table if exists company;

drop table if exists computer;

drop table if exists country;

drop table if exists destination;

drop table if exists destination_type;

drop table if exists district;

drop table if exists gender;

drop table if exists nationality;

drop table if exists passport;

drop table if exists role;

drop table if exists traveller_type;

drop table if exists trip;

drop table if exists trip_destination;

drop table if exists user;

drop table if exists user_nationality;

drop table if exists user_passport;

drop table if exists user_role;

drop table if exists user_traveller_types;

