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
    <Snackbar :snackbarModel="this.snackbarModel" />
  </div>
</template>


<script>
import ManageUsers from "./ManageUsers/ManageUsers.vue";
import EditUserForm from "./EditUserForm/EditUserForm.vue";
import { getUsers } from "./AdminPanelService.js";
import { patchUser } from "./AdminPanelService.js";
import superagent from "superagent";
import { endpoint } from '../../utils/endpoint';
import Snackbar from '../../components/Snackbars/Snackbar.vue';

export default {
  components: {
    ManageUsers,
    EditUserForm,
    Snackbar
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
      users: [], // single source of truth for children components relying on users so that info stays up to date
      snackbarModel: {
        show: false, // whether the snackbar is currently shown or not
        timeout: 5000, // how long the snackbar will be shown for, it will not update the show property automatically though
        text: '', // the text to show in the snackbar
        color: '', // green, red, yellow, red, etc
        snackbarId: 0 // used to know which snackbar was manually dismissed
		  }
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
      console.log(this.userBeingEdited);
      this.showEditUserForm = true; // show the edit user dialog
    },
    handleEditUserFormDismissal: function() {
      this.showEditUserForm = false; // close the edit user dialog
    },
    handleEditUserFormSubmission: async function(patchedUser) {
      // TODO: call the AdminPanelService and ask it to patch the user

      let userId = patchedUser.userId;
      try {
        await patchUser(userId, patchedUser);
      } catch (e) {
        console.log(e);
        this.snackbarModel.text = 'Could not edit the user';
        this.snackbarModel.color = 'red';
        this.snackbarModel.show = true;
      }
      this.showEditUserForm = false;
      this.getAllUsers();
      this.snackbarModel.text = 'Successfully edited user';
      this.snackbarModel.color = 'green';
      this.snackbarModel.show = true;
    },
    handleDeleteUsersByIds: async function(userIds) {
      const promises = [];
      console.log('userIds being deleted: ' + userIds)
      userIds.forEach(userId => {
        const promise = superagent
          .delete(endpoint(`/users/${userId}`)).set('Authorization', localStorage.getItem('authToken'));
        promises.push(promise);
      });
      try {
        await Promise.all(promises);
        this.getAllUsers();
        this.snackbarModel.text = 'Successfully deleted user(s)';
        this.snackbarModel.color = 'green';
        this.snackbarModel.show = true;
      } catch(err) {
        this.snackbarModel.text = 'Could not delete user(s)';
        this.snackbarModel.color = 'red';
        this.snackbarModel.show = true;
        console.error(`Could not delete those users: ${err}`);
      }
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
