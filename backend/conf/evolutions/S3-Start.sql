

create table country (
  country_id                    integer auto_increment not null,
  country_name                  varchar(255),
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
  constraint pk_destination primary key (destination_id)
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
  nationality_country           varchar(255),
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
  constraint pk_passport primary key (passport_id)
);

create table passport_user (
  passport_passport_id          integer not null,
  user_user_id                  integer not null,
  constraint pk_passport_user primary key (passport_passport_id,user_user_id)
);

create table role (
  role_id                       integer auto_increment not null,
  role_type                     varchar(255),
  constraint pk_role primary key (role_id)
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
  arrival_time                  integer not null,
  departure_date                datetime(6),
  departure_time                integer not null,
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
  password_hash                 varchar(255),
  token                         varchar(255),
  timestamp                     datetime(6) not null,
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

create index ix_district_country_country_id on district (country_country_id);
alter table district add constraint fk_district_country_country_id foreign key (country_country_id) references country (country_id) on delete restrict on update restrict;

create index ix_nationality_user_nationality on nationality_user (nationality_nationality_id);
alter table nationality_user add constraint fk_nationality_user_nationality foreign key (nationality_nationality_id) references nationality (nationality_id) on delete restrict on update restrict;

create index ix_nationality_user_user on nationality_user (user_user_id);
alter table nationality_user add constraint fk_nationality_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_passport_user_passport on passport_user (passport_passport_id);
alter table passport_user add constraint fk_passport_user_passport foreign key (passport_passport_id) references passport (passport_id) on delete restrict on update restrict;

create index ix_passport_user_user on passport_user (user_user_id);
alter table passport_user add constraint fk_passport_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_traveller_type_user_traveller_type on traveller_type_user (traveller_type_traveller_type_id);
alter table traveller_type_user add constraint fk_traveller_type_user_traveller_type foreign key (traveller_type_traveller_type_id) references traveller_type (traveller_type_id) on delete restrict on update restrict;

create index ix_traveller_type_user_user on traveller_type_user (user_user_id);
alter table traveller_type_user add constraint fk_traveller_type_user_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_trip_user_user_id on trip (user_user_id);
alter table trip add constraint fk_trip_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_trip_destination_trip_trip_id on trip_destination (trip_trip_id);
alter table trip_destination add constraint fk_trip_destination_trip_trip_id foreign key (trip_trip_id) references trip (trip_id) on delete restrict on update restrict;

create index ix_trip_destination_destination_destination_id on trip_destination (destination_destination_id);
alter table trip_destination add constraint fk_trip_destination_destination_destination_id foreign key (destination_destination_id) references destination (destination_id) on delete restrict on update restrict;

