<template>
  <div class="admin-panel">
    <div style="float: right">
      <UndoRedo ref="undoRedo"/>
    </div>

    <h2>Admin Panel</h2>
    <ManageUsers
      v-bind:adminSearch.sync="adminSearch"
      :users="getFilteredUsers"
      v-on:deleteUsersByIds="handleDeleteUsersByIds"
      v-on:logoutUsersByIds="handleLogoutUsersByIds"
    />
    <DestinationProposals
      v-on:showError="showErrorSnackbar"
      v-on:showMessage="showSuccessSnackbar"
     />

    <Snackbar :snackbarModel="this.snackbarModel" v-on:dismissSnackbar="snackbarModel.show=false"/>
  </div>
</template>


<script>
import ManageUsers from "./ManageUsers/ManageUsers.vue";
import EditUserForm from "./EditUserForm/EditUserForm.vue";
import { getUsers, getAllUsers, deleteUsers, undoDeleteUsers } from "./AdminPanelService.js";
import { patchUser } from "./AdminPanelService.js";
import superagent from "superagent";
import { endpoint } from '../../utils/endpoint';
import Snackbar from '../../components/Snackbars/Snackbar.vue';
import DestinationProposals from "./DestinationProposals/DestinationProposals";
import UndoRedo from "../../components/UndoRedo/UndoRedo";
import Command from "../../components/UndoRedo/Command";

export default {
  components: {
    ManageUsers,
    EditUserForm,
    Snackbar,
    DestinationProposals,
    UndoRedo
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
    async handleDeleteUsersByIds(userIds) {

      const undoCommand = async (userIds) => {
        await undoDeleteUsers(userIds);
        this.getAllUsers();
      };

      const redoCommand = async (userIds) => {
        await deleteUsers(userIds);
        this.getAllUsers();
      }

      const deleteUsersCommand = new Command(undoCommand.bind(null, userIds), redoCommand.bind(null, userIds));
      this.$refs.undoRedo.addUndo(deleteUsersCommand);

      try {
        await deleteUsers(userIds);
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

  h2 {
    width: 130px;
    margin: 0 auto;
  }

</style>
