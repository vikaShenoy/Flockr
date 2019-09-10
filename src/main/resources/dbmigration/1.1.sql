-- apply changes
create table trip_user_role (
  trip_user_role_id             integer auto_increment not null,
  user_user_id                  integer,
  trip_trip_node_id             integer,
  role_role_id                  integer,
  constraint pk_trip_user_role primary key (trip_user_role_id)
);

create index ix_trip_user_role_user_user_id on trip_user_role (user_user_id);
alter table trip_user_role add constraint fk_trip_user_role_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_trip_user_role_trip_trip_node_id on trip_user_role (trip_trip_node_id);
alter table trip_user_role add constraint fk_trip_user_role_trip_trip_node_id foreign key (trip_trip_node_id) references trip_node (trip_node_id) on delete restrict on update restrict;

create index ix_trip_user_role_role_role_id on trip_user_role (role_role_id);
alter table trip_user_role add constraint fk_trip_user_role_role_role_id foreign key (role_role_id) references role (role_id) on delete restrict on update restrict;

