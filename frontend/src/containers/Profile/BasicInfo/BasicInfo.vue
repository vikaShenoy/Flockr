<template>
  <div>
    <div id="header">
      <h3>Basic Info</h3>
      <!--Only show edit/save btn if user is logged in-->

      <div id="undo-redo-buttons">
        <UndoRedo ref="undoRedo" />
      </div>

      <div v-if="userStore.userId === userProfile.userId" id="edit-btn">
        <v-btn
          small
          flat
          color="secondary"
          @click="toggleEditSave"
        >
          <v-icon v-if="!isEditing">edit</v-icon>
          <span v-else>Save</span>
        </v-btn>
      </div>
    </div>

    <v-card id="basic-info">
      <v-form ref="form">
        <div class="profile-item">
          <div class="item-signifier">
            <b>First Name</b>
          </div>

          <div class="item-value">
            <span v-if="!this.isEditing">{{ userProfile.firstName }}</span>
            <v-text-field
              class="edit-field"
              v-else
              v-model="firstName"
              color="secondary"
              :rules="fieldRules"
              :maxlength="50"
            >
            </v-text-field>
          </div>
        </div>

        <v-divider />

        <div class="profile-item">
          <div class="item-signifier">
            <b>Middle Name</b>
          </div>

        <div class="item-value">
          <span v-if="!this.isEditing"> {{ userProfile.middleName }} </span>
          <v-text-field
            class="edit-field"
            v-else
            v-model="middleName"
            color="secondary"
            :rules="optionalFieldRules"
            :maxlength="50"
          >
          </v-text-field>
        </div>
      </div>

        <v-divider />

        <div class="profile-item">
          <div class="item-signifier">
            <b>Last Name</b>
          </div>

          <div class="item-value">
            <span v-if="!this.isEditing">{{ userProfile.lastName }}</span>
            <v-text-field
              class="edit-field"
              v-else
              v-model="lastName"
              color="secondary"
              :rules="fieldRules"
              :maxlength="50"
            >
            </v-text-field>
          </div>
        </div>

        <v-divider />

        <div class="profile-item">
          <div class="item-signifier">
            <b>Date of Birth</b>
          </div>

          <div class="item-value">
            <span v-if="!this.isEditing">{{ userProfile.dateOfBirth }}</span>
            <v-menu
              v-else
              ref="dateMenu"
              v-model="dateMenu"
              :close-on-content-click="false"
              :nudge-right="40"
              :return-value.sync="dateOfBirth"
              lazy
              transition="scale-transition"
              offset-y
              full-width
            >
              <template v-slot:activator="{ on }">
                <v-text-field
                  class="edit-field"
                  v-model="dateOfBirth"
                  readonly
                  v-on="on"
                  :rules="dateRules"

                ></v-text-field>
              </template>
              <v-date-picker
                color="secondary"
                ref="picker"
                :max="currentDate"
                v-model="dateOfBirth"
                no-title
                scrollable
              >
                <v-spacer></v-spacer>
                <v-btn
                  flat
                  color="primary"
                  @click="dateMenu = false"
                >Cancel</v-btn>
                <v-btn
                  flat
                  color="primary"
                  @click="$refs.dateMenu.save(dateOfBirth)"
                >OK</v-btn>
              </v-date-picker>
            </v-menu>
          </div>
        </div>

        <v-divider />

        <div class="profile-item">

        <div class="item-signifier">
          <b>Gender</b>
        </div>

        <div class="item-value">
          <span v-if="!this.isEditing">{{ userProfile.gender }}</span>
          <v-select
            v-else
            :rules="fieldRules"
            v-model="gender"
            :items="genderOptions"
            class="edit-field"
            color="secondary"
          >
          </v-select>
        </div>
      </div>
      </v-form>
    </v-card>
  </div>
