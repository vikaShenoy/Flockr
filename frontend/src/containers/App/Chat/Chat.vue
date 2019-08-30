<template>
  <div id="chat">
    <v-expansion-panel id="connected-users">
      <v-expansion-panel-content id="header">
        <template v-slot:header>
          <h4 id="title" v-if="currentChatId == null">Chat</h4>
          
          <div v-else id="chat-group-title">
            <v-icon color="secondary" id="back-to-chats-icon" @click="goBackToChats()">chevron_left</v-icon>

            <v-spacer align="center"><h4 id="title">{{ getChatGroupName }}</h4></v-spacer>
          </div>
        </template>

        <v-icon
          slot="actions"
          color="secondary"
        >$vuetify.icons.expand</v-icon>

        <v-card id="chats">
          <ChatList
            v-if="currentChatId === null"
            :chats="chats"
            @goToChatGroup="goToChatGroup"
          />
          <ChatGroup @newMessage = "newMessage" v-else :chatGroup="getCurrentChat()" @messagesRetrieved="messagesRetrieved" />
        </v-card>

      </v-expansion-panel-content>
    </v-expansion-panel>

  </div>
</template>

<script>
import { getChats } from "./ChatService";
import ChatList from "./ChatList/ChatList";
import ChatGroup from "./ChatGroup/ChatGroup";

export default {
  components: {
    ChatList,
    ChatGroup
  },
  data() {
    return {
      chats: [],
      // Specifies the current chat the user is viewing. If null then list is being viewed
      currentChatId: null
    };
  },
  mounted() {
    this.getChats();
  },
  methods: {
    /**
     * Shows an error snackbar
     * @param {string} message The message to show
     */
    showErrorSnackbar(message) {
      this.$root.$emit("show-snackbar", {
        message,
        color: "error",
        timeout: 2000
      });
    },
    /**
     * Gets a list of all the chats that a user is apart of
     */
    async getChats() {
      try {
        const chats = await getChats();
        this.chats = chats;
      } catch (e) {
        console.log(e);
        this.showErrorSnackbar("Error getting chats");
      }
    },
    /**
     * Goes to a specific chat group
     * @param {number} chatGroupId The chat group to go to
     */
    goToChatGroup(chatGroupId) {
      this.currentChatId = chatGroupId;
    },
    /**
     * Goes back to the chats list
     */
    goBackToChats() {
      this.currentChatId = null;
      event.stopPropagation();
    },
    /**
     * Gets called when new message is created. Adds message to data
     */
    newMessage(message) {
      const currentChat = this.getCurrentChat();
      const newMessages = [...currentChat.messages, message];
      currentChat.messages = newMessages;
    },
    getCurrentChat() {
      return this.chats.find(chat => chat.chatGroupId === this.currentChatId);
    },
    messagesRetrieved(messages) {
      this.$set(this.getCurrentChat(), "messages", messages);
    }
  },
  computed: {
    getChatGroupName() {
      if (this.currentChatId == null || this.chats.length === 0) return "";
      const chatGroup = this.chats.find(chat => chat.chatGroupId === this.currentChatId);
      return chatGroup.name;
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../../styles/_variables.scss";

#chat {
  bottom: 0;
  position: fixed;
  z-index: 2;
  width: 400px;
}

#header {
  background-color: $primary;
}

#title {
  color: $secondary;
}

#chats {
  height: 400px;
  overflow: auto;
}

#chat-group-title {
  display: flex;
}

</style>