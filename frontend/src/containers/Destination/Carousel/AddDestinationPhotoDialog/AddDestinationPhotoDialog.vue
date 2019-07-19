<template>
    <div>

        <v-dialog v-model="showAddPhotoDialog">
            <v-card>
                <v-card-title class="primary title">
                    <v-layout row>
                        <v-spacer align="center">
                            <h2 class="light-text">
                                <v-icon large>add_photo_alternate</v-icon>
                                Add Photos
                            </h2>
                        </v-spacer>
                    </v-layout>
                </v-card-title>
                <v-card-text>

                    <v-layout
                            v-if="userPhotos"
                            row
                            wrap>
                        <v-flex
                                v-for="photo in userPhotos"
                                :key="photo.filename"
                                d-flex
                                lg3
                                md4
                                sm6
                                xs12
                                class="photo-tile">
                            <v-img
                                    :src="thumbnailPhotoUrl(photo.photoId)"
                                    aspect-ratio="1"
                                    class="grey lighten-2">
                                <template v-slot:placeholder>
                                    <v-layout
                                            fill-height
                                            align-center
                                            justify-center>
                                        <v-progress-circular
                                                indeterminate
                                                color="grey lighten-5"/>
                                    </v-layout>
                                </template>
                                <v-btn fab v-on:click="addPhotoToDestination(photo.photoId)"><v-icon>add</v-icon></v-btn>
                            </v-img>
                        </v-flex>
                        <v-spacer align="center">
                            <h4 v-if="!userPhotos.length">{{ $route.params.userId ? "This user has " : "You have " }} no available photos for this destination</h4>
                        </v-spacer>
                    </v-layout>
                </v-card-text>
            </v-card>
        </v-dialog>

    </div>
</template>

<script>

    import superagent from 'superagent';
    import {endpoint} from '../../../../utils/endpoint';
    export default {
        name: "AddPhotoDialog",
        props: {
            showDialog: {
                type: Boolean,
                required: true
            },
            destinationId: Number,
            destinationPhotos: Array,
            userPhotos: Array
        },
        data() {
            return {
                showAddPhotoDialog: false,
                id: null
            }
        },
        watch: {
            showDialog: {
                handler: 'onShowDialogUpdated',
                immediate: true
            },
            showAddPhotoDialog: {
                handler: 'onShowPhotoDialogUpdated',
                immediate: true
            },
            destinationId: {
                handler: "onDestinationIdUpdated",
                immediate: true
            }
        },
        methods: {
            onShowDialogUpdated() {
                this.showAddPhotoDialog = this.showDialog;
            },
            onShowPhotoDialogUpdated() {
                this.$emit('closeAddPhotoDialog', this.showAddPhotoDialog);
            },
            thumbnailPhotoUrl(photoId) {
                const authToken = localStorage.getItem("authToken");
                const queryAuthorization = `?Authorization=${authToken}`;
                return endpoint(`/users/photos/${photoId}/thumbnail${queryAuthorization}`);
            },
            addPhotoToDestination: async function(photoId) {
                let data = {
                    photoId: photoId
                };
                let authToken = localStorage.getItem('authToken');

                try {
                    const res = await superagent.post(endpoint(`/destinations/${this.id}/photos`))
                        .set('Authorization', authToken)
                        .send(data);
                    this.showAddPhotoDialog = false;
                    let photo = res.body;
                    photo["endpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}?Authorization=${localStorage.getItem("authToken")}`);
                    photo["thumbEndpoint"] = endpoint(`/users/photos/${photo.personalPhoto.photoId}/thumbnail?Authorization=${localStorage.getItem("authToken")}`);
                    this.$emit("addPhoto", photo);
                } catch (e) {
                    this.$emit("displayError", e.message);
                }

            },
            onDestinationIdUpdated() {
                this.id = this.destinationId;
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import "../../../../styles/_variables.scss";
    @import "../../../../styles/_defaults.scss";

    .light-text {
        -webkit-text-fill-color: $darker-white;
    }

    @keyframes fadein {
        from { opacity: 0; border: none; }
        to   { opacity: 1;  }
    }

    .profile-photo {
        max-width: 200px;
        max-height: 200px;
        padding-left: 5px;
        padding-right: 5px;
        animation: fadein 1s;
    }

    #profile-photos-selection {
        padding: 20px;
    }

    .selected-photo {
        border: 2px solid $primary;
    }

    #user-gallery {
        h1 {
            color: $primary;
        }

        width: 100%;
        margin: 1.5em;
    }

    .photo-tile {
        padding: 0.2em;
    }

</style>