</template>
<script>
import UserStore from "../../../stores/UserStore";
import { rules, updateBasicInfo } from "./BasicInfoService.js";
import moment from "moment";
import UndoRedo from "../../../components/UndoRedo/UndoRedo";
import Command from "../../../components/UndoRedo/Command";

export default {
  props: ["userProfile", "userId"],
  components: {
    UndoRedo
  },
  data() {
    return {
      userStore: UserStore.data,
      isEditing: false,
      dateMenu: false,
      firstName: "",
      lastName: "",
      middleName: "",
      dateOfBirth: "",
      gender: "",
      firstNameErrors: [],
      middleNameErrors: [],
      lastNameErrors: [],
      dateOfBirthErrors: [],
      genderErrors: [],
      hasInvalidCredentials: false,
      genderOptions: ["Male", "Female", "Other"],
      fieldRules: [rules.required, rules.noNumbers],
      optionalFieldRules: [rules.noNumbers],
      dateRules: [rules.required],
      currentDate: moment().format("YYYY-MM-DD")
    };
  },
  methods: {
    /**
     * Toggles into edit state or saves from edit state
     */
   toggleEditSave() {
      if (this.isEditing) {
        // if user was editing and has now submitted their changes
        if (!this.$refs.form.validate()) {
          return;
        }

        const userId = localStorage.getItem("userId");
        const { userProfile } = this;

        const oldBasicInfo = {
          firstName: userProfile.firstName,
          middleName: userProfile.middleName,
          lastName: userProfile.lastName,
          dateOfBirth: userProfile.dateOfBirth,
          gender: userProfile.gender
        };

        const newBasicInfo = {
          firstName: this.firstName,
          middleName: this.middleName,
          lastName: this.lastName,
          dateOfBirth: this.dateOfBirth,
          gender: this.gender
        }

        const command = async (basicInfo) => {
          await updateBasicInfo(userId, basicInfo);
          const mergedUserProfile = {...userProfile, ...basicInfo};
          UserStore.methods.setData(mergedUserProfile);
          this.$emit("update:userProfile", mergedUserProfile);
        };

        const undoCommand = command.bind(null, oldBasicInfo);
        const redoCommand = command.bind(null, newBasicInfo);
        const updateBasicInfoCommand = new Command(undoCommand, redoCommand);
        this.$refs.undoRedo.addUndo(updateBasicInfoCommand);
        redoCommand(newBasicInfo); // actually make the update

        this.isEditing = false;
      } else {
        this.isEditing = true;
      }
    },
    /**
     * Sets edit data from read-only data
     */
   setEditState() {
      this.firstName = this.userProfile.firstName;
      this.middleName = this.userProfile.middleName;
      this.lastName = this.userProfile.lastName;
      this.dateOfBirth = this.userProfile.dateOfBirth;
      this.gender = this.userProfile.gender;
  }
  },
  /**
   * Sets edit state
   */
  mounted: function() {
    this.setEditState();
  },
  watch: {
    // Used to set date picker to YEAR mode after resetting
    dateMenu(val) {
      val && setTimeout(() => (this.$refs.picker.activePicker = 'YEAR'))
    }
  }
}

</script>
<style lang="scss" scoped>
h3 {
  font-size: 1.2rem;
}

b {
  font-size: 1.05rem;
}

#basic-info {
  padding: 5px 10px 5px 10px;
}

.profile-item {
  margin-top: 7px;
  margin-bottom: 7px;
}

.item-signifier {
  display: inline-block;
  width: 50%;
}

.item-value {
  display: inline-block;
  width: 50%;
}

#header {
  width: 100%;
  display: flex;
  flex-flow: row nowrap;
  justify-content: space-between;
  align-items: center;

  h3 {
    text-align: left;
  }
}
</style>

<style lang="scss">
// Overrides to remove margin and padding for inputs
#basic-info {
  .v-input__slot {
    margin-bottom: 2px;
  }

  .v-text-field {
    padding-top: 0;
    margin-top: 0;
  }
}
</style>
