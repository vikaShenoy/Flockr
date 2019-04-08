<template>
  <div class="admin-panel">
    <h2>Admin Panel</h2>
    <ManageUsers v-on:editUserById="handleEditUserById"/>
    
  </div>
</template>


<script>
import ManageUsers from "./ManageUsers/ManageUsers.vue";

export default {
  components: {
    ManageUsers
  },
  data() {
    return {

    }
  },
  methods: {
    // event handler for when a child component wants to edit a user by id
    handleEditUserById: function(userId) {
      console.log(`Wanting to edit user ${userId} in admin panel`);
    }
  }
};
</script>


<style lang="scss" scoped>
  .admin-panel {
    width: 100%;
    height: 100%;
    padding: 10px;
  }
</style>
