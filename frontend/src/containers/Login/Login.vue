<template>
  <div id="login-container">
    <v-card
      id="login"
      class="col-lg-6 offset-lg-3"
    >

      <h2>Sign In</h2>

      <v-alert
        :value="hasInvalidCredentials"
        type="error"
      >
        Invalid Credentials
      </v-alert>

      <v-form ref="form">
        <v-text-field
          v-model="email"
          label="Email"
          color="secondary"
          :rules="fieldRules"
        >
        </v-text-field>

        <v-text-field
          type="password"
          v-model="password"
          label="Password"
          color="secondary"
          :rules="fieldRules"
          v-on:keyup.enter="login"
        >
        </v-text-field>

        <v-btn
          color="secondary"
          depressed
          class="col-sm-4 offset-sm-4"
          @click="login()"
        >Log in</v-btn>

      </v-form>

    </v-card>
  </div>
</template>

<script>

import { login } from "./LoginService.js";
import UserStore from "../../stores/UserStore";

export default {
  data() {
    return {
      email: "",
      password: "",
      emailErrors: [],
      passwordErrors: [],
      hasInvalidCredentials: false,
      fieldRules: [field => !!field || "Field is required"]
    };
  },
  methods: {
    /**
     * Firstly validates form before logging in user if fields are valid
     */
   async login() {
     if (!this.$refs.form.validate()) {
       return;
     }

      try {
        const user = await login(this.email, this.password);
        UserStore.methods.setData(user);
        localStorage.setItem("authToken", user.token);
        localStorage.setItem("userId", user.userId);
        localStorage.setItem("ownUserId", user.userId);
        this.$router.push(`/profile/${user.userId}`);
      } catch (e) {
        this.hasInvalidCredentials = true;         
      }
    }
  }
};
</script>


<style lang="scss" scoped>
#login {
  align-self: center;
  padding: 20px;

  h2 {
    text-align: center;
  }

}

#login-container {
  display: flex;
  background-image: url("../../assets/background.jpg");
  background-size: cover;
  width: 100%;
}

</style>

