<template>
  <v-navigation-drawer permanent id="navbar" :width="240">
    <v-toolbar flat id="title-box">
      <v-list>
        <v-list-tile>
          <v-list-tile-title id="title">
            Travel EA
          </v-list-tile-title>
        </v-list-tile>
      </v-list>
    </v-toolbar>

    <v-divider class="light-divider"></v-divider>

    <v-list dense class="pt-0">
      <v-list-tile
        v-for="item in itemsToShow"
        :key="item.title"
        @click="navClicked(item.url)"
        class="nav-item"
      >
        <v-list-tile-action>
          <v-icon class="nav-icon">{{ item.icon }}</v-icon>
        </v-list-tile-action>

        <v-list-tile-content>
          <v-list-tile-title>{{ item.title }}</v-list-tile-title>
        </v-list-tile-content>
      </v-list-tile>
    </v-list>
 </v-navigation-drawer>
</template>

<script>

import UserStore from "../../../stores/UserStore";
import { logout } from "./NavbarService";

export default {
  data() {
    return {
      userStore: UserStore.data,
      items: [
        {
          title: "Home",
          url: "/",
          icon: "dashboard",
          loggedIn: true,
          loggedOut: true
        },
        {
          title: "Search Travellers",
          icon: "search",
          url: "/search",
          profileCompleted: true,
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Destinations",
          icon: "location_on",
          url: "/destinations",
          profileCompleted: true,
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Trips",
          icon: "navigation",
          url: "/trips",
          profileCompleted: true,
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Sign up",
          icon: "person_add",
          url: "/signup",
          loggedIn: false,
          loggedOut: true 
        },
        {
          title: "Log in",
          icon: "exit_to_app",
          url: "/login",
          loggedIn: false,
          loggedOut: true
        },
        {
          title: "Profile",
          icon: "face",
          url: "/profile",
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Travellers",
          icon: "supervisor_account",
          url: "/travellers",
          profileCompleted: true,
          loggedIn: true,
          loggedOut: false
        },
        {
          title: "Log out",
          url: "/logout",
          icon: "power_settings_new",
          loggedIn: true,
          loggedOut: false
        }
      ]
    };
  },
  methods: {
    /**
     * Run when the nav was clicked on
     * @param {string} url - The url of nav item that was clicked on
     */
    async navClicked(url) {
      switch (url) {
        case "/profile":
          this.$router.push(`/profile/${UserStore.data.userId}`)
          break;
        case "/logout":
          await logout();
          UserStore.methods.logout();
          localStorage.removeItem("userId");
          localStorage.removeItem("authToken");
          this.$router.push("/");
          break;
        default:
          this.$router.push(url)
          break;
      }
    }

  },
  computed: {
    /**
     * Computed property which filters nav items to show
     */
    itemsToShow() {
      const loggedIn = UserStore.methods.loggedIn();
      const profileCompleted = UserStore.methods.profileCompleted();

      // return this.items.filter(item => {
      //   return (item.loggedIn && loggedIn && (item.profileCompleted && profileCompleted || !item.profileCompleted)) || item.loggedOut && !loggedIn;
      // });

      return this.items;
    },
    /**
     * Event handler called when nav item has been clicked
     * @param {string} url - The url of the nav item
     */
  }
}
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  #navbar {
    background-color: $primary;
    position: fixed !important;
  }

  #title-box {
    background-color: $primary;
  }

  #title {
    color:$darker-white;  
  }

  .nav-icon {
    color:$darker-white;  
  }

  .nav-item {
    color: $darker-white;
  }

  .light-divider {
    background-color: $lighter-primary;
  }

</style>


