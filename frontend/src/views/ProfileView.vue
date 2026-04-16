<script setup lang="ts">
import { computed } from "vue";
import { useAuthStore } from "@/stores/auth";

const authStore = useAuthStore();

const roleBadgeClass = computed(() => (authStore.user?.role === "ADMIN" ? "admin" : "user"));
</script>

<template>
  <section class="panel">
    <h1 class="page-title">My Profile</h1>
    <p class="page-desc">This page is available for authenticated users.</p>

    <div v-if="authStore.user" class="profile-grid">
      <article class="profile-card">
        <h3>Account Info</h3>
        <p><strong>ID:</strong> {{ authStore.user.id }}</p>
        <p><strong>Username:</strong> {{ authStore.user.username }}</p>
        <p><strong>Email:</strong> {{ authStore.user.email }}</p>
        <p>
          <strong>Role:</strong>
          <span class="role-badge" :class="roleBadgeClass">{{ authStore.user.role }}</span>
        </p>
      </article>

      <article class="profile-card">
        <h3>Session Status</h3>
        <p>Access Token loaded: <strong>{{ authStore.accessToken ? "Yes" : "No" }}</strong></p>
        <p>Refresh Token loaded: <strong>{{ authStore.refreshToken ? "Yes" : "No" }}</strong></p>
        <p v-if="authStore.user.role === 'ADMIN'">Admin privileges are enabled for your account.</p>
        <p v-else>Standard user permissions are active.</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 1rem;
}

.profile-card {
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(196, 210, 236, 0.9);
  border-radius: 14px;
  padding: 1rem;
}

.profile-card h3 {
  margin-bottom: 0.6rem;
}

.role-badge {
  display: inline-block;
  padding: 0.18rem 0.6rem;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 700;
}

.role-badge.user {
  background: #d6e4ff;
  color: #2957b5;
}

.role-badge.admin {
  background: #caefe6;
  color: #1f806e;
}
</style>
