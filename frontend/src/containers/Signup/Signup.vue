<template>
  <div id="signup-container">
    <v-stepper v-model="currStepperStep">
      <v-stepper-header>
        <v-stepper-step :complete="currStepperStep > 1" step="1">Basic Info</v-stepper-step>
        <v-divider/>
        <v-stepper-step :complete="currStepperStep > 2" step="2">Login Info</v-stepper-step>
        <v-divider/>
        <v-stepper-step step="3">Travelling Info</v-stepper-step>
      </v-stepper-header>

      <v-stepper-items>
        <v-stepper-content step="1">
          <v-card class="mb-5" flat>
            <v-text-field v-model="firstName" color="secondary" label="First name" @blur="validateFirstName()"
                          :error-messages="firstNameErrors" :maxlength="50"/>

            <v-text-field v-model="middleName" color="secondary" label="Middle name (optional)" @blur="validateMiddleName()"
                          :error-messages="middleNameErrors" :maxlength="50"/>

            <v-text-field v-model="lastName" color="secondary" label="Last name" @blur="validateLastName()"
                          :error-messages="lastNameErrors" :maxlength="50"/>

            <v-text-field v-model="email" color="secondary" label="Email" @blur="validateEmail()"
                          :error-messages="emailErrors" autocomplete="off" :maxlength="320"/>

            <v-text-field v-model="dateOfBirth" mask="date" label="Birthday" hint="DD/MM/YYYY" persistent-hint
                          return-masked-value placeholder="12/04/2003" :rules="[rules.dateBeforeToday, rules.required]"
                          validate-on-blur/>

            <v-select :rules="[rules.required]" v-model="gender" :items="genderOptions" color="secondary"
                      label="Gender"/>
          </v-card>
          <v-spacer row>

              <div class="button-row">
                <v-btn color="secondary"
                      @click="signIn()"
                      flat
                      >
                  Sign in
                </v-btn>

                <v-btn color="primary" class="continue-btn" @click="currStepperStep = 2" :disabled="!isBasicInfoStepperCompleted">Continue</v-btn>
              </div> 

          </v-spacer>
        </v-stepper-content>

        <v-stepper-content step="2">
          <v-card class="mb-5" flat>


            <v-text-field v-model="password" type="password" color="secondary" label="Password"
                          @blur="validatePassword()" :error-messages="passwordErrors" :maxlength="50"/>

            <v-text-field v-model="confirmPassword" type="password" color="secondary" label="Confirm Password"
                          @blur="validateConfirmPassword()" :error-messages="confirmPasswordErrors" :maxlength="50"
                          v-on:keyup.enter="signup"/>
          </v-card>

          <div class="button-row">
            <v-btn flat @click="currStepperStep = 1">Go back</v-btn>

            <v-btn :loading="loading" :disabled="!isLoginInfoStepperCompleted" color="primary" @click="signup()">
              Continue
            </v-btn>
          </div>

        </v-stepper-content>

        <v-stepper-content step="3">
          <v-card class="mb-5" flat>
            <v-combobox v-model="selectedNationalities" :items="this.allNationalities"
                        :item-text="n => n.nationalityName"
                        label="Nationalities" :rules="[rules.nonEmptyArray]" clearable multiple
                        @change="updateSelectedNationalities"/>

            <v-combobox v-model="selectedPassports" :items="this.allPassports" :item-text="p => p.passportCountry"
                        label="Passports"
                        clearable multiple
                        @change="updateSelectedPassports"/>

            <v-combobox v-model="selectedTravellerTypes" :items="this.allTravellerTypes"
                        :item-text="t => t.travellerTypeName"
                        label="Traveller types" :rules="[rules.nonEmptyArray]" clearable multiple
                        @change="updateSelectedTravellerType"
            />
          </v-card>

          <v-btn :loading="loading" :disabled="!isTravellingInfoStepperCompleted" color="primary"
                 @click="sendTravellerInfo()">
            Continue
          </v-btn>

        </v-stepper-content>
      </v-stepper-items>
    </v-stepper>
  </div>
</template>

