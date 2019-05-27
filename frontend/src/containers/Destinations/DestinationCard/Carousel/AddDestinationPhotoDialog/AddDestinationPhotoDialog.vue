<template>
    <div>

        <v-dialog v-model="showAddPhotoDialog">
            <v-card>
                <v-card-title>
                    Testing
                </v-card-title>
                <v-card-text>
                    This will be the dialog to link personal photos to a destination
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

<style scoped>

</style>