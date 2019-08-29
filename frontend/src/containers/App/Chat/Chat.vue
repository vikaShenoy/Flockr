<template>
  <div id="chat">
    <v-expansion-panel id="connected-users">
      <v-expansion-panel-content id="header">
        <template v-slot:header>
          <h4 id="title">Chat</h4>
        </template>

        <v-icon
          slot="actions"
          color="secondary"
        >$vuetify.icons.expand</v-icon>

        <v-card id="chats">
          <v-list>
            <v-list-tile
              class="chat-list-item"
              @click=""
              v-for="chat in chats"
              v-bind:key="chat.chatGroupId"
            >

            <div><h4>{{ chat.name }}</h4></div>
           </v-list-tile>
          </v-list> 
        </v-card>

      </v-expansion-panel-content>
    </v-expansion-panel>

  </div>
</template>

<script>
import { getChats } from "./ChatService";

export default {
  data() {
    return {
      chats: []
    };
  },
  mounted() {
    this.getChats();
  },
  methods: {
    showErrorSnackbar(message) {
      this.$root.$emit("show-snackbar", {
        message,
        color: "error",
        timeout: 2000
      });
    },
    async getChats() {
      try {
        const chats = await getChats();
        this.chats = chats;
      } catch (e) {
        console.log(e);
        this.showErrorSnackbar("Error getting chats");
      }
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../../styles/_variables.scss";

#chat {
  bottom: 0;
  background-color: green;
  position: absolute;
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
  max-height: 400px;
  overflow: auto;
}

.chat-list-item {
  &:not(:last-child) {
    border-bottom: 1px solid rgba(0,0,0,0.12);
 }

  h4 {
    color: $secondary;
  }
}
</style>