import roleType from "./roleType";
import superagent from "superagent";
import { endpoint } from "../utils/endpoint";


const UserStore = {
  data: {
    userId: null,
    firstName: null,
    middleName: null,
    lastName: null,
    email: null,
    dateOfBirth: null,
    nationalities: null,
    passports: null,
    travellerTypes: null,
    gender: null,
    timestamp: null,
    viewingAsAnotherUser: false
  },
  methods: {
    setData(user) {
      console.log("user is: ", user);
      UserStore.data.userId = user.userId;
      UserStore.data.firstName = user.firstName;
      UserStore.data.middleName = user.middleName;
      UserStore.data.lastName = user.lastName;
      UserStore.data.email = user.email;
      UserStore.data.dateOfBirth = user.dateOfBirth;
      UserStore.data.nationalities = user.nationalities;
      UserStore.data.passports = user.passports;
      UserStore.data.travellerTypes = user.travellerTypes;
      UserStore.data.gender = user.gender;
      UserStore.data.timestamp = user.timestamp; 
      UserStore.roles = user.roles;

      const userId = localStorage.getItem("userId");
      const ownUserId = localStorage.getItem("ownUserId");

      UserStore.data.viewingAsAnotherUser = ownUserId !== userId;
    },
    /**
     * Check if a user is an admin
     */
    isAdmin() {
      if (!this.loggedIn()) {
        return false;
      }

      for (const role of UserStore.roles)  {
        if (role.roleType === roleType.ADMIN || role.roleType === roleType.DEFAULT_ADMIN) {
          return true;
        }
        return false;
      }
    },
    /**
     * Check if a user is a default admin
     */
    isDefaultAdmin() {
      if (!this.loggedIn()) {
        return false;
      }
      
      for (const role of UserStore.roles)  {
        if (role.roleType === roleType.DEFAULT_ADMIN) {
          return true;
        }
        return false;
      }
    },
    /**
     * Checks that a user can do something for themselves or an admin can something
     * in place of other users
     * @param {number} userId The user ID to compare to the logged in user
     * @returns {boolean} True if the user has permission, false otherwise
     */
    hasPermission(userId) {
      return userId == UserStore.data.userId || UserStore.methods.isAdmin();
    },
    /**
     * Checks if user is logged in
     */
    loggedIn() {
      return UserStore.data.userId;
    },
    logout() {
      UserStore.data.userId = null;
      UserStore.data.firstName = null;
      UserStore.data.middleName = null;
      UserStore.data.lastName = null;
      UserStore.data.email = null;
      UserStore.data.dateOfBirth = null;
      UserStore.nationalities = null;
      UserStore.passports = null;
      UserStore.travellerTypes = null;
      UserStore.gender = null;
      UserStore.timestamp = null; 
      localStorage.removeItem("userId");
      localStorage.removeItem("authToken");
      localStorage.removeItem("ownUserId");
      UserStore.data.viewingAsAnotherUser = false;
    },
    /**
     * Determines if the user's profile has been completed
     * @returns {boolean} Whether profile has been completed
     */

    profileCompleted() {
      return UserStore.data.dateOfBirth !== null && UserStore.data.gender !== null && UserStore.data.nationalities.length && UserStore.data.travellerTypes.length;
    },
    /**
     * 
     */
    viewAsAnotherUser(userData) {
      UserStore.methods.setData(userData);
      UserStore.data.viewingAsAnotherUser = true;
      localStorage.setItem("userId", userData.userId);
    },
    /**
     * Allows admins to go back to their own accounts after viewing
     * as a specific user
     */
    async backToOwnAccount() {
      const ownUserId = localStorage.getItem("ownUserId");
      const authToken = localStorage.getItem("authToken");
      
      const res = await superagent.get(endpoint(`/users/${ownUserId}`))
        .set("Authorization", authToken)

      UserStore.methods.setData(res.body);
      localStorage.setItem("userId", ownUserId);
      UserStore.data.viewingAsAnotherUser = false;
      return res.body.userId;
    }
  }
};

export default UserStore;