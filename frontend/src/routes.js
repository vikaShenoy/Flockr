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

import { loggedIn, loggedInOrOut, isAdmin } from "./utils/auth";
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
    beforeEnter: loggedInOrOut
  },
  {
    path: "/login",
    component: Login,
    beforeEnter: loggedInOrOut
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
];

const router = new VueRouter({
  mode: "history",
  routes: routes
});

export default router;