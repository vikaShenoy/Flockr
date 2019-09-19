<template>
  <div class="message">
    <UserAvatar
      :isConnected="isUserConnected"
      :user="message.user"
      :size="30"
      :isOwnUser="isMessageFromSelf"
    />

    <!-- Message content -->
    <div
      class="content"
      :class="isMessageFromSelf ? 'your-message' : 'other-message'"
    >
      {{ message.contents }}
    </div>
  </div>
</template>

<script>
import UserStore from '../../../../../stores/UserStore';
import UserAvatar from "./UserAvatar";

export default {
  data() {
    return {
      userStore: UserStore
    }
  },
  components: { UserAvatar },
  props: {
    message: {
      required: true,
      chatGroup: Object,
      contents: String,
      messageId: Number,
      user: Object
    },
    isUserConnected: Boolean
  },
  computed: {
    /**
     * Return true if the message comes from the own user
     * @returns {Boolean} true if the message is from the own user
     */
    isMessageFromSelf() {
      return this.message.user.userId === this.userStore.data.userId;
    }
  }
}
</script>



<style lang="scss" scoped>
  @import "../../../../../styles/_variables.scss";

  .message {
    margin-bottom: 20px;

    .content {
      border-radius: 20px;
      color: #FFF;
      padding: 8px;
      max-width: 270px;
      word-wrap: break-word;
    }
  }

  .other-message {
    background-color: $text-light-grey !important;
    color: black !important;
    float: left;
  }

  .your-message {
    float: right;
    background-color: $secondary;
  }
</style>
