<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const isAuthPage = computed(() => route.name === "login" || route.name === "register");

async function onLogout(): Promise<void> {
  await authStore.logout();
  await router.push({ name: "login" });
}
</script>

<template>
  <div class="app-bg">
    <div class="shape shape-left" />
    <div class="shape shape-right" />

    <header class="topbar">
      <div class="brand">Secure Portal</div>
      <nav class="nav-links">
        <RouterLink to="/profile" class="nav-item">Profile</RouterLink>
        <RouterLink v-if="authStore.isAdmin" to="/admin/users" class="nav-item">Admin</RouterLink>
        <RouterLink v-if="!authStore.accessToken" to="/login" class="nav-item">Login</RouterLink>
        <RouterLink v-if="!authStore.accessToken" to="/register" class="nav-item">Register</RouterLink>
        <button v-if="authStore.accessToken" class="ghost-btn" type="button" @click="onLogout">Logout</button>
      </nav>
    </header>

    <main class="page-wrap" :class="{ compact: isAuthPage }">
      <RouterView v-slot="{ Component }">
        <Transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </Transition>
      </RouterView>
    </main>
  </div>
</template>

<style scoped>
.app-bg {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 999px;
  filter: blur(4px);
  opacity: 0.45;
  pointer-events: none;
}

.shape-left {
  width: 420px;
  height: 420px;
  background: radial-gradient(circle at 30% 30%, #95b8ff 0%, #6c89d4 60%, #3c507f 100%);
  top: -150px;
  left: -120px;
}

.shape-right {
  width: 520px;
  height: 520px;
  background: radial-gradient(circle at 60% 40%, #98d8d2 0%, #5ea8a1 60%, #2f6f72 100%);
  right: -200px;
  bottom: -260px;
}

.topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.2rem 2rem;
}

.brand {
  font-size: 1.4rem;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.nav-links {
  display: flex;
  gap: 0.8rem;
  align-items: center;
}

.nav-item,
.ghost-btn {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.2);
  color: #12223d;
  padding: 0.45rem 0.8rem;
  border-radius: 999px;
  text-decoration: none;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.nav-item:hover,
.ghost-btn:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.35);
}

.page-wrap {
  position: relative;
  z-index: 2;
  width: min(1120px, 92vw);
  margin: 1.2rem auto 2rem;
}

.page-wrap.compact {
  width: min(560px, 92vw);
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
