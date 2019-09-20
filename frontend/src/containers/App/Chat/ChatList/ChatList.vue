<template>
  <v-list>
    <v-list-tile
      class="chat-list-item"
      @click="$emit('goToChatGroup', chat.chatGroupId)"
      v-for="chat in chats"
      v-bind:key="chat.chatGroupId"
    >

      <div>
        <h4>{{ chat.name }}</h4>
      </div>
    </v-list-tile>
  </v-list>

</template>

<script>
export default {
  props: {
    chats: Array 
  }
};
</script>

<style lang="scss" scoped>
@import "../../../../styles/_variables.scss";

.chat-list-item {
  &:not(:last-child) {
    border-bottom: 1px solid rgba(0,0,0,0.12);
 }

  h4 {
    color: $secondary;
  }
}
</style>