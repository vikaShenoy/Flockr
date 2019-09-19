-- apply changes
create table user_chat_group (
  user_user_id                  integer not null,
  chat_group_chat_group_id      integer not null,
  constraint pk_user_chat_group primary key (user_user_id,chat_group_chat_group_id)
);

alter table message add column user_user_id integer;

create index ix_user_chat_group_user on user_chat_group (user_user_id);
alter table user_chat_group add constraint fk_user_chat_group_user foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

create index ix_user_chat_group_chat_group on user_chat_group (chat_group_chat_group_id);
alter table user_chat_group add constraint fk_user_chat_group_chat_group foreign key (chat_group_chat_group_id) references chat_group (chat_group_id) on delete restrict on update restrict;

alter table message add constraint fk_message_user_user_id foreign key (user_user_id) references user (user_id) on delete restrict on update restrict;

