<template>
    <v-card
            id="add-trip"
            class="col-lg-10 offset-lg-1"
            :flat="!!isSidebarComponent"
    >
        <h2 v-if="!isSidebarComponent">Add Trip</h2>
        <h3 id="title-subtrip" v-else>Create New Subtrip</h3>
        <v-form ref="addTripForm">
            <v-text-field
                    v-model="tripName"
                    label="Trip Name"
                    color="secondary"
                    class="col-md-6"
                    :rules="tripNameRules"
            >
            </v-text-field>

            <v-combobox v-if="!isSidebarComponent"
                        :items="users" :item-text="formatName"
                        v-model="selectedUsers" label="Users" multiple class="col-md-6"></v-combobox>

            <ul>
                <li
                  v-for="user in selectedUsers"
                  v-bind:key="user.userId"
                  class="selected-user"
                >
                    {{ formatName(user) }} <v-select v-model="user.userRole" :items="roleTypes" class="role-type" color="secondary" item-text="name" item-value="value"></v-select>
                </li>
            </ul>

            <TripTable :tripDestinations="tripDestinations"/>

            <v-btn
                    depressed
                    color="secondary"
                    small
                    id="add-destination"
                    @click="addDestination"
            >
                <v-icon>add</v-icon>
            </v-btn>

            <v-btn
                    depressed
                    color="secondary"
                    id="add-trip-btn"
                    @click="addTrip"
            >
                Create
            </v-btn>

            <v-btn
                    depressed
                    color="error"
                    id="cancel-trip-creation-btn"
                    @click="$emit('cancel-trip-creation')"
            >
                Cancel
            </v-btn>

        </v-form>
    </v-card>
</template>

<script>
    import TripTable from "../../components/TripTable/TripTable";
    import {createTrip, getAllUsers} from "./AddTripService.js";
    import UserStore from "../../stores/UserStore";
    import roleType from "../../stores/roleType"


    const rules = {
        required: field => !!field || "Field required"
    };

    const tripDestination = {
        destinationId: null,
        arrivalDate: null,
        arrivalTime: null,
        departureDate: null,
        departureTime: null,
    };

    export default {
        components: {
            TripTable
        },
        props: {
            isSidebarComponent: {
                type: Boolean,
                required: false
            },
            parentTrip: {
                type: Object,
                required: false
            }

        },
        data() {
            return {
                tripName: "",
                tripDestinations: [{...tripDestination, id: 0}, {...tripDestination, id: 1}],
                tripNameRules: [rules.required],
                selectedUsers: [],
                users: null,
                roleTypes: [
                    {
                        name: "Trip Manager",
                        value: roleType.TRIP_MANAGER
                    },
                    {
                        name: "Trip Member",
                        value: roleType.TRIP_MEMBER
                    },
                    {
                        name: "Trip Owner",
                        value: roleType.TRIP_OWNER
                    }
                ]
            };
        },
        mounted() {
            this.getUsers();
        },
        methods: {
            /**
             * Gets all users and filters out the logged in user
             */
            async getUsers() {
                const users = (await getAllUsers())
                    .filter(user => user.userId !== UserStore.data.userId);
                this.users = users;
            },
            /**
             * Adds an empty destination
             */
            addDestination() {
                this.tripDestinations.push({...tripDestination, id: this.tripDestinations.length});
            },
            /**
             * Iterates through destinations and check and renders error message
             * if destinations are contiguous.
             * @returns {boolean} True if contiguous destinations are found, false otherwise.
             */
            contiguousDestinations() {
                let foundContiguousDestination = false;

                this.$set(this.tripDestinations[0], "destinationErrors", []);

                for (let i = 1; i < this.tripDestinations.length; i++) {
                    if (this.tripDestinations[i].destinationId === this.tripDestinations[i - 1].destinationId && this.tripDestinations[i].destinationId) {
                        this.$set(this.tripDestinations[i], "destinationErrors", ["Destination is same as last destination"]);
                        foundContiguousDestination = true;
                        continue;
                    }
                    this.tripDestinations[i].destinationErrors = [];
                }
                return foundContiguousDestination;
            },
            /**
             * Validates and renders errors if there are any
             * @returns {boolean} True if fields are valid, false otherwise
             */
            validate() {
                const validFields = this.$refs.addTripForm.validate();
                const contiguousDestinations = this.contiguousDestinations();
                if (!validFields || contiguousDestinations) {
                    return false;
                }

                return true;
            },
            /**
             * Validates fields before sending a request to add a trip
             */
            async addTrip() {
                const validFields = this.validate();
                if (!validFields) return;
                // Specifies the extra users that should be added to the trip
                let userIds = [];
                this.selectedUsers.forEach(function (selectedUser) {
                    userIds.push({userId: selectedUser.userId, role: selectedUser.userRole});
                });

                const subTrip = await createTrip(this.tripName, this.tripDestinations, userIds);

                // If this is happening on the sidebar, the new trip is a subtrip. This adds it to parent trip.
                if (this.isSidebarComponent) {
                    subTrip.isShowing = false;
                    this.$emit("close-dialog");

                }
                this.$emit("newTripAdded", subTrip);
            },
            /**
             * Formats a users full name by their first name and last name
             */
            formatName(user) {
                return `${user.firstName} ${user.lastName}`;
            }
        }
    };
</script>

<style lang="scss" scoped>
    #add-trip {
        margin-top: 30px;
        height: 465px;

        h2 {
            text-align: center;
            padding-top: 10px;
        }
    }

    #add-destination {
        margin-top: 10px !important;
        display: block;
        margin: 0 auto;
    }

    #cancel-trip-creation-btn {
        float: right;
    }

    #add-trip-btn {
        float: right;
    }

    .role-type {
        margin-left: 10px;
        flex: none;
        width: 150px;
    }

    .selected-user {
        display: flex;
        align-items: center;
    }
</style>


