<template>
    <v-combobox
        :items="items"
        :item-text="itemText"
        :loading="isLoadingItems"
        color="secondary"
        v-model="selectedItem"
        :label="label"
        @update:searchInput="handleTyping"
        @input="handleNewItemSelection"
    />
</template>

<script>
export default {
    props: {
        itemText: { // specifies what to display for each item, if a primitive will just show the primitive
            type: String,
            required: false,
            default: undefined
        },
        label: {
            type: String,
            required: true
        },
        getFunction: { // the function called to get items
            // NOTE: will need to only take one parameter: the search string to filter items by
            type: Function,
            required: false
        }
    },
    data() {
        return {
            items: [],
            searchString: "",
            selectedItem: null,
            watchdog: null, // timeout for debouncing
            isLoadingItems: false
        };
    },
    mounted() {
        this.getItems();
    },
    methods: {
        /**
         * Called when the user types into the combobox.
         * @param input the new text.
         */
        handleTyping(input) {
            this.searchString = input;
            const timeNeededSinceLastKeyPress = 400; // in milliseconds, how long to wait between keypresses until sending a request
            clearTimeout(this.watchdog);
            this.watchdog = setTimeout(this.getItems, timeNeededSinceLastKeyPress);
        },
        /**
         * Called when a new item is selected
         * @param newSelectedItem
         */
        handleNewItemSelection(newSelectedItem) {
            this.$emit('item-selected', newSelectedItem);
        },
        /**
         * Get the items
         * @returns {Promise<void>}
         */
        async getItems() {
            try {
                this.isLoadingItems = true;
                this.items = await this.getFunction(this.searchString);
                this.isLoadingItems = false;
            } catch (err) {
                this.$root.$emit("show-error-snackbar", `Could not get items for ${this.label}`, 3000);
                this.isLoadingItems = false;
            }
        }
    }
}
</script>
