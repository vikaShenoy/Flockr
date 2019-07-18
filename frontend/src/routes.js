import VueRouter from "vue-router";

import Home from "./containers/Home/Home.vue";
import Signup from "./containers/Signup/Signup.vue";
import Login from "./containers/Login/Login.vue";
import Profile from "./containers/Profile/Profile.vue";
import Destinations from "./containers/Destinations/Destinations.vue";
import SearchTravellers from "./containers/SearchTravellers/SearchTravellers.vue";
import Trips from "./containers/Trips/Trips.vue";
import AddTrip from "./containers/AddTrip/AddTrip.vue";
import Trip from "./containers/Trip/Trip.vue";
import EditTrip from "./containers/EditTrip/EditTrip.vue";
import AdminPanel from "./containers/AdminPanel/AdminPanel.vue";

import { loggedIn, loggedInOrOut, isAdmin, loggedOut } from "./utils/auth";
import UserGallery from "./containers/UserGallery/UserGallery";

// All routes need to be annotated with loggedIn or loggedInOrOut
const routes = [
  {
    path: "/",
    component: Home,
    beforeEnter: loggedInOrOut
  },
  {
    path: "/profile/:id",
    component: Profile,
    beforeEnter: loggedIn
  },
  {
    path: "/signup",
    component: Signup,
    beforeEnter: loggedOut
  },
  {
    path: "/login",
    component: Login,
    beforeEnter: loggedOut
  },
  {
    path: "/search",
    component: SearchTravellers,
    beforeEnter: loggedIn
  },
  {
    path: "/destinations",
    component: Destinations,
    beforeEnter: loggedIn,
  },
  {
    path: "/trips",
    component: Trips,
    beforeEnter: loggedIn
  },
  {
    // Allows an admin to view other traveller's trips
    path: "/travellers/:travellerId/trips",
    component: Trips,
    beforeEnter: isAdmin
  },
  {
    path: "/trips/add",
    component: AddTrip,
    beforeEnter: loggedIn
  },
  {
    // Allows an admin to create trips for other users
    path: "/travellers/:travellerId/trips/add",
    component: AddTrip,
    beforeEnter: isAdmin
  },
  {
    path: "/trips/:id",
    component: Trip,
    beforeEnter: loggedIn
  },
  {
    path: "/travellers/:travellerId/trips/:id",
    component: Trip,
    beforeEnter: isAdmin
  },
  {
    path: "/trips/:id/edit",
    component: EditTrip,
    beforeEnter: loggedIn
  },
  {
    path: "/travellers/:travellerId/trips/:id/edit",
    component: EditTrip,
    beforeEnter: isAdmin
  },
  {
    path: "/admin",
    component: AdminPanel,
    beforeEnter: isAdmin
  },
  {
    path: "/profile/:id/photos",
    component: UserGallery,
    beforeEnter: loggedIn
  },
  {
    path: "/users/:userId/destinations",
    component: Destinations,
    beforeEnter: loggedIn,
  }
];

const router = new VueRouter({
  mode: "history",
  routes: routes
});

export default router;