<template>
  <div
    id="root-container"
    v-if="userProfile"
  >
    <v-alert
      :value="shouldShowBanner()"
      color="info"
      icon="info"
    >
      Please fill in your full profile before using the site
    </v-alert>

    <div class="row">
      <div class="col-lg-4">
        <ProfilePic
          :profilePhoto="userProfile.profilePhoto"
          :photos="userProfile.personalPhotos"
          :userId="userProfile.userId"
          v-on:updateProfilePic="updateProfilePic"
          v-on:showError="showError"
        />

        <v-card id="undo-redo-card">
          <p>You can undo and redo your changes.</p>

          <UndoRedo ref="undoRedo" />
        </v-card>

        <BasicInfo
          :userProfile="userProfile"
          @update-basic-info="this.updateBasicInfo"
        />

        <!-- TODO: move undo redo to unified component -->
        <Photos
          :photos="userProfile.personalPhotos"
          @deletePhoto="deletePhoto"
          @undoDeletePhoto="undoDeletePhoto"
          @addPhoto="addImage"
          @showError="showError"
		  @addPhotoCommand="addPhotoCommand"
          />
      </div>

      <div class="col-lg-8">
        <Nationalities
          :userNationalities="userProfile.nationalities"
          @update-user-nationalities="updateUserNationalities"
          :userId="userProfile.userId"
        />
        <Passports
          :userPassports="userProfile.passports"
          @update-user-passports="updateUserPassports"
          :userId="userProfile.userId"
        />
        <TravellerTypes
          :userTravellerTypes="userProfile.travellerTypes"
          @update-user-traveller-types="updateUserTravellerTypes"
          :userId="userProfile.userId"
        />
        <div>
          <Trips
            :trips.sync="userProfile.trips"
            :userId="userProfile.userId"
          />
        </div>
      </div>
    </div>
    <Snackbar :snackbarModel="errorSnackbar" v-on:dismissSnackbar="errorSnackbar.show=false"></Snackbar>
  </div>
</template>

<script>
import ProfilePic from "./ProfilePic/ProfilePic";
import Nationalities from "./Nationalities/Nationalities";
import Passports from "./Passports/Passports";
import TravellerTypes from "./TravellerTypes/TravellerTypes";
import BasicInfo from "./BasicInfo/BasicInfo";
import Trips from "../Trips/Trips";
import Photos from "./Photos/Photos";
import UndoRedo from "../../components/UndoRedo/UndoRedo";
import Command from "../../components/UndoRedo/Command";
import UserStore from "../../stores/UserStore";
import moment from "moment";
import { getUser } from "./ProfileService";
import { updateBasicInfo } from "./BasicInfo/BasicInfoService";
import { updateNationalities } from "./Nationalities/NationalityService";
import { updatePassports } from "./Passports/PassportService";
import { updateTravellerTypes } from "./TravellerTypes/TravellerTypesService";
import { setProfilePictureToOldPicture } from "./ProfilePic/ProfilePicService";
import Snackbar from "../../components/Snackbars/Snackbar";
import {endpoint} from "../../utils/endpoint";
import { undoDeleteUserPhoto, deleteUserPhoto } from '../UserGallery/UserGalleryService';

