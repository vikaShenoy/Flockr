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
            @addAdminPriviledge="addAdminPrivilege"
            @removeAdminPriviledge="removeAdminPrivilege"
            @userSignedUp="userSignedUp"
						@getNextUsers="getNextUsers"
    />
    <DestinationProposals
            v-on:showError="showErrorSnackbar"
            v-on:showMessage="showSuccessSnackbar"
            @addUndoCommand="addUndoCommand"
    />
  </div>
</template>


<script>
  import ManageUsers from "./ManageUsers/ManageUsers.vue";
  import {
    deleteUser,
    deleteUsers,
    undoDeleteUser,
    undoDeleteUsers,
    updateRoles
  } from "./AdminPanelService.js";
  import superagent from "superagent";
  import {endpoint} from '../../utils/endpoint';
  import DestinationProposals from "./DestinationProposals/DestinationProposals";
  import UndoRedo from "../../components/UndoRedo/UndoRedo";
  import Command from "../../components/UndoRedo/Command";
  import roleType from '../../stores/roleType';
  import {getMoreUsers} from "./AdminPanelService";

  export default {
    components: {
      ManageUsers,
      DestinationProposals,
      UndoRedo
    },

    mounted() {
      this.getNextUsers();
    },

    data() {
      return {
        adminSearch: '',
        showEditUserForm: false,
        userBeingEdited: null,
				userOffset: 0,
				userLimit: 15,
        users: [] // single source of truth for children components relying on users so that info stays up to date
      }
    },
    computed: {
      getFilteredUsers() {
        return this.users.filter(user => {
          const fullName = `${user.firstName} ${user.lastName}`.toLowerCase();
          return fullName.includes(this.adminSearch.toLowerCase());
        });
      }
    },
    methods: {
			/**
			 * Call the search user endpoint to get the next set of users.
			 * Add the search term in search box if provided.
			 */
			async getNextUsers() {
        let queries = `offset=${this.userOffset}&limit=${this.userLimit}`;
        if (this.adminSearch) {
          queries += `&name=${this.adminSearch}`;
				}
        try {
          this.users = this.users.concat(await getMoreUsers(queries));
          this.userOffset += this.userLimit;
        } catch (e) {
          this.$root.$emit("show-snackbar", {
            timeout: 2000,
						message: "Could not get more users.",
						color: "error"
					});
				}
      },
      /**
       * Adds an undo command to the undo element.
       */
      addUndoCommand(acceptProposalCommand) {
        this.$refs.undoRedo.addUndo(acceptProposalCommand);
      },
      showSuccessSnackbar(message) {
        this.$root.$emit("show-snackbar", {
          message: message,
          color: "success",
          timeout: 3000
        });
      },
      showErrorSnackbar(errorMessage) {
        this.$root.$emit("show-snackbar", {
          message: errorMessage,
          color: "error",
          timeout: 3000
        });
      },

      /**
       * Delete users on the admin panel. Create the command to be used with undo/redo functionality.
       * @param userIds list of user ids for users to be deleted.
       */
      async handleDeleteUsersByIds(userIds) {


        try {
          await deleteUsers(userIds);

          const undoCommand = async (userIds) => {
            await undoDeleteUsers(userIds);
            this.userOffset = 0;
            this.users = [];
            this.getNextUsers();
          };

          const redoCommand = async (userIds) => {
            await deleteUsers(userIds);
            this.userOffset = 0;
            this.users = [];
            this.getNextUsers();
          };

          const deleteUsersCommand = new Command(undoCommand.bind(null, userIds), redoCommand.bind(null, userIds));
          this.$refs.undoRedo.addUndo(deleteUsersCommand);
          this.userOffset = 0;
          this.users = [];

          this.getNextUsers();
          this.showSuccessSnackbar("Successfully deleted user(s)'");
        } catch (err) {
          this.showErrorSnackbar("Could not delete user(s)");
        }
      },

      /**
       * Log out all the selected users. Set their auth tokens in local storage to null.
       * Show a success or error snackbar, depending on whether logout was successful.
       */
      handleLogoutUsersByIds: async function (userIds) {
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
        } catch (err) {
          this.showErrorSnackbar("Could not logout user(s)");
        }
      },
      /**
       * Adds admin privilege to a user
       * @param {number} selectedUserId ID of user to give admin privileges to
       */
      async addAdminPrivilege(selectedUserId) {
        const selectedUser = this.users.filter(user => user.userId === selectedUserId)[0];
        const roleTypes = selectedUser.roles.map(role => role.roleType);
        const newRoleTypes = [...roleTypes, roleType.ADMIN];

        const undoCommand = async (selectedUserId, roleTypes) => {
          await updateRoles(selectedUserId, roleTypes);
          this.getAllUsers();
        };

        const redoCommand = async (selectedUserId, roleTypes) => {
          await updateRoles(selectedUserId, roleTypes);
          this.getAllUsers();
        };

        const addAdminPriviledgeCommand = new Command(undoCommand.bind(null, selectedUserId, roleTypes), redoCommand.bind(null, selectedUserId, newRoleTypes));
        this.$refs.undoRedo.addUndo(addAdminPriviledgeCommand);

        try {
          await updateRoles(selectedUserId, newRoleTypes);
          this.showSuccessSnackbar("Added admin privileges");
          this.getAllUsers();
        } catch (e) {
          this.showErrorSnackbar("Error adding admin privileges");
        }
      },
      /**
       * Removes admin priviledge from a user
       * @param {number} selectedUserId ID of user to remove admin privileges for
       */
      async removeAdminPrivilege(selectedUserId) {
        const selectedUser = this.users.filter(user => user.userId === selectedUserId)[0];
        const oldRoleTypes = selectedUser.roles.map(role => role.roleType);
        // Remove admin role from user
        const roleTypes = selectedUser.roles
            .filter(role => role.roleType !== roleType.ADMIN)
            .map(role => role.roleType);

        const undoCommand = async (selectedUserId, roleTypes) => {
          await updateRoles(selectedUserId, roleTypes);
          this.getAllUsers();
        };
        const redoCommand = async (selectedUserId, roleTypes) => {
          await updateRoles(selectedUserId, roleTypes);
          this.getAllUsers();
        };

        const removeAdminPriviledgeCommand = new Command(undoCommand.bind(null, selectedUserId, oldRoleTypes),
            redoCommand.bind(null, selectedUserId, roleTypes));
        this.$refs.undoRedo.addUndo(removeAdminPriviledgeCommand);
        try {
          await updateRoles(selectedUserId, roleTypes);
          this.showSuccessSnackbar("Removed admin privileges");
          this.getAllUsers();
        } catch (e) {
          this.showErrorSnackbar("Error removing admin privileges");
        }
      },

      /**
       * Add commands to the undo stack and refresh users.
       * @param userId user who has been signed up.
       */
      userSignedUp(userId) {
        const undoCommand = async (userId) => {
          await deleteUser(userId);
          this.getAllUsers();
        };

        const redoCommand = async (userId) => {
          await undoDeleteUser(userId);
          this.getAllUsers();
        };

        const signupCommand = new Command(undoCommand.bind(null, userId), redoCommand.bind(null, userId));
        this.getAllUsers();
        this.$refs.undoRedo.addUndo(signupCommand);
      }
    },
		watch: {
      /**
			 * Called when the user types in the search bar.
			 * Clear existing users.
			 * Call the search user endpoint to retrieve users matching the search term.
       */
      adminSearch() {
        this.userOffset = 0;
        this.users = [];
        this.getNextUsers();
			}
		},

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