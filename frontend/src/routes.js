import VueRouter from "vue-router";

import Home from "./containers/Home/Home.vue";
import Signup from "./containers/Signup/Signup.vue";
import Login from "./containers/Login/Login.vue";
import Profile from "./containers/Profile/Profile.vue"
import Destinations from "./containers/Destinations/Destinations.vue";
import Trips from "./containers/Trips/Trips.vue";

const routes = [
  {
    path: "/",
    component: Home
  },
  {
    path: "/profile/:id",
    component: Profile
  },
  {
    path: "/signup",
    component: Signup 
  },
  {
    path: "/login",
    component: Login
  },
  {
    path: "/destinations",
    component: Destinations
  },
  {
    path: "/trips",
    component: Trips
  },
];

const router = new VueRouter({
  mode: "history",
  routes: routes
});

export default router;