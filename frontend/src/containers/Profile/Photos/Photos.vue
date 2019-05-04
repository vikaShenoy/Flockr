<template>
    <div>
        <h3 style="margin-bottom: 4px;margin-top: 4px;">Photos</h3>

        <v-card  id="photos">
            <v-responsive>
                <v-container grid-list-sm fluid>

                    <!-- users photos -->
                    <v-layout v-if="photos.length" row wrap>
                        <v-flex sm12 md6 lg4 v-for="photo in photos" v-bind:key="photo">
                            <v-img class="photo clickable" v-on:click.stop="openViewPhotoDialog(photo)" :src="getThumbnailPhotoEndpoint(photo.filename)"></v-img>
                        </v-flex>
                    </v-layout>

                    <!-- default photos -->
                    <v-layout v-else row wrap>
                        <v-spacer align="center">
                            <h3 class="font-weight-regular">This user has no photos.</h3>
                        </v-spacer>
                    </v-layout>

                </v-container>
            </v-responsive>

            <v-card-actions>
                <v-spacer align="center">
                    <add-photo-dialog
                            :dialog="addPhotoDialog"
                            v-on:closeDialog="closeAddPhotoDialog"
                    ></add-photo-dialog>
                </v-spacer>
            </v-card-actions>
        </v-card>

        <view-photo-dialog
            :dialog="viewPhotoDialog"
            :photo="currentPhoto"
            v-on:closeDialog="closeViewPhotoDialog"
        ></view-photo-dialog>

    </div>
</template>

<script>
  import AddPhotoDialog from "./AddPhotoDialog/AddPhotoDialog";
  import {endpoint} from "../../../utils/endpoint";
  import {getPhotos} from "./PhotosService";
  import ViewPhotoDialog from "./ViewPhotoDialog/ViewPhotoDialog";

  export default {

    components: {
      AddPhotoDialog,
      ViewPhotoDialog
    },

    data() {
      return {
        addPhotoDialog: false,
        photos: [],
        userId: null,
        currentPhoto: null,
        viewPhotoDialog: false
      };
    },

    methods: {

      /**
       * Gets a list of photos for this user from the backend and adds it to the photos array.
       */
      getUserPhotos: async function () {
        try {
          this.photos = await getPhotos(this.userId, 6);
        } catch (error) {
          if (error.status === 401) {
            // Need to log in (pop up then this.$router.push('/')
          } else if (error.status === 404) {
            // User doesn't exist (pop up then this.$router.back(); ???
          }
        }
      },

      /**
       * Gets the photo thumbnail endpoint to query for the given photo filename
       *
       * @param photoFilename the filename of the photo
       * @return String the url of photos the endpoint
       */
      getThumbnailPhotoEndpoint: function (photoFilename) {
        return endpoint(`/travellers/photos/${photoFilename}/thumbnail`);
      },

      /**
       * Called when a photo is clicked on.
       * Sets the current photo as the one clicked on.
       * Opens the view photo dialog by setting viewPhotoDialog to true
       *
       * @param photo the photo clicked on by the user
       */
      openViewPhotoDialog: function (photo) {
        this.currentPhoto = photo;
        this.viewPhotoDialog = true;
      },

      /**
       * Called when the view photo dialog is closed by the user.
       * Sets the current photo to null again.
       * Closes the view photo dialog by setting viewPhotoDialog to false
       */
      closeViewPhotoDialog: function () {
        this.currentPhoto = null;
        this.viewPhotoDialog = false;
      },

      /**
       * Called when the add photo dialog is closed by the user.
       * Closes the add photo dialog by setting addPhotoDialog to false.
       */
      closeAddPhotoDialog: function () {
        this.addPhotoDialog = false;
      }

    },

    mounted: function () {
      this.userId = this.$route.params.id;
      this.photos = this.getUserPhotos();
    }
  }
</script>

<style lang="scss" scoped>

    #photos {
        padding: 20px;
    }

    .photo {
        width: 100%;
        height: auto;
    }

    .clickable :hover {
        cursor: pointer;
    }

</style>


