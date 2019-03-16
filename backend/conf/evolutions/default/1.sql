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

create table destination (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  destination_type              integer,
  district                      varchar(255),
  latitude                      double,
  longitude                     double,
  country                       varchar(255),
  constraint ck_destination_destination_type check ( destination_type in (0,1,2,3,4,5,6)),
  constraint pk_destination primary key (id)
);

create table traveller (
  id                            bigint auto_increment not null,
  gender                        varchar(255),
  first_name                    varchar(255),
  middle_name                   varchar(255),
  last_name                     varchar(255),
  nationalities                 varchar(255),
  birthday                      timestamp,
  email_address                 varchar(255),
  traveller_type                integer,
  passports                     varchar(255),
  timestamp                     timestamp,
  password                      varchar(255),
  constraint ck_traveller_traveller_type check ( traveller_type in (0,1,2,3,4,5,6)),
  constraint pk_traveller primary key (id)
);

create index ix_computer_company_id on computer (company_id);
alter table computer add constraint fk_computer_company_id foreign key (company_id) references company (id) on delete restrict on update restrict;


# --- !Downs

alter table computer drop constraint if exists fk_computer_company_id;
drop index if exists ix_computer_company_id;

drop table if exists company;

drop table if exists computer;

drop table if exists destination;

drop table if exists traveller;

