<template>
  <div>
    <div id="contents" ref="contents" v-on:scroll="handleScroll()" :style="{height: voiceParticipants.length ? '290px' : '330px'}">
        <div v-if="sending && shouldSend" id="loadingMessages">
            <v-progress-circular
            indeterminate
            color="secondary"
            style="margin: 5px !important;"
            >
            </v-progress-circular>
        </div>
      <Messages v-if="chatGroup.messages" :messages="chatGroup.messages" :connectedUsers="connectedUsers"/>

      <div v-else id="loading">
        <v-progress-circular
          indeterminate
          color="secondary">
        </v-progress-circular>
      </div>
    </div>

  <div id="new-message">
    <div id="new-message-input">
      <v-text-field
        v-model="message"
        label="Type a message"
        single-line
        outline
        color="secondary"
        maxlength="100"
        @keyup.enter="sendMessage()"
      ></v-text-field>
    </div>

    <v-icon id="send-btn" color="secondary" @click="sendMessage()">send</v-icon>
  </div>

  </div>
</template>

<script>
import { sendMessage, getChatMessages, getConnectedUsers } from "./ChatGroupService";
import Messages from "./Messages/Messages";
import UserStore from "../../../../stores/UserStore";

export default {
  components: {
    Messages
  },
  props: {
    chatGroup: Object,
    voiceParticipants: Array
  },
  data() {
    return {
      message: "",
      shouldSend: true,
      sending: false,
      connectedUsers: [],
      userSocket: UserStore.data.socket
    };
  },
  mounted() {
    if (!this.chatGroup.messages) {
      this.getChatMessages();
    } else {
      const { contents } = this;
      contents.scrollTop = contents.scrollHeight;
    }
    this.getConnectedUsersForChat();
    this.listenForSocketMessages();
  },
  methods: {
    /**
     * Listen for user connections and disconnections
     */
    listenForSocketMessages() {
      this.userSocket.addEventListener("message", (event) => {
        const socketMessage = JSON.parse(event.data);
        if (socketMessage.type === "disconnected") {
          const disconnectedUserId = socketMessage.user.userId;
          this.connectedUsers = this.connectedUsers.filter(user => user.userId !== disconnectedUserId);
        } else if (socketMessage.type === "connected") {
          const { user } = socketMessage;
          this.connectedUsers.push(user);
        }
      });
    },
    /**
     * Get the connected users for the chat group.
     */
    async getConnectedUsersForChat() {
      try {
        this.connectedUsers = await getConnectedUsers(this.chatGroup.chatGroupId);
      } catch (err) {
        this.$root.$emit('show-error-snackbar', "Could not get connected users for the chat", 4000);
      }
    }, 
    async sendMessage() {
      try {
          if (!this.message.length) {
              return;
          }
        const message = {
          chatGroup: this.chatGroup,
          contents: this.message,
          user: {
            userId: UserStore.data.userId
          }
        };
        const messageContents = this.message;
        this.message = "";
        this.$emit("newMessage", message);
        await sendMessage(this.chatGroup.chatGroupId, messageContents);
      } catch (e) {
        this.$root.$emit("show-error-snackbar", "Error sending message", 2000);
      }
    },
    /**
     * Gets chat messages and emits them back to parent
     */
    async getChatMessages() {
      try {
        const messages = await getChatMessages(this.chatGroup.chatGroupId, null, null);
        this.$emit("messagesRetrieved", messages);
      } catch (e) {
        this.$root.$emit("show-snackbar", {
          message: "Could not get chat messages",
          color: "error",
          timeout: 2000
        });
      }
    },
    async handleScroll() {
      const contents = this.$refs.contents;
      const nearTop = contents.scrollTop <= 50;

      if (nearTop && !this.sending) {
          this.sending = true;
          const oldScroll = contents.scrollHeight;
          const messages = await getChatMessages(this.chatGroup.chatGroupId, this.chatGroup.messages.length, 20);
          if (messages.length > 0) {
              await this.$emit("newMessages", messages);
              const newScroll = contents.scrollHeight;
              contents.scrollTop = newScroll - oldScroll;
              this.sending = false;
          } else {
              this.shouldSend = false;
          }
      }
    }
  },
  watch: {
    chatGroup: {
      handler() {
        const contents = this.$refs.contents;
        const isAtBottom = contents.scrollTop + contents.clientHeight > contents.scrollHeight - 50;

        if (isAtBottom) {
          setTimeout(() => {
            contents.scrollTop = contents.scrollHeight;
          }, 0);
        }
      },
      deep: true
    }
  }
}
</script>

<style lang="scss">

    #new-message .v-text-field__details {
        display: none;
    }

    #new-message .v-input__slot {
        margin-bottom: 0;
    }

</style>

<style lang="scss" scoped>
#new-message {
  width: 100%;
  display: flex;
  position: absolute;
  bottom: 5px;
  justify-content: center;
  align-items: center;
  height: 57px;
}

#new-message-input {
  width: 90%;
  height: 57px;
    padding-left: 7px;
    padding-bottom: 0;
}

#send-btn {
  width: 10%;
}

#contents {
  overflow: auto;
  transition: height 0.1s ease-in;
}


#loading {
  justify-content: center;
  align-items: center;
  display: flex;
  height: 100%;
}
    #loadingMessages {
        justify-content: center;
        align-items: center;
        display: flex;
    }
</style>