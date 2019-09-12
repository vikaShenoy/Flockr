-- apply changes
create table trip_node_user_role (
  trip_node_trip_node_id        integer not null,
  user_role_user_role_id        integer not null,
  constraint pk_trip_node_user_role primary key (trip_node_trip_node_id,user_role_user_role_id)
);


alter table user_role add column user_user_id integer;
alter table user_role add column role_role_id integer;

create index ix_trip_node_user_role_trip_node on trip_node_user_role (trip_node_trip_node_id);
alter table trip_node_user_role add constraint fk_trip_node_user_role_trip_node foreign key (trip_node_trip_node_id) references trip_node (trip_node_id) on delete restrict on update restrict;

create index ix_trip_node_user_role_user_role on trip_node_user_role (user_role_user_role_id);
alter table trip_node_user_role add constraint fk_trip_node_user_role_user_role foreign key (user_role_user_role_id) references user_role (user_role_id) on delete restrict on update restrict;


create index ix_user_role_user_user_id on user_role (user_user_id);
alter table user_role add constraint fk_user_role_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_user_role_role_role_id on user_role (role_role_id);
alter table user_role add constraint fk_user_role_role_role_id foreign key (role_role_id) references role (role_id) on delete restrict on update restrict;

