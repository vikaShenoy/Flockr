<template>
  <div id="messages">
    <Message
      v-for="message in messages" 
      :key="message.messageId"
      :message="message"
      :isUserConnected="isUserConnected(message.user)"
    />
  </div>
</template>

<script>
import Message from "./Message";

export default {
  props: {
    messages: Array,
    connectedUsers: Array
  },
  components: { Message },
  methods: {
    /**
     * Return true if the user is connected, false otherwise
     * @param {Object} user the user that we want to know if it's connected
     * @returns {Boolean} true if the user is connected, false otherwise
     */
    isUserConnected(user) {
      // to make sure it doesn't break if the API returns only some parts of the user
      // for one of the user objects
      return this.connectedUsers.filter(u => u.userId === user.userId).length > 0;
    }
  }
};
</script>

<style lang="scss" scoped>

#messages {
  padding: 15px;
  display: flex;
  flex-direction: column;
}

</style>