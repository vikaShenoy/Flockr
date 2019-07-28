<template>
  <div class="admin-panel">
    <h2>Admin Panel</h2>
    <ManageUsers
      v-bind:adminSearch.sync="adminSearch"
      :users="getFilteredUsers"
      v-on:wantToEditUserById="handleWantToEditUserById"
      v-on:deleteUsersByIds="handleDeleteUsersByIds"
      v-on:logoutUsersByIds="handleLogoutUsersByIds"
      @addAdminPriviledge="addAdminPriviledge"
      @removeAdminPriviledge="removeAdminPriviledge"
    />
    <DestinationProposals
      v-on:showError="showErrorSnackbar"
      v-on:showMessage="showSuccessSnackbar"
     />

    <EditUserForm
      v-if="userBeingEdited"
      :showForm="this.showEditUserForm"
      :initialUserData="this.userBeingEdited"
      v-on:dismissForm="handleEditUserFormDismissal"
      v-on:submitForm="handleEditUserFormSubmission"
      v-on:incorrectData="handleEditUserFormError"
    />
    <Snackbar :snackbarModel="this.snackbarModel" v-on:dismissSnackbar="snackbarModel.show=false"/>
  </div>
</template>


<script>
import ManageUsers from "./ManageUsers/ManageUsers.vue";
import EditUserForm from "./EditUserForm/EditUserForm.vue";
import { getUsers, getAllUsers, updateRoles } from "./AdminPanelService.js";
import { patchUser } from "./AdminPanelService.js";
import superagent from "superagent";
import { endpoint } from '../../utils/endpoint';
import Snackbar from '../../components/Snackbars/Snackbar.vue';
import DestinationProposals from "./DestinationProposals/DestinationProposals";
import roleType from '../../stores/roleType';

export default {
  components: {
    ManageUsers,
    EditUserForm,
    Snackbar,
    DestinationProposals
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
    showSuccessSnackbar(message) {
      this.snackbarModel.text = message;
      this.snackbarModel.color = 'green';
      this.snackbarModel.show = true;
    },
    showErrorSnackbar(errorMessage) {
      this.snackbarModel.text = errorMessage;
      this.snackbarModel.color = 'red';
      this.snackbarModel.show = true;
    },
    handleEditUserFormSubmission: async function(patchedUser) {
      const userId = patchedUser.userId;
      try {
        await patchUser(userId, patchedUser);
        this.showEditUserForm = false;
        this.getAllUsers();
        this.showSuccessSnackbar("Successfully edited user");
      } catch (e) {
        this.showErrorSnackbar("Could not edit the user");
     }
    },
    async handleDeleteUsersByIds(userIds) {
      const deleteUserPromises = userIds.map(userId => {
        const promise = superagent
          .delete(endpoint(`/users/${userId}`)).set('Authorization', localStorage.getItem('authToken'));
        return promise;
      });

      try {
        await Promise.all(deleteUserPromises);
        this.getAllUsers();
        this.showSuccessSnackbar("Successfully deleted user(s)'");
      } catch(err) {
        this.showErrorSnackbar("Could not delete user(s)");
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
        this.showSuccessSnackbar("Successfully logged out user(s)");
      } catch(err) {
        this.showErrorSnackbar("Could not logout user(s)");
      }
    },
    /**
     * Adds admin priviledge to a user
     * @param {number} selectedUserId ID of user to give admin priviledges to
     */
    async addAdminPriviledge(selectedUserId) {
      const selectedUser = this.users.filter(user => user.userId === selectedUserId)[0];
      const roleTypes = selectedUser.roles.map(role => role.roleType)
      roleTypes.push(roleType.ADMIN);

      try {
        await updateRoles(selectedUserId, roleTypes);
        this.showSuccessSnackbar("Added admin priviledges");
        this.getAllUsers();
      } catch (e) {
        this.showErrorSnackbar("Error adding admin priviledges");
      }
    },
    /**
     * Removes admin priviledge from a user
     * @param {number} selectedUserId ID of user to remove admin priviledges for
     */
    async removeAdminPriviledge(selectedUserId) {
      const selectedUser = this.users.filter(user => user.userId === selectedUserId)[0];
      // Remove admin role from user
      const roleTypes = selectedUser.roles
        .filter(role => role.roleType !== roleType.ADMIN)
        .map(role => role.roleType);


      try {
        await updateRoles(selectedUserId, roleTypes);
        this.showSuccessSnackbar("Removed admin priviledges");
        this.getAllUsers();
      } catch (e) {
        this.showErrorSnackbar("Error removing admin priviledges");
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
