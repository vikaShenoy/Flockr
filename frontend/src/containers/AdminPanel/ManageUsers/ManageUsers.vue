<template>
	<div class=manage-users>
		<v-card class="manage-filter-card">
			<v-list two-line>
				<v-subheader class="manage-users-row">
					<div class="manage-users-text">
						<p>Manage users</p>
					</div>
					<v-text-field label="Search User" color="secondary" @input="searchAdminChange"/>

					<v-btn
									class="action-button"
									:disabled="this.selectedUsers.length !== 1"
									@click="viewAsUserClicked"
									depressed
									color="secondary"
					>
						View as User
					</v-btn>

					<v-btn
									class="action-button"
									:disabled="this.selectedUsers.length !== 1"
									@click="logoutUsersButtonClicked"
									depressed
									color="secondary"
					>
						Log Out User
					</v-btn>

					<v-btn
									class="action-button"
									@click="signupButtonClicked"
									depressed
									color="secondary"
					>
						Sign Up User
					</v-btn>

					<v-btn
									class="action-button"
									:disabled="!this.canAddAdminPriviledge"
									@click="showPrompt('Are you sure?', addAdminPriviledge)"
									depressed
									color="secondary"
					>
						Make Admin
					</v-btn>

					<v-btn
									class="action-button"
									:disabled="!this.canRemoveAdminPriviledge"
									@click="showPrompt('Are you sure?', removeAdminPriviledge)"
									depressed
									color="secondary"
					>
						Remove admin
					</v-btn>


					<v-btn
									class="action-button"
									:disabled="this.selectedUsers.length === 0"
									@click="showPrompt('Are you sure?', deleteUsersButtonClicked)"
									depressed
									color="secondary"
					>
						Delete users
					</v-btn>
				</v-subheader>
			</v-list>
		</v-card>

		<!-- User tile -->
			<v-card id="users"
							v-on:scroll="usersScrolled"
							ref="userCard">
				<v-list>
					<v-list-tile v-for="item in items" :key="item.userId" avatar @click="item.selected = !item.selected">
						<v-list-tile-avatar>
							<img :src="item.avatar">
						</v-list-tile-avatar>

						<v-list-tile-content>
							<v-list-tile-title>{{item.title}}</v-list-tile-title>
							<v-list-tile-sub-title>{{item.subtitle}}</v-list-tile-sub-title>
						</v-list-tile-content>

						<!-- Checkmark for when user is selected -->
						<v-icon v-if="!item.selected">check_circle_outline</v-icon>
						<v-icon v-else class="selected-icon">check_circle</v-icon>
					</v-list-tile>
				</v-list>
			</v-card>

		<v-dialog v-model="showSignup" max-width="800">
			<v-card>
				<SignUp @exit="closeSignupModal" isSigningUpAsAdmin></SignUp>
				<v-card-actions>
					<v-spacer></v-spacer>
				</v-card-actions>
			</v-card>
		</v-dialog>
		<prompt-dialog
						:message=prompt.message
						:onConfirm="prompt.onConfirm"
						:dialog="prompt.show"
						v-on:promptEnded="prompt.show=false"></prompt-dialog>
	</div>

</template>


