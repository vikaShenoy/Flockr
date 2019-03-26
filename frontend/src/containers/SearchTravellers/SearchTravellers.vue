<template>
    <div style="width: 100%">
        <div class="page-title"><h1>Search Travellers</h1></div>

        <!-- Min and Max Age Slider -->
        <div style="padding: 12% 0% 0% 0%;">
            <v-card flat color="transparent">
            <v-card-text row
            class="age-slider">Select Age Range
                <v-layout>
                    <v-flex
                    shrink
                    style="width: 60px"
                    >
                    <v-text-field
                        v-model="ageRange[0]"
                        class="mt-0"
                        hide-details
                        single-line
                        type="number"
                    ></v-text-field>
                    </v-flex>
                    <v-flex class="px-3">
                    <v-range-slider
                        v-model="ageRange"
                        :min="minAge"
                        :max="maxAge"
                    ></v-range-slider>
                    </v-flex>
                    <v-flex
                    shrink
                    style="width: 60px"
                    >
                    <v-text-field
                        v-model="ageRange[1]"
                        class="mt-0"
                        hide-details
                        single-line
                        type="number"
                    ></v-text-field>
                    </v-flex>
                </v-layout>
            </v-card-text>
            </v-card>
        </div>

        <!-- Nationality, Gender and Traveller Typ Select Input -->
        <div class="col-md-12">
            <div class="row">
                 <v-select 
                class="selector-input"
                label="Nationality"
                :items="nationalities.names"
                :value="nationality"
                v-model="nationality"
                ></v-select>

                <v-select
                class="selector-input"
                label="Gender"
                :items="genders"
                :value="gender"
                v-model="gender"
                ></v-select>

                <v-select
                class="selector-input"
                label="Traveller Type"
                :items="travellerTypes.names"
                :value="travellerType"
                v-model="travellerType"
                ></v-select>

                <v-btn id="searchButton" v-on:click="search" class="button">Search</v-btn>
            </div>
        </div>


        <!-- Table Result -->
        <v-data-table
            :headers="headers"
            :items="users"
            class="elevation-1"
        >
            <template v-slot:items="props">
            <td class="text-xs-left">{{ props.item.name }}</td>
            <td class="text-xs-left">{{ props.item.age }}</td>
            <td class="text-xs-left">{{ props.item.gender }}</td>
            <td class="text-xs-left">{{ props.item.nationality }}</td>
            <td class="text-xs-left">{{ props.item.travellerType }}</td>
            </template>
        </v-data-table>
    </div>
</template>

<script>
import {requestTravellers} from "./SearchTravellersService";
import {getNationalities} from "../Profile/Nationalities/NationalityService";
import {getAllTravellerTypes} from "../Profile/TravellerTypes/TravellerTypesService";
export default {
    data() {
        return {
            nationalities: {
                names: [],
                ids: []
            },
            minAge: 18,
            maxAge: 60,
            ageRange: [18, 60],
            genders: ["Female", "Male", "Other"],
            travellerTypes: [],
            nationality: "",
            travellerType: "",
            gender: "",

            headers: [
          { text: 'Name', align: 'left', sortable: true, value: 'name' },
          { text: 'Age', align: 'left', sortable: true, value: 'age' },
          { text: 'Gender', align: 'left', sortable: true, value: 'gender'},
          { text: 'Nationality', align: 'left', sortable: true, value: 'nationality'},
          { text: 'Traveller Type(s)', align:'left', sortable: true, value: 'travellerType' },
        ],
        travellers: []
        }
    },
    mounted: async function () {
        try {
            this.travellers = await requestTravellers("");
        } catch(error) {
            console.log(error);
        }
        let currentNationalities;
        try {
            currentNationalities = await getNationalities();

            for (let index = 0; index < currentNationalities.length; index++) {
                this.nationalities.names.push(currentNationalities[index].nationalityCountry);
                this.nationalities.ids.push(currentNationalities[index].nationalityId);
            }
        } catch (error) {
            console.log(error);
        }
        // let currentTravellerTypes;
        // try {
        //     currentNationalities = await getNationalities();

        //     for (let index = 0; index < currentNationalities.length; index++) {
        //         this.nationalities.names.push(currentNationalities[index].nationalityCountry);
        //         this.nationalities.ids.push(currentNationalities[index].nationalityId);
        //     }
        // } catch (error) {
        //     console.log(error);
        // }
    }, 
    methods: {
         // search: function () {
        //     // get the queries from the selector variables
        //     // parse them into an acceptable format to be sent
        //     // call the get travellers function passing in the formated queries
            
        //     this.travellers = 
        // }

    }
}
</script>

<style lang="scss" scoped>
@import "../../styles/_variables.scss";

.page-title {
  position: fixed;
  z-index: 1;
  width: 100%;
  padding: 15px;
  text-align: left;
  background: black;
  color: $primary;
  font-size: 30px;
  h1 {
    color: white;
  }
}

.title-card {
  display: inline-block;
}

.selector-input {
    padding: 0px 0 0px 10px;
    text-align: left;
}

.age-slider {
    padding: 0 20% 0 20%;
}

.button {
    background-color: $primary;
}


</style>
