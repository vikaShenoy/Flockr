-- apply changes
alter table chat_group add column voice_room_id integer default null;
alter table chat_group add column room_token varchar(255) default null;

alter table message drop index uq_message_user_user_id;