<script>
  import {emailTaken, rules, signup, updateBasicInfo} from "./SignupService.js";
  import {login} from "../Login/LoginService";
  import {getNationalities} from "../Profile/Nationalities/NationalityService.js";
  import {getPassports} from "../Profile/Passports/PassportService.js";
  import {getAllTravellerTypes} from "../Profile/TravellerTypes/TravellerTypesService.js";
  import {validate} from "email-validator";
  import moment from "moment";
  import UserStore from "../../stores/UserStore";
  import config from '../../config';

  export default {
    props: {
      // flag that identified if component should be used for signing up or signup as a user as an admin
      isSigningUpAsAdmin: {
        type: Boolean,
        required: false
      }
    },
    async mounted() {
      try {
        this.allNationalities = await getNationalities();
        this.allPassports = await getPassports();
        this.allTravellerTypes = await getAllTravellerTypes();
      } catch (err) {
        this.$root.$emit('show-error-snackbar', 'Could not get info from server', 3000);
      }
    },
    data() {
      return {
        firstName: "",
        middleName: "",
        lastName: "",
        email: "",
        password: "",
        confirmPassword: "",
        gender: "",
        genderOptions: ["Other", "Female", "Male"],
        firstNameErrors: [],
        middleNameErrors: [],
        lastNameErrors: [],
        emailErrors: [],
        passwordErrors: [],
        confirmPasswordErrors: [],
        allNationalities: [],
        selectedNationalities: [],
        allPassports: [],
        selectedPassports: [],
        allTravellerTypes: [],
        selectedTravellerTypes: [],
        dateOfBirth: "",
        currStepperStep: 1,
        rules: rules,
        loading: false,
        isEmailTaken: true,
        signedUpUserId: 0 // used to cache the id of the signed up user, used when admin signs up another user
      };
    },
    computed: {
      /**
       * Return true if all the required fields in the basic info stepper are completed
       */
      isBasicInfoStepperCompleted() {
        const {firstName, lastName, gender, dateOfBirth, email,  isEmailTaken} = this;
        const fieldsAreNotEmpty = [firstName, lastName, gender, email, dateOfBirth].every(field => field.length > 0);
        return fieldsAreNotEmpty && !isEmailTaken;
      },
      /**
       * Return true if all the required fields in the login info stepper are completed
       */
      isLoginInfoStepperCompleted() {
        const {password, confirmPassword} = this;
        return password.length > 0 && password === confirmPassword;
      },
      /**
       * Return true if all the required fields in the travelling info stepper are completed
       */
      isTravellingInfoStepperCompleted() {
        const {selectedNationalities, selectedTravellerTypes} = this;
        return [selectedNationalities, selectedTravellerTypes].every(array => array.length > 0);
      },
      /**
       * @returns {Number[]} the selected nationality ids
       */
      selectedNationalityIds() {
        return this.selectedNationalities.map(nationality => nationality.nationalityId);
      },
      /**
       * @returns {Number[]} the selected passport ids
       */
      selectedPassportIds() {
        return this.selectedPassports.map(passport => passport.passportId);
      },
      /**
       * @returns {Number[]} the selected traveller type ids
       */
      selectedTravellerTypeIds() {
        return this.selectedTravellerTypes.map(travellerType => travellerType.travellerTypeId);
      }
    },
    methods: {
      checksAllLetter(name) {
        const letters = /^[A-Za-z ]+$/;
        if (letters.test(name)) {
          return true;
        } else {
          return false;
        }
      },
      /**
       * Checks if the first name field is empty and renders error if it is
       * @returns {number} If there are errors or not
       */
      validateFirstName() {
        this.firstNameErrors = [];

        if (!this.firstName) {
          this.firstNameErrors = ["First name is required"];
        } else if (this.firstName.length < 2 || !this.checksAllLetter(this.firstName))  {
          this.firstNameErrors = ["First name must be at least 2 characters long and only contains alphabetical letters"];
        } else {
          this.firstNameErrors = [];
        }
        return this.firstNameErrors.length === 0;
      },
      /**
       * Checks if the middle name is valid if the user filled out the middle name field
       */
      validateMiddleName() {
        this.middleNameErrors = [];

        if (this.middleName.length === 0) {
          this.middleNameErrors = [];
          return true;
        } else if (!this.checksAllLetter(this.middleName)) {
          this.middleNameErrors = ["Middle name must only contain alphabetical letters"];
          return this.middleNameErrors.length === 0;
        }
      },
      /**
       * Checks if the last name field is empty and renders error if it is
       * @returns {boolean} True if there are no errors, false otherwise
       */
      validateLastName() {
        this.lastNameErrors = [];

        if (!this.lastName) {
          this.lastNameErrors = ["Last name is required"];
        } else if (this.lastName.length < 2 || !this.checksAllLetter(this.lastName)) {
          this.lastNameErrors = ["Last name must be at least 2 characters long and only contains alphabetical letters"];
        } else {
          this.lastNameErrors = [];
        }
        return this.lastNameErrors.length === 0;
      },
      /**
       * Checks the f ollowing errors in order and renders if an error exists
       * - Checks if the email is blank
       * - Checks if the email is not valid
       * - Checks if the email is taken
       * @returns {boolean} True if there are no errors, false otherwise
       */
      async validateEmail() {
        if (!this.email) {
          this.emailErrors = ["Email is required"];
        } else if (!validate(this.email)) {
          this.emailErrors = ["Email is not valid"];
        } else if (await emailTaken(this.email)) {
          this.emailErrors = ["Email is already taken"];
          this.isEmailTaken = true;
        } else {
          this.emailErrors = [];
          this.isEmailTaken = false;
        }
        return this.emailErrors.length === 0;
      },
      /**
       * Checks the following errors and renVikasders and renders if an error exists
       * - Checks if password is blank
       * - Checks if password and confirm password don't match
       * @returns {Promise<boolean>} True if there are no errors, false otherwise
       */
      validatePassword() {
        if (!this.password) {
          this.passwordErrors = ["Password is required"];
        } else if (this.password.length < 6) {
          this.passwordErrors = ["Password has to be at least 6 characters long"];
        } else if (this.confirmPassword && (this.password !== this.confirmPassword)) {
          this.passwordErrors = ["Passwords are not identical"];
          this.confirmPasswordErrors = ["Passwords are not identical"];
        } else {
          this.passwordErrors = [];
          this.confirmPasswordErrors = [];
        }
        return this.passwordErrors.length === 0;
      },
      /**
       * Checks if the following errors and renders if an error exists
       * - Checks if confirm password is blank
       * - Checks if password and confirm password don't match
       * @returns {number} True if there are no errors, false otherwise
       */
      validateConfirmPassword() {

        if (!this.confirmPassword) {
          this.confirmPasswordErrors = ["Confirm Password is required"];
        } else if (this.confirmPassword.length < 6) {
          this.confirmPasswordErrors = ["Confirm Password has to be at least 6 characters"];
        } else if (this.password && (this.password !== this.confirmPassword)) {
          this.passwordErrors = [];
          this.confirmPasswordErrors = ["Passwords are not identical"];
          this.passwordErrors = ["Passwords are not identical"];
        } else {
          this.passwordErrors = [];
          this.confirmPasswordErrors = [];
        }

        return this.confirmPasswordErrors.length === 0;
      },
      /**
       * Validates email and password fields
       * @returns {Promise<boolean>} - True is fields are valid, false otherwise
       */
      async validate() {
        const fieldPromises = [
          this.validateFirstName(),
          this.validateLastName(),
          this.validateEmail(),
          this.validatePassword(),
          this.validateConfirmPassword(),
          this.validateMiddleName()
        ];

        const fieldResults = await Promise.all(fieldPromises);
        return fieldResults.every(fieldResult => fieldResult);
      },
      /**
       * Check if fields are valid. Send a request to sign the user up if so.
       */
      async signup() {
        this.loading = true;
        const validFields = await this.validate();
        if (!validFields) return;

        const userInfo = {
          firstName: this.firstName,
          lastName: this.lastName,
          email: this.email,
          password: this.password,
        };

        if (this.middleName.length > 0) {
          userInfo.middleName = this.middleName;
        }

        try {
          const {token, userId} = await signup(userInfo);

          // Only set local storage if not signup up as an admin
          if (!this.isSigningUpAsAdmin) {
            localStorage.setItem("authToken", token);
            localStorage.setItem("userId", userId);

            /* Used so if user is admin and wants to view as another user,
            then we'll know what ID to go back to once they are done
            */
            localStorage.setItem("ownUserId", userId);
          }

          this.signedUpUserId = userId;
          this.loading = false;
          this.currStepperStep = 3; // go to next stepper in sign up sequence
        } catch (e) {
          this.$root.$emit('show-error-snackbar', 'Could not sign up', 3000);
          this.loading = false; // to allow users to make changes
        }
      },
      /**
       * Updates the traveller information
       */
      async sendTravellerInfo() {
        this.loading = true;
        const {
          signedUpUserId,
          selectedNationalityIds,
          selectedPassportIds,
          selectedTravellerTypeIds,
          gender,
          dateOfBirth
        } = this;
        try {
          const updatedUser = await updateBasicInfo(signedUpUserId, {
            nationalities: selectedNationalityIds,
            passports: selectedPassportIds,
            travellerTypes: selectedTravellerTypeIds,
            gender: gender,
            dateOfBirth: moment(dateOfBirth, 'DD/MM/YYYY').format('YYYY-DD-MM')
          });
          UserStore.methods.setData(updatedUser);

          // If signing up a user as admin, then emit, otherwise go to user profile page
          if (this.isSigningUpAsAdmin) {
            this.$emit("exit", this.signedUpUserId);
          } else {
              const user =  await login(this.email, this.password);
              UserStore.methods.setData(user);
              localStorage.setItem("authToken", user.token);
              localStorage.setItem("userId", user.userId);
              localStorage.setItem("ownUserId", user.userId);
              const socket = new WebSocket(`${config.websocketUrl}?Authorization=${localStorage.getItem("authToken")}`);
              UserStore.data.socket = socket;
              this.$router.push(`/profile/${signedUpUserId}`) && this.$router.go(0);
          }

          this.loading = false;
        } catch (err) {
          this.showErrorSnackbar(`Could not add traveller info for your user`);
          this.loading = false;
        }
      },
      /**
       * Shows an error snackbar
       */
      showErrorSnackbar(text) {
        this.$root.$emit("show-snackbar", {
          message: text,
          color: "error",
          timeout: 5000
        });
      },
      /**
       * Updates the selected nationalities of the user to all valid ones so if there is an
       * invalid nationality i.e. the user typed a string in the combo box, it is removed
       * automatically.
       * @param nationalities the chosen nationality values
       */
      updateSelectedNationalities(nationalities) {
        this.selectedNationalities = nationalities.filter(item => typeof item !== 'string');
      },
      /**
       * Updates the selected passports of the user to all valid ones so if there is an
       * invalid passport i.e. the user typed a string in the combo box, it is removed
       * automatically.
       * @param passports the chosen passport values
       */
      updateSelectedPassports(passports) {
        this.selectedPassports = passports.filter(passport => typeof passport !== 'string');
      },
      /**
       * Updates the selected traveller types of the user to all valid ones so if there is an
       * invalid traveller type i.e. the user typed a string in the combo box, it is removed
       * automatically.
       * @param travellerTypes the chosen traveller types values
       */
      updateSelectedTravellerType(travellerTypes) {
        this.selectedTravellerTypes = travellerTypes.filter(type => typeof type !== 'string');
      },
      /**
       * Login the user
       * @returns {Promise<void>}
       */
      async signIn() {
        this.$router.push("/login");
      }
    }

  };
</script>


<style lang="scss" scoped>
  #signup {
    align-self: center;
    padding: 20px;

    h2 {
      text-align: center;
    }
  }

  #signup-container {
    display: flex;
    background-image: url("../../assets/background.jpg");
    background-size: cover;
    width: 100%;
    height: 100%;
    align-items: center;
    justify-content: center;
  }

  .button-row {
    display: flex;
    justify-content: space-between;
  }

  .continue-btn {
    float: right;
  }

</style>


