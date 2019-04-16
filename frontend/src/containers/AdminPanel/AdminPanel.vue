<template>
  <div class="admin-panel">
    <h2>Admin Panel</h2>
    <ManageUsers
      :users="this.users"
      v-on:wantToEditUserById="handleWantToEditUserById"
      v-on:deleteUsersByIds="handleDeleteUsersByIds"
    />
    <EditUserForm
      :showForm="this.showEditUserForm"
      :initialUserData="this.userBeingEdited"
      v-on:dismissForm="handleEditUserFormDismissal"
      v-on:submitForm="handleEditUserFormSubmission"
    />
  </div>
</template>


<script>
import ManageUsers from "./ManageUsers/ManageUsers.vue";
import EditUserForm from "./EditUserForm/EditUserForm.vue";
import { getUsers } from "./AdminPanelService.js";
import superagent from "superagent";
import { endpoint } from '../../utils/endpoint';

export default {
  components: {
    ManageUsers,
    EditUserForm
  },

  mounted() {
    this.getAllUsers();
  },

  data() {
    return {
      showEditUserForm: false,
      userBeingEdited: {
        "userId": 0,
        "firstName": "Mariana",
        "middleName": "Rogelia",
        "lastName": "Cabrera",
        "dateOfBirth": "string",
        "email": "mariana@email.com",
        "gender": "Female",
        "nationalities": [
          {
            "nationalityId": 0,
            "nationalityName": "New Zealand"
          },
          {
            "nationalityId": 1,
            "nationalityName": "South Korea"
          }
        ],
        "passports": [
          {
            "passportId": 0,
            "passportCountry": "Argentina"
          }
        ],
        "travellerTypes": [
          {
            "travellerTypeId": 0,
            "travellerTypeName": "Gap Year"
          }
        ],
        "roles": [
          {
            "roleId": 2,
            "roleType": "Potato"
          }
        ],
        "timestamp": 728364786872368,
      },
      users: [] // single source of truth for children components relying on users so that info stays up to date
    }
  },
  methods: {

    async getAllUsers() {
      const allUsers = await getUsers();
      this.users = allUsers;
      console.log(this.users);
    },

    // event handler for when a child component wants to edit a user by id
    handleWantToEditUserById: async function(userId) {
      console.log(`Wanting to edit user ${userId} in admin panel`);
      const res = await superagent.get(endpoint(`/users/${userId}`)).set("Authorization", localStorage.getItem("authToken"));
      this.userBeingEdited = res.body;
      this.showEditUserForm = true; // show the edit user dialog
    },
    handleEditUserFormDismissal: function() {
      this.showEditUserForm = false; // close the edit user dialog
    },
    handleEditUserFormSubmission: function(patchedUser) {
      // TODO: call the AdminPanelService and ask it to patch the user
      throw new Error('Need to implement calling API to patch user by id');
    },
    handleDeleteUsersByIds: function(userIds) {
      // TODO: call the AdminPanelService and ask it to delete the users
      throw new Error('Need to implement calling API to delete users by id');
    }
  }
};
</script>


<style lang="scss" scoped>
  .admin-panel {
    width: 100%;
    height: 100%;
    padding: 10px;
  }
</style>
