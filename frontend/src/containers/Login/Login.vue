<template>
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

    <v-text-field
      v-model="email"
      label="Email"
      color="secondary"
      @blur="validateEmail"
      :error-messages="emailErrors"
    >
    </v-text-field>

    <v-text-field
      type="password"
      v-model="password"
      label="Password"
      color="secondary"
      @blur="validatePassword"
      :error-messages="passwordErrors"
    >
    </v-text-field>

    <v-btn
      color="secondary"
      depressed
      class="col-sm-4 offset-sm-4"
      @click="login()"
    >Log in</v-btn>

  </v-card>
</template>

<script>

import { login } from "./LoginService.js";

export default {
  data() {
    return {
      email: "",
      password: "",
      emailErrors: [],
      passwordErrors: [],
      hasInvalidCredentials: false
    };
  },
  methods: {
    /**
     * Validates email and renders an error if there is one
     */
    validateEmail() {
      if (!this.email) {
        this.emailErrors = ["Email is required"];
      } else {
        this.emailErrors = [];
      }

      return this.emailErrors.length === 0;
    },
    /**
     * Validates password and renders an error if there is one
     */
    validatePassword() {
      if (!this.password) {
        this.passwordErrors = ["Password is required"];
      } else {
        this.passwordErrors = [];
      }

      return this.passwordErrors.length === 0;
    },
    /**
     * Validates fields before logging in user
     */
    async login() {
      const validEmail = this.validateEmail();
      const validPassword = this.validatePassword();

      if (!validEmail || !validPassword)  return;

      try {
        await login(this.email, this.password);
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
</style>

