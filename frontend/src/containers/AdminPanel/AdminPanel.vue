<template>
  <div class="admin-panel">
    <h2>Admin Panel</h2>
    <ManageUsers
      v-bind:adminSearch.sync="adminSearch"
      :users="getFilteredUsers"
      v-on:wantToEditUserById="handleWantToEditUserById"
      v-on:deleteUsersByIds="handleDeleteUsersByIds"
      v-on:logoutUsersByIds="handleLogoutUsersByIds"
    />
    <EditUserForm
      v-if="userBeingEdited"
      :showForm="this.showEditUserForm"
      :initialUserData="this.userBeingEdited"
      v-on:dismissForm="handleEditUserFormDismissal"
      v-on:submitForm="handleEditUserFormSubmission"
      v-on:incorrectData="handleEditUserFormError"
    />
    <Snackbar :snackbarModel="this.snackbarModel" />
  </div>
</template>


<script>
import ManageUsers from "./ManageUsers/ManageUsers.vue";
import EditUserForm from "./EditUserForm/EditUserForm.vue";
import { getUsers, getAllUsers } from "./AdminPanelService.js";
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
      adminSearch: '',
      showEditUserForm: false,
      userBeingEdited: null,
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
  computed:{
    getFilteredUsers() {
      return this.users.filter(user => {
        const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
        return fullName.includes(this.adminSearch.toLowerCase());
      });
    }
  },
  methods: {

    /** 
     * Call admin panel service method to retrieve 
     * all users, including those with incomplete profiles.
     * Set the users so they display on the panel.
     */
    async getAllUsers() {
      const allUsers = await getAllUsers();
      this.users = allUsers;
    },
    // event handler for when a child component wants to edit a user by id
    handleWantToEditUserById: async function(userId) {
      const res = await superagent.get(endpoint(`/users/${userId}`)).set("Authorization", localStorage.getItem("authToken"));
      this.userBeingEdited = res.body;
      this.showEditUserForm = true; // show the edit user dialog
    },
    handleEditUserFormDismissal: function() {
      this.showEditUserForm = false; // close the edit user dialog
    },
    handleEditUserFormError: function() {
      this.snackbarModel.text = "Data Incorrect, Try Again";
      this.snackbarModel.color = 'red';
      this.snackbarModel.show = true;
    },
    handleEditUserFormSubmission: async function(patchedUser) {
      let userId = patchedUser.userId;
      try {
        await patchUser(userId, patchedUser);
        this.showEditUserForm = false;
        this.getAllUsers();
        this.snackbarModel.text = 'Successfully edited user';
        this.snackbarModel.color = 'green';
        this.snackbarModel.show = true;
      } catch (e) {
        this.snackbarModel.text = 'Could not edit the user';
        this.snackbarModel.color = 'red';
        this.snackbarModel.show = true;
      }
    },
    handleDeleteUsersByIds: async function(userIds) {
      const promises = [];
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
      }
    },

    /**
     * Log out all the selected users. Set their auth tokens in local storage to null.
     * Show a success or error snackbar, depending on whether logout was successful.
     */
    handleLogoutUsersByIds: async function(userIds) {
      const promises = [];
      userIds.forEach(userId => {
        const promise = superagent
          .post(endpoint(`/auth/users/${userId}/logout`)).set('Authorization', localStorage.getItem('authToken'));
        promises.push(promise);
      });
      try {
        await Promise.all(promises);
        this.getAllUsers();
        this.snackbarModel.text = 'Successfully logged out user(s)';
        this.snackbarModel.color = 'green';
        this.snackbarModel.show = true;
      } catch(err) {
        this.snackbarModel.text = 'Could not logout user(s)';
        this.snackbarModel.color = 'red';
        this.snackbarModel.show = true;
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
