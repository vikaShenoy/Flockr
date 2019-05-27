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
                    Testing
                </v-card-text>
            </v-card>
        </v-dialog>

    </div>
</template>

<script>
    export default {
        name: "AddPhotoDialog",
        props: {
            showDialog: {
                type: Boolean,
                required: true
            }
        },
        data() {
            return {
                showAddPhotoDialog: false
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
            }
        },
        methods: {
            onShowDialogUpdated() {
                this.showAddPhotoDialog = this.showDialog;
            },
            onShowPhotoDialogUpdated() {
                this.$emit('closeAddPhotoDialog', this.showAddPhotoDialog);
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import "../../../../../styles/_variables.scss";
    @import "../../../../../styles/_defaults.scss";

    .light-text {
        -webkit-text-fill-color: $darker-white;
    }
</style>