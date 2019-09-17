<template>
  <v-app>
    <Navbar/>
    <Sidebar v-if="isLoggedIn"/>
    <v-content>
      <v-container fluid fill-height id="container">
        <v-layout>
          <v-flex>
            <GlobalSnackbar />
            <router-view/>
          </v-flex>
        </v-layout>
      </v-container>
    </v-content>
  </v-app>
</template>

<script>
  import UserStore from "../../stores/UserStore";
  import Sidebar from "./Sidebar/Sidebar";
  import Navbar from "./Navbar/Navbar";
  import GlobalSnackbar from "../../components/Snackbars/GlobalSnackbar";



export default {
  components: {
    Sidebar,
    Navbar,
    GlobalSnackbar,
  },
  name: "App",
  data() {
    return {}
  },
  computed: {
    /**
     * Checks if the user is currently logged in
     * @returns {The user ID}
     */
    isLoggedIn() {
      return UserStore.methods.loggedIn();
    }
  }
}
</script>

<style lang="scss">
  @import url("https://fonts.googleapis.com/css?family=Roboto");
  @import "../../styles/_defaults.scss";

  #container {
    padding: 0;
    margin-top: 60px;
    height: calc(100% - 60px);
  }

</style>

