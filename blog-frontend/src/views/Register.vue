<template>
  <div class="register-page">
    <Register />
  </div>
</template>

<script setup lang="ts">
import Register from '@/components/auth/Register.vue'
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>