<template>
  <div id="chat">
    <v-expansion-panel id="connected-users" :value="chatIsOpen ? 1 : 0" readonly>
      <v-expansion-panel-content id="header">
        <template v-slot:header>
          <div v-if="!chatIsOpen">
            <v-icon
                    v-if="!currentChatId && !isShowingCreateChat"
                    flat
                    class="header-button"
                    color="secondary"
                    @click="isShowingCreateChat = true"
            >add</v-icon>
         </div>

          <h4 id="title" v-if="!currentChatId">Chat</h4>

          <div v-if="currentChatId || isShowingCreateChat || isShowingManageChat" id="chat-group-title">
            <v-icon color="secondary" id="back-to-chats-icon" @click="goBackToChats()"
                    class="hover-white">chevron_left</v-icon>

            <v-spacer align="center"><h4 id="title">{{ getChatGroupName }}</h4></v-spacer>

            <v-icon
              flat
              class="hover-white"
              color="secondary"
              v-on:click="isShowingManageChat = true"
            >settings
            </v-icon>

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
            isShowing.sync = "isShowingCreateChat"
            @createChat="createChat"
            />
          </div>

          <div v-else-if="isShowingManageChat">
            <ManageChat
              isShowing.sync = "isShowingManageChat"
              :chatGroup="getCurrentChat()"
              @editGroupChat="editGroupChat"
              @deleteGroupChat="deleteGroupChat"
            >

            </ManageChat>
          </div>

          <div v-else>
            <ChatList
                    v-if="currentChatId === null"
                    :chats="chats"
                    @goToChatGroup="goToChatGroup"
            />
            <ChatGroup @newMessage = "newMessage" v-else :chatGroup="getCurrentChat()"
                       @messagesRetrieved="messagesRetrieved"
                       @newMessages="newMessages"/>
          </div>

        </v-card>
      </v-expansion-panel-content>
    </v-expansion-panel>

  </div>
</template>

<script>
import CreateChat from "../Chat/CreateChat/CreateChat";
import {createChat, deleteChat, editChat, getChats} from "./ChatService";
import ChatList from "./ChatList/ChatList";
import ChatGroup from "./ChatGroup/ChatGroup";
import UserStore from "../../../stores/UserStore";
import VoiceChat from "./VoiceChat/VoiceChat";
import ManageChat from "./ManageChat/ManageChat";
import UndoRedo from "../../../components/UndoRedo/UndoRedo";
import Command from "../../../components/UndoRedo/Command";

export default {
  components: {
    ChatList,
    ChatGroup,
    VoiceChat,
    CreateChat,
    ManageChat,
  },
  data() {
    return {
      chats: [],
      isShowingCreateChat: false,
      isShowingManageChat: false,
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
     * Edit a chat group. Send request to backend. Show snackbar on success and return to
     * main chat screen. Show error snackbar if there's an error.
     * @param chatGroupId id of the chat to edit.
     * @param chatName name of the group chat.
     * @param users array of user objects to be in the chat.
     */
    async editGroupChat(chatGroupId, chatName, users) {
      try {
        const ownId = Number(localStorage.getItem("ownUserId"));
        let userIds = users.map(user => user.userId);
        userIds.push(ownId);
        const res = await editChat(chatGroupId, chatName, userIds);
        this.getChats();
        this.goBackToChats();
        this.showSnackbar("Changes saved", "success", 2000);
      } catch (e) {
        this.showSnackbar(e, "error", 2000);
      }
    },
    /**
     * Send a request to the backend to delete a group chat.
     * Go back to main chat screen, show an error snackbar on success/error.
     * @aparam chatGroupId id of the chat to delete.
     */
    async deleteGroupChat(chatGroupId) {
      try {
        const res = await deleteChat(chatGroupId);
        this.chats = this.chats.filter(chat => chat.chatGroupId !== chatGroupId);
        this.goBackToChats();
        this.showSnackbar("Chat deleted", "success", 2000);
      } catch (e) {
        this.showSnackbar(e, "error", 2000);
      }
    },
    /**
     * Emits to the global snackbar component to show a snackbar.
     * @param message message to be shown in the snackbar.
     * @param color color of the snackbar.
     * @time length of time to show the snackbar for, in milliseconds.
     */
    showSnackbar(message, color, time) {
      this.$root.$emit("show-snackbar", {
        color: color,
        message: message,
        timeout: time
      });
    },
    /**
     * Called when the createChat component emits a chatCreated event.
     * Closes the chat creation component and refreshes the list of chats to display the new chat.
     * @param userIds users in the new group chat.
     * @param chatName name of the new group chat.
     */
    async createChat(userIds, chatName) {
      try {
        const res = await createChat(userIds, chatName);
        console.log(res);
        this.showSnackbar("Chat created", "success", 2000);
        this.isShowingCreateChat = false;
        this.getChats();
      } catch (e) {
        this.showSnackbar(e, "error", 2000);
      }
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

      if (this.isShowingManageChat) {
        this.isShowingManageChat = false;
      } else {
        this.currentChatId = null;
        this.isShowingCreateChat = false;
      }

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
      async newMessages(messages) {
          const currentChat = this.getCurrentChat();
          const newMessages = messages.concat(currentChat.messages);
          console.log(newMessages)
          currentChat.messages = newMessages;
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