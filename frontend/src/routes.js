import VueRouter from "vue-router";

import Home from "./containers/Home/Home.vue";
import Signup from "./containers/Signup/Signup.vue";
import Login from "./containers/Login/Login.vue";
import Profile from "./containers/Profile/Profile.vue"
import Travellers from "./containers/Travellers/Travellers.vue";
import Destinations from "./containers/Destinations/Destinations.vue";
import Trips from "./containers/Trips/Trips.vue";
import AddTrip from "./containers/AddTrip/AddTrip.vue";
import Trip from "./containers/Trip/Trip.vue";
import EditTrip from "./containers/EditTrip/EditTrip.vue";

import { loggedIn } from "./utils/auth";

const routes = [
  {
    path: "/",
    component: Home
  },
  {
    path: "/profile/:id",
    component: Profile,
    beforeEnter: loggedIn
  },
  {
    path: "/travellers",
    component: Travellers,
    beforeEnter: loggedIn
  },
  {
    path: "/signup",
    component: Signup 
  },
  {
    path: "/login",
    component: Login,
  },
  {
    path: "/destinations",
    component: Destinations,
    beforeEnter: loggedIn
  },
  {
    path: "/trips",
    component: Trips,
    beforeEnter: loggedIn
  },
  {
    path: "/trips/add",
    component: AddTrip,
    beforeEnter: loggedIn
  },
  {
    path: "/trips/:id",
    component: Trip,
    beforeEnter: loggedIn
  },
  {
    path: "/trips/:id/edit",
    component: EditTrip,
    beforeEnter: loggedIn
  }
];

const router = new VueRouter({
  mode: "history",
  routes: routes
});

export default router;