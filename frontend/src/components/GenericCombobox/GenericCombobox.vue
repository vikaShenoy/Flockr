<template>
    <v-combobox
        :items="items"
        :item-text="itemText"
        :loading="isLoadingItems"
        color="secondary"
        :value="value"
        :label="label"
        @update:searchInput="handleTyping"
        @input="handleNewItemSelection"
        :multiple="multiple"
        :required="required"
        :hide-selected="multiple"
        :chips="multiple"
        :deletable-chips="multiple"
        :rules="rules"
    />
</template>

<script>
export default {
    props: {
        itemText: { // specifies what to display for each item, if a primitive will just show the primitive
            type: [String, Function],
            required: false,
            default: undefined
        },
        label: { // the label shown before anything is selected e.g. Destinations
            type: String,
            required: true
        },
        getFunction: { // the function called to get items
            // NOTE: will need to only take one parameter: the search string to filter items by
            type: Function,
            required: false
        },
        multiple: {
            type: Boolean,
            default: false
        },
        required: { // will be passed down to underlying v-combobox for integration with v-form components
            type: Boolean,
            required: false,
            default: false
        },
        value: {
            type: [Array, Object]
        },
        rules: {
            type: Array,
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
         * @param newSelectedItem the new item selected by the user
         */
        handleNewItemSelection(newSelectedItem) {
            if (Array.isArray(newSelectedItem)) {
                const items = newSelectedItem.filter(item => typeof item !== "string");
                this.$emit("input", items);
            } else {
                this.$emit("input", newSelectedItem);
            }
        },
        /**
         * Get the items based on what the user search as the search string.
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
    },
}
</script>
