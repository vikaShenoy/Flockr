<template>
  <div id="chat">
    <v-expansion-panel id="connected-users" :value="chatIsOpen ? 1 : 0" readonly>
      <v-expansion-panel-content id="header">
        <template v-slot:header>
          <div v-if="!chatIsOpen">
            <v-btn
                    v-if="!isShowingCreateChat"
                    flat
                    class="header-button"
                    color="secondary"
                    @click="isShowingCreateChat = true"
            >
              <v-icon>add</v-icon>
            </v-btn>
            <v-btn
                    v-if="isShowingCreateChat"
                    flat
                    class="header-button"
                    color="secondary"
                    @click="isShowingCreateChat = false"
            >
              <v-icon>arrow_back</v-icon>
            </v-btn>
          </div>

          <h4 id="title" v-if="currentChatId == null">Chat</h4>

          <div v-else id="chat-group-title">
            <v-icon color="secondary" id="back-to-chats-icon" @click="goBackToChats()"
                    class="hover-white">chevron_left</v-icon>

            <v-spacer align="center"><h4 id="title">{{ getChatGroupName }}</h4></v-spacer>

            <VoiceChat :chatGroup="getCurrentChat()"/>
          </div>


        </template>

        <v-icon
          slot="actions"
          color="secondary"
          id="toggle-chat"
          class="hover-white"
          @click="chatIsOpen = !chatIsOpen"
        >$vuetify.icons.expand</v-icon>

        <v-card id="chats">

          <div v-if="isShowingCreateChat">
            <CreateChat
            isShowing.sync = isShowingCreateChat
            @chatCreated="chatCreated"
            />
          </div>

          <div v-else>
            <ChatList
                    v-if="currentChatId === null"
                    :chats="chats"
                    @goToChatGroup="goToChatGroup"
            />
            <ChatGroup @newMessage = "newMessage" v-else :chatGroup="getCurrentChat()" @messagesRetrieved="messagesRetrieved" />
          </div>

        </v-card>
      </v-expansion-panel-content>
    </v-expansion-panel>

  </div>
</template>

<script>
import CreateChat from "../Chat/CreateChat/CreateChat";
import { getChats } from "./ChatService";
import ChatList from "./ChatList/ChatList";
import ChatGroup from "./ChatGroup/ChatGroup";
import UserStore from "../../../stores/UserStore";
import VoiceChat from "./VoiceChat/VoiceChat";

export default {
  components: {
    ChatList,
    ChatGroup,
    VoiceChat,
    CreateChat
  },
  data() {
    return {
      chats: [],
      isShowingCreateChat: false,
      // Specifies the current chat the user is viewing. If null then list is being viewed
      currentChatId: null,
      chatIsOpen: true 
    };
  },
  mounted() {
    this.getChats();
    this.listenOnMessage();
  },
  methods: {
    /**
     * Called when the createChat component emits a chatCreated event.
     * Closes the chat creation component and refreshes the list of chats to display the new chat.
     */
    chatCreated() {
      this.isShowingCreateChat = false;
      this.getChats();
    },
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
     * Goes back to the chats list.
     */
    goBackToChats() {
      this.currentChatId = null;
      event.stopPropagation();
    },
    /**
     * Gets called when new message is created. Adds message to data.
     */
    newMessage(message) {
      const currentChat = this.getCurrentChat();
      const newMessages = [...currentChat.messages, message];
      currentChat.messages = newMessages;
    },
    /**
     * Gets the current chat that the user is viewing
     */
    getCurrentChat() {
      return this.chats.find(chat => chat.chatGroupId === this.currentChatId);
    },
    /**
     * Gets emitted when a messages is retrieved
     */
    messagesRetrieved(messages) {
      this.$set(this.getCurrentChat(), "messages", messages);
    },
    /**
     * Listens on any incoming messages and adds it to the corresponding chat
     */
    listenOnMessage() {
        UserStore.data.socket.addEventListener("message", (event) => {
          const message = JSON.parse(event.data);
          if (message.type === "send-chat-message") {
              for (const chat of this.chats) {
                  if (chat.chatGroupId === message.chatGroupId && chat.messages) {
                      chat.messages.push({
                          contents : message.message,
                          user: message.sender,
                          messageId: message.messageId
                      })
                  }
              }
          }
        });
    },
  },
  computed: {
    /**
     * Gets the name of the currently viewed chat
     */
    getChatGroupName() {
      if (this.currentChatId == null || this.chats.length === 0) return "";
      const chatGroup = this.chats.find(chat => chat.chatGroupId === this.currentChatId);
      return chatGroup.name;
    }
  }
};
</script>

<style lang="scss">
#chat {
  .v-expansion-panel__header {
    cursor: default;
  }
}

</style>

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

#toggle-chat {
  cursor: pointer;
}

.hover-white {
  transition: 0.1s ease-in all;
  &:hover {
    color: white !important; 
  }
}

.header-button {
  width: 5px;
  min-width: 5px;
  margin: 5px;
}

</style>