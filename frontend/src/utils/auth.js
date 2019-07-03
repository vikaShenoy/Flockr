import superagent from "superagent";
import { endpoint } from "./endpoint";
import UserStore from "../stores/UserStore";

/**
 * Router onEnter hook to check if a user is logged in, if user
 * is not logged in, redirect, otherwise go to page
 * @param {function} to The route to go to
 * @param {function} from The route the user is currently in
 * @param {function} next Function to change where the user is going
 */
export async function loggedIn(to, from, next) {
  const userId = localStorage.getItem("userId");
  const userToken = localStorage.getItem("authToken");

  if (!userId || !userToken) {
    next("/login");
    return;
  }

  //If user is already logged in, then don't have to resend request
  if (UserStore.methods.loggedIn()) {
    next();
    return;
  }

  let res;
  try {
    res = await  superagent.get(endpoint(`/users/${userId}`))
    .set("Authorization", userToken)
  } catch (e) {
    next("/login");
    return;
  }

  UserStore.methods.setData(res.body);
  
  next();
}

/**
 * Router onEnter hook to check if a user is logged in, if user
 * is not logged in, redirect, otherwise go to page
 * @param {function} to The route to go to
 * @param {function} from The route the user is currently in
 * @param {function} next Functiont to change where the user is going
 */
export async function loggedInOrOut(to, from, next) {
  const userId = localStorage.getItem("userId");
  const userToken = localStorage.getItem("authToken");

  if (!userId || !userToken) {
    next();
    return;
  }

  //If user is already logged in, then don't have to resend request
  if (UserStore.methods.loggedIn()) {
    next();
    return;
  }

  let res;
  try {
    res = await  superagent.get(endpoint(`/users/${userId}`))
    .set("Authorization", userToken)
  } catch (e) {
    next();
    return;
  }

  UserStore.methods.setData(res.body);
  next();
}

/**
 * Router onEnter hook to check if the user is an admin
 * @param {*} to The route to go to
 * @param {*} from The router the user is currently in
 * @param {*} next Function to choose where the user is going
 */
export async function isAdmin(to, from, next) {
  const userId = localStorage.getItem("userId");
  const userToken = localStorage.getItem("authToken");

  if (!userId || !userToken) {
    next("/login");
    return;
  }

  //If user is already logged in, then don't have to resend request
  if (UserStore.methods.loggedIn() && UserStore.methods.isAdmin()) {
    next();
    return;
  }

  let res;
  try {
    res = await superagent.get(endpoint(`/users/${userId}`)).set("Authorization", userToken);
  } catch (e) {
    next("/login");
    return;
  }

  UserStore.methods.setData(res.body);

  if (UserStore.methods.isAdmin()) {
    next();
  } else {
    // If a user is logged in but not an admin, then don't let them have permission
    next("/");
  }
}