<script>
  import {endpoint} from "../../../utils/endpoint.js";
  import moment from "moment";
  import SignUp from "../../Signup/Signup";
  import PromptDialog from "../../../components/PromptDialog/PromptDialog.vue";
  import UserStore from "../../../stores/UserStore";
  import roleType from '../../../stores/roleType';

  export default {
    components: {
      PromptDialog,
      SignUp
    },
    mounted() {
      this.items = this.mapUsers();
    },
    data() {
      return {
        items: [],
				scrollCalled: false,
        showSignup: false,
        prompt: {
          message: "",
          onConfirm: null,
          show: false
        }
      };
    },
    computed: {

      /**
       * Get the user ids of selected users.
       */
      selectedUsers: function () {
        return this.items.filter((item) => item.selected).map((user) => user.userId);
      },
      /**
       * Checks if user can add admin privileges to the selected users
       * @returns {boolean} True if admin can add admin priviledges, false otherwise
       */
      canAddAdminPriviledge() {
        if (this.selectedUsers.length === 0 || this.selectedUsers.length > 1) {
          return false;
        }
        const user = this.items.filter(user => user.userId === this.selectedUsers[0])[0];

        for (const role of user.roles) {
          if (role.roleType === roleType.ADMIN || roleType.DEFAULT_ADMIN) {
            return false;
          }
        }

        return true;
      },
      /**
       * Check if the person trying to remove admin rights is allowed to do so.
       */
      canRemoveAdminPriviledge() {
        if (this.selectedUsers.length === 0 || this.selectedUsers.length > 1) {
          return false;
        }
        const user = this.items.filter(user => user.userId === this.selectedUsers[0])[0];

        for (const role of user.roles) {
          if (role.roleType === roleType.ADMIN) {
            return true;
          }
        }
        return false;
      },

    },
    methods: {
			/**
			 * Called on scroll event for the admin panel user list.
			 * When the user scrolls down a certain amount, emit an event to fetch more users for the list.
			 * Uses delay to prevent too many endpoint calls.
			 */
      usersScrolled() {
        const userCard = this.$refs.userCard;
        const tolerance = 0.8;
        // Variable to prevent multiple endpoint calls when the scroll condition is satisfied.
				// NOTE - this may have to be adjusted based on network performance.
        const callDelay = 500;
        const {scrollHeight, scrollTop, clientHeight} = userCard.$el;
        const nearBottom = (scrollHeight - scrollTop) * tolerance <= clientHeight;
        if (nearBottom && !this.scrollCalled) {
          this.$emit("getNextUsers");
          this.scrollCalled = true;
          setTimeout(() => {this.scrollCalled = false}, callDelay);
        }
      },

      /**
       * Called when the view destinations button is clicked.
       * routes the admin to the destinations page for the selected user.
       */
      viewDestinationsButtonClicked() {
        const userId = this.selectedUsers[0];

        this.$router.push(`/users/${userId}/destinations`);
      },

      /**
       * Emit a function call, indicates search admin
       * has changed.
       */
      searchAdminChange(searchAdmin) {
        this.$emit('update:adminSearch', searchAdmin);
      },

      /**
       * Show a prompt to the user.
       */
      showPrompt(message, onConfirm) {
        this.prompt.message = message;
        this.prompt.onConfirm = onConfirm;
        this.prompt.show = true;
      },

      /**
       * Close the modal containing the signup component.
       */
      closeSignupModal(userId) {
        this.showSignup = false;
        this.$emit("userSignedUp", userId);
      },

      /**
       * Open the modal component containing the signup component.
       */
      signupButtonClicked: function () {
        this.showSignup = true;
      },
      /**
       * Call the admin panel service to logout the given user ids.
       */
      logoutUsersButtonClicked: async function () {
        const userIds = this.selectedUsers;
        this.$emit("logoutUsersByIds", userIds);
      },
      /**
       * Call the admin panel service to delete the given user ids.
       */
      deleteUsersButtonClicked: async function () {
        const userIds = this.selectedUsers;
        this.$emit("deleteUsersByIds", userIds);
      },

      /**
       * Open the trips page for the selected user.
       */
      viewTripsButtonClicked: function () {
        const userId = this.selectedUsers[0];
        this.$router.push(`/travellers/${userId}/trips`);
      },
      /**
       * Format the user data to be displayed on the admin panel.
       * Also filters user so it doesn't contain self
       * Use a generic avatar untill photos are implemented.
       */
      mapUsers: function () {
        return this.users
            .filter(user => user.userId !== UserStore.data.userId)
            .map(user => ({
              avatar: this.photoUrl(user.profilePhoto),
              userId: user.userId,
              title: user.firstName + ' ' + user.lastName,
              subtitle: 'Joined on ' + moment(user.timestamp).format("D/M/YYYY H:mm"),
              selected: false,
              roles: user.roles
            }));
      },
      /**
       * Gets the URL of a photo for a user
       * @param {number} photoId the ID of the photo to get
       * @returns {string} the url of the photo
       */
      photoUrl(profilePhoto) {
        if (profilePhoto != null) {
          const authToken = localStorage.getItem("authToken");
          const queryAuthorization = `?Authorization=${authToken}`;
          return endpoint(`/users/photos/${profilePhoto.photoId}${queryAuthorization}`);
        } else {
          return "http://s3.amazonaws.com/37assets/svn/765-default-avatar.png";
        }
      },
      /**
       * Redirect the user so they can view/use the application as another user.
       * (Used by admins to browse as a user and change their account).
       */
      viewAsUserClicked() {
        const user = this.users.filter(user => user.userId == this.selectedUsers[0])[0];
        UserStore.methods.viewAsAnotherUser(user);
        this.$router.push(`/profile/${user.userId}`);
      },
      /**
       * Emits event to add admin priviledge to a user
       */
      addAdminPriviledge() {
        this.$emit("addAdminPriviledge", this.selectedUsers[0]);
      },
      /**
       * Emits event to remove admin priviledge from a user
       */
      removeAdminPriviledge() {
        this.$emit('removeAdminPriviledge', this.selectedUsers[0]);
      }
    },
    props: ["users"],
    watch: {
      users() {
        this.items = this.mapUsers();
      }
    },
  }
</script>


<style lang="scss" scoped>
	@import "../../../styles/_variables.scss";

	.manage-users {
		width: 100%;
	}

	.manage-filter-card {
		height: 10%;
		width: 100%;
	}

	.selected-icon {
		color: $success;
	}

	.manage-users-row {
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		align-items: center;
		height: auto;

		.manage-users-text {
			p {
				text-align: left;
			}

			flex-grow: 1;
			justify-self: start;
		}

		.action-button {
			justify-self: end;
			width: fit-content;
		}

	}

	p {
		margin: 0;
	}

	#users {
		max-height: 400px;
		overflow: auto;
	}
</style>
