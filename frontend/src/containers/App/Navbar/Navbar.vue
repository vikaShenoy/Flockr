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
        @click="$router.push(item.url === '/profile' ? `/profile/${userStore.userId}` : item.url)"
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

export default {
  data() {
    return {
      userStore: UserStore.data,
      items: [
        {
          title: "Home",
          icon: "dashboard",
          url: "/",
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
          icon: "power_settings_new",
          loggedIn: true,
          loggedOut: false
        }
      ]
    };
  },
  computed: {
    /**
     * Computed property which filters nav items to show
     */
    itemsToShow() {
      const loggedIn = UserStore.methods.loggedIn();
      const profileCompleted = UserStore.methods.profileCompleted();

      return this.items.filter(item => {
        return (item.loggedIn && loggedIn && (item.profileCompleted && profileCompleted || !item.profileCompleted)) || item.loggedOut && !loggedIn;
      });
    },
  }
}
</script>

<style lang="scss" scoped>
  @import "../../../styles/_variables.scss";

  #navbar {
    background-color: $primary;
    position: fixed;
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


