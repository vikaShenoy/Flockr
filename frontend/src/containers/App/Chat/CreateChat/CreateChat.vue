<template>
  <v-card flat>
    <v-form ref="form">

      <v-text-field
        class="padding"
        label="Chat name"
        :rules="requiredRule"
        v-model="selectedChatName"
      ></v-text-field>

        <GenericCombobox
                class="padding"
                label="Users"
                :get-function="searchUser"
                :item-text="(user) => user.firstName + ' ' + user.lastName"
                multiple
                v-model="selectedUsers"
                @items-selected="updateSelectedUsers"
        ></GenericCombobox>

      <v-spacer align="center">
        <v-btn
          color="secondary"
          dark
          v-on:click="createNewChat"
          >
          <v-icon>add</v-icon>
        </v-btn>
      </v-spacer>
    </v-form>
  </v-card>
</template>

<script>
  import { createChat, getUsers } from "../ChatService";
  import { rules } from "../../../../utils/rules";
  import GenericCombobox from "../../../../components/GenericCombobox/GenericCombobox";

  export default {
    name: "CreateChat",
      components: {GenericCombobox},
      props: {
      isShowing: {
        type: Boolean,
        required: false
      },
    },
    data() {
      return {
        allUsers: [],
        selectedChatName: "",
        selectedUsers: [],
        requiredRule: [rules.required],
        arrayRule: [rules.requiredArray]
      };
    },
    methods: {

        updateSelectedUsers(newUsers) {
            this.selectedUsers = newUsers
        },

        searchUser: async name => await getUsers(name),
      /**
       * Retrieve all users in the system.
       * Filter out the user's own id from the list so they can't add themselves to the chat.
       */
      async getAllUsers() {
        this.allUsers = await getUsers();
        const ownId = Number(localStorage.getItem("ownUserId"));
        this.allUsers = this.allUsers.filter(user => user.userId !== ownId);
      },
      /**
       * Called when the user clicks the create chat button.
       * Checks that the fields have been filled in then emits to the parent component
       * which sends the request to create the chat.
       */
      async createNewChat() {
        if (!this.$refs.form.validate()) {
          return
        }
        try {
          const userIds = this.selectedUsers.map(user => user.userId);
          this.$emit("createChat", userIds, this.selectedChatName);
        } catch (e) {
          this.showSnackbar(e, "error", 2000);
        }
      },
      /**
       * @param {String} message the message to show in the snackbar
       * @param {String} color the colour for the snackbar
       * @param {Number} the amount of time (in ms) for which we show the snackbar
       */
      showSnackbar(message, color, timeout) {
        this.$root.$emit("show-snackbar", {
          message: message,
          color: color,
          timeout: timeout
        });
      },
    },
    mounted() {
      this.getAllUsers();
    },
    watch: {
      isShowing(value) {
        this.isShowing = value;
      }
    }
  }
</script>

<style scoped>

.padding {
  margin: 20px;
  padding: 20px;
}

</style>