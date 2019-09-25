<template>
  <div id="container">
    <div>
      <h2>You Flocked Up....</h2>
      <h1>Page Not Found</h1>
      <v-flex row id="button-row">
        <v-btn
          v-if="!isLoggedIn"
          color="secondary"
          depressed
          @click="homeSignup()"
        >Sign Up
        </v-btn>
        <v-btn
          v-if="!isLoggedIn"
          color="secondary"
          depressed
          @click="homeLogin()"
        >Log in
        </v-btn>
      </v-flex>

    </div>

  </div>
</template>


<script>
  import UserStore from "../../stores/UserStore";
  export default {
    data() {
      return {
        e1: 0
      }
    },
    methods: {
      /**
       * Sets the router to the login page
       * @returns {Promise<void>}
       */
      async homeLogin() {
        this.$router.push("/login");
      },
      /**
       * Sets the router to the sign up page
       * @returns {Promise<void>}
       */
      async homeSignup() {
        this.$router.push("/signup");
      }
    },
    computed: {
      /**
       * Checks if the user is logged in or not
       * @returns {*|UserStore.data.userId|null} the user id of the user or null if the user is not logged in
       */
      isLoggedIn() {
        return UserStore.methods.loggedIn();
      }
    }

  }
</script>

<style lang="scss" scoped>
  @import "../../styles/_variables.scss";

  #container {
    display: flex;
    align-items: center;
    justify-content: center;
    background-image: url("../../assets/background.jpg");
    background-size: cover;
    box-shadow: inset 0 0 0 1000px rgba(0, 0, 0, .5);
    width: 100%;
    height: 100%;
    margin-top:0;

    h1 {
      font-size: 5rem;
      text-align: center;
      color: #FFF;
      text-shadow: 1px 1px 1px #000;
    }

    h2 {
      font-size: 2rem;
      text-align: center;
      color: #FFF;
    }

  }
  #button-row {
    align-items: center;
    justify-content: center;
  }
</style>