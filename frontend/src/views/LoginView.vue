<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { AxiosError } from "axios";
import { useAuthStore } from "@/stores/auth";
import type { ApiError } from "@/types/auth";

const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const identifier = ref("");
const password = ref("");
const loading = ref(false);
const error = ref("");

async function submit(): Promise<void> {
  error.value = "";
  loading.value = true;
  try {
    await authStore.login({
      identifier: identifier.value.trim(),
      password: password.value
    });

    const redirect = typeof route.query.redirect === "string" ? route.query.redirect : "/profile";
    await router.push(redirect);
  } catch (err) {
    const apiError = (err as AxiosError<ApiError>).response?.data;
    error.value = apiError?.message ?? "login failed";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="panel">
    <h1 class="page-title">Welcome Back</h1>
    <p class="page-desc">Sign in with your username or email to continue.</p>

    <form @submit.prevent="submit">
      <div class="field">
        <label for="identifier">Username / Email</label>
        <input id="identifier" v-model="identifier" type="text" autocomplete="username" required />
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" autocomplete="current-password" required />
      </div>

      <button class="primary-btn" type="submit" :disabled="loading">
        {{ loading ? "Signing in..." : "Sign In" }}
      </button>

      <p v-if="error" class="error-text">{{ error }}</p>
    </form>

    <p>
      New here?
      <RouterLink class="inline-link" to="/register">Create an account</RouterLink>
    </p>
  </section>
</template>