export default {
  components: {
    Snackbar,
    ProfilePic,
    Nationalities,
    Passports,
    BasicInfo,
    TravellerTypes,
    Trips,
    Photos,
    UndoRedo
  },
  data() {
    return {
      userProfile: null,
      photos: null,
      errorSnackbar: {
        show: false,
        text: "",
        color: "error",
        duration: 3000,
        snackbarId: 1
      }
    };
  },
  mounted() {
    this.getUserInfo();
  },
  methods: {
    /**
	 * Add an undo/redo command to the stack.
	 * */
  	addPhotoCommand(command) {
        this.$refs.undoRedo.addUndo(command);
	},
    updateUserTravellerTypes(oldTravellerTypes, newTravellerTypes) {
      const userId = localStorage.getItem("userId");

      const command = async (travellerTypes) => {
        const travellerTypeIds = travellerTypes.map(t => t.travellerTypeId);
        await updateTravellerTypes(userId, travellerTypeIds);
        UserStore.data.travellerTypes = travellerTypes;
        this.userProfile.travellerTypes = travellerTypes;
      };

      const undoCommand = command.bind(null, oldTravellerTypes);
      const redoCommand = command.bind(null, newTravellerTypes);
      const updateTravellerTypesCommand = new Command(undoCommand, redoCommand);
      this.$refs.undoRedo.addUndo(updateTravellerTypesCommand);
      redoCommand(); // perform update
    },
    updateUserPassports(oldPassports, newPassports) {
      const userId = localStorage.getItem("userId");

      const command = async (passports) => {
        const passportIds = passports.map(p => p.passportId);
        await updatePassports(userId, passportIds);
        UserStore.data.passports = passports;
        this.userProfile.passports = passports;
      };

      const undoCommand = command.bind(null, oldPassports);
      const redoCommand = command.bind(null, newPassports);
      const updatePassportsCommand = new Command(undoCommand, redoCommand);
      this.$refs.undoRedo.addUndo(updatePassportsCommand);
      redoCommand(); // actually perform the update
    },
    updateUserNationalities(oldNationalities, newNationalities) {
      const userId = localStorage.getItem("userId");

      const command = async(nationalities) => {
        const nationalityIds = nationalities.map(nationality => nationality.nationalityId);
        await updateNationalities(userId, nationalityIds);
        UserStore.data.nationalities = nationalities;
        this.userProfile.nationalities = nationalities;
      };

      const undoCommand = command.bind(null, oldNationalities);
      const redoCommand = command.bind(null, newNationalities);
      const updateNationalitiesCommand = new Command(undoCommand, redoCommand);
      this.$refs.undoRedo.addUndo(updateNationalitiesCommand);
      redoCommand(); // perform the update
    },
    updateBasicInfo(oldBasicInfo, newBasicInfo) {
      const userId = localStorage.getItem("userId");
      const { userProfile } = this;

      const command = async (basicInfo) => {
        await updateBasicInfo(userId, basicInfo);
        const mergedUserProfile = {...userProfile, ...basicInfo};
        UserStore.methods.setData(mergedUserProfile);
        this.userProfile = mergedUserProfile;
      };

      const undoCommand = command.bind(null, oldBasicInfo);
      const redoCommand = command.bind(null, newBasicInfo);
      const updateBasicInfoCommand = new Command(undoCommand, redoCommand);
      this.$refs.undoRedo.addUndo(updateBasicInfoCommand);
      redoCommand(newBasicInfo); // actually make the update
    },
    /**
     * Called when a deletePhoto event is emitted from the photos component.
     * Removes the photo at the given index.
     *
     * @param index {Number} the index of the photo to be removed.
     */
    deletePhoto(index, shouldShowSnackbar) {
      this.userProfile.personalPhotos.splice(index, 1);
      if (shouldShowSnackbar) {
        this.errorSnackbar.color = "success";
        this.errorSnackbar.text = "Photo deleted successfully";
        this.errorSnackbar.show = true;
      }
    },
    /**
     * Called when a undoDeletePhoto event is emitted from the photos component. 
     * Adds the photo back to the list at the given index.
     * 
     * @param index {number} the index where the photo should be added.
     * @param photo {} the photo to add.
     */
    undoDeletePhoto(index, photo) {
      this.userProfile.personalPhotos.splice(index, 0, photo);
    },
    /**
     * Gets a users info and sets the users state
     */
    async getUserInfo() {
      const userId = this.$route.params.id;

      const user = await getUser(userId);

      // Change date format so that it displays on the basic info component.
      const formattedDate = user.dateOfBirth
        ? moment(user.dateOfBirth).format("YYYY-MM-DD")
        : "";

      user.dateOfBirth = formattedDate;

      this.userProfile = user;
    },
    shouldShowBanner() {
      return !(
        this.userProfile.firstName &&
        this.userProfile.lastName &&
        this.userProfile.gender &&
        this.userProfile.dateOfBirth &&
        this.userProfile.nationalities.length &&
        this.userProfile.travellerTypes.length
      );
    },
    /**
     * Updates the profile picture of a user after it has been changed.
     * Add an undo/redo command to the undo/redo stack.
     * @param oldPhoto the old profile picture.
     * @param newPhoto the new profile picture.
     */
    updateProfilePic(oldPhoto, newPhoto) {
      this.userProfile.profilePhoto = newPhoto;
      let undoCommand = async (profilePhoto) => {
        if (profilePhoto) {
          await setProfilePictureToOldPicture(profilePhoto);
        }
        this.userProfile.profilePhoto = profilePhoto;
      };
      undoCommand = undoCommand.bind(null, oldPhoto);

      let redoCommand = async (profilePhoto) => {
        await setProfilePictureToOldPicture(profilePhoto);
        this.userProfile.profilePhoto = profilePhoto;
      };
      redoCommand = redoCommand.bind(null, newPhoto);
      const updateProfilePicCommand = new Command(undoCommand, redoCommand);
      this.$refs.undoRedo.addUndo(updateProfilePicCommand);
    },
    /**
     * Shows an snackbar error message
     * @param {string} text the text to display on the snackbar
     */
    showError(text) {
      this.errorSnackbar.text = text;
      this.errorSnackbar.show = true;
      this.errorSnackbar.color = "error";
    },
    /**
     * Update an image in the front end and create and store undo/redo commands
     * for it.
     */
    addImage(image) {
      image.endpoint = endpoint(`/users/photos/${image["photoId"]}?Authorization=${localStorage.getItem("authToken")}`);
      image.thumbEndpoint = endpoint(`/users/photos/${image["photoId"]}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
      this.userProfile.personalPhotos.push(image);

      const undoCommand = (
        async (image) => {
          await deleteUserPhoto(image);
          this.undoAddPhoto(image);
        }
      ).bind(null, image);
      const redoCommand = (
        async (image) => {
          await undoDeleteUserPhoto(image);
          image.endpoint = endpoint(`/users/photos/${image["photoId"]}?Authorization=${localStorage.getItem("authToken")}`);
          image.thumbEndpoint = endpoint(`/users/photos/${image["photoId"]}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
          this.userProfile.personalPhotos.push(image);
        }
      ).bind(null, image);

      const undoUploadCommand = new Command(undoCommand, redoCommand);
      this.$refs.undoRedo.addUndo(undoUploadCommand);
    },
    undoAddPhoto(image) {
      this.userProfile.personalPhotos = this.userProfile.personalPhotos.filter(e => e.photoId !== image.photoId);
    }
  }
};
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";

#root-container {
  width: 100%;
  margin-left: 15px;
  margin-right: 15px;

  #undo-redo-card {
    margin-top: 10px;
    padding: 5px;
    display: flex;
    flex-direction: column;
    justify-content: flex-start;
    align-items: center;
  }
}
</style>


