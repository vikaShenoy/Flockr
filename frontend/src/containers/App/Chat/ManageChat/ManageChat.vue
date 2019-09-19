<template>
  <v-card flat>
    <v-flex>
      <v-form ref="form">
        <v-text-field
          class="padding"
          v-model="currentName"
          :rules="requiredRule"
        >
        </v-text-field>


          <GenericCombobox
                  class="padding"
                  label="Users"
                  :get-function="searchUser"
                  :item-text="(user) => user.firstName + ' ' + user.lastName"
                  multiple
                  v-model="currentUsers"
          ></GenericCombobox>

        <UserSummary
          class="padding"
          v-for="user in currentUsers"
          v-bind:key="user.userId"
          :user=user
          @deleteUser="deleteUser"
        ></UserSummary>

        <v-spacer align="center">
         <v-btn
            color="error"
            v-on:click="deleteChat"
            depressed
          >
            Delete Chat
          </v-btn>

          <v-btn
            color="secondary"
            v-on:click="editChat"
            depressed
            >
            
            Save Changes
          </v-btn>
 
        </v-spacer>
      </v-form>
    </v-flex>
  </v-card>
  
</template>

<script>
  import UserSummary from "./UserSummary"
  import { getUsers, editChat } from "../ChatService";
  import { rules } from "../../../../utils/rules";
  import GenericCombobox from "../../../../components/GenericCombobox/GenericCombobox";
  export default {
    name: "ManageChat",
    components: {
        GenericCombobox,
      UserSummary,
    },
    data() {
      return {
        addUsers: [],
        currentName: this.chatGroup.name,
        currentUsers: [], //todo had to change
        usersToAdd: [],
        requiredRule: [rules.required],
      }
    },
    methods: {

        updateSelectedUsers(newUsers) {
          this.currentUsers = newUsers
        },

        searchUser: async name => await getUsers(name),

      /**
       * Populate the add users combobox. Fill it with all users except ones already in chat.
       * @returns {Promise<void>}
       */
      async getAddUsers() {
        let users = await getUsers();
        const chatGroupUserIds = this.chatGroup.users.map(user => user.userId);
        this.addUsers = users.filter(user => !chatGroupUserIds.includes(user.userId));
      },
      /**
       * Delete a user from the temporary current users list. Emitted from UserSummary
       * component.
       * @param userId id of the user to delete from the temp list.
       */
      deleteUser(userId) {
        this.currentUsers = this.currentUsers.filter(user => user.userId !== userId);
      },
      /**
       * Make a copy of the current group chat users list.
       * Used as a temporary variable which is only saved/sent to backend
       * when the user saves changes.
       * Filter out the user's id from the list.
       */
      getCurrentUsers() {
        let users = [...this.chatGroup.users];
        // Filter out self
        const ownUserId = Number(localStorage.getItem("ownUserId"));
        users = users.filter(user => user.userId !== ownUserId);
        this.currentUsers = users;
      },
      /**
       * Emit to the chat component to delete the chat. Called on delete chat button click.
       */
      deleteChat() {
        this.$emit('deleteGroupChat', this.chatGroup.chatGroupId);
      },
      /**
       * Check the form is valid (name can't be empty).
       * Emit a call to the chat component to send the edit request.
       */
      editChat() {
        if (!this.$refs.form.validate()) {
          return
        }
        // Send the request to change the chat on backend.
        const allUsers = this.currentUsers.concat(this.usersToAdd);
        this.$emit('editGroupChat', this.chatGroup.chatGroupId, this.currentName, allUsers);
      }
    },
    mounted() {
      this.getAddUsers();
      this.getCurrentUsers();
    },
    props: {
      isShowing: {
        type: Boolean,
        required: false,
      },
      chatGroup: {
        type: Object,
        required: false,
      }
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
  padding-left: 20px;
  padding-right: 20px;
}

.light-pad {
  padding: 5px;
  margin: 5px;
}

</style>