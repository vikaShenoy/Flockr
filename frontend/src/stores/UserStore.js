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
    timestamp: null
  },
  methods: {
    setData(user) {
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
    },
    loggedIn() {
      return UserStore.data.userId;
    },
    signout() {
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
    },
    profileCompleted() {
      return UserStore.data.middleName !== null && UserStore.data.dateOfBirth !== null && UserStore.data.gender !== null && UserStore.data.nationalities.length;
    }
  }
};

export default UserStore;