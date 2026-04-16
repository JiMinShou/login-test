<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { AxiosError } from "axios";
import { useAuthStore } from "@/stores/auth";
import type { ApiError } from "@/types/auth";

const authStore = useAuthStore();
const router = useRouter();

const username = ref("");
const email = ref("");
const password = ref("");
const confirmPassword = ref("");
const loading = ref(false);
const error = ref("");

const passwordHint = computed(() =>
  "8-64 chars, include uppercase, lowercase and number"
);

async function submit(): Promise<void> {
  error.value = "";
  if (password.value !== confirmPassword.value) {
    error.value = "Passwords do not match";
    return;
  }

  loading.value = true;
  try {
    await authStore.register({
      username: username.value.trim(),
      email: email.value.trim(),
      password: password.value
    });

    await authStore.login({ identifier: username.value.trim(), password: password.value });
    await router.push("/profile");
  } catch (err) {
    const apiError = (err as AxiosError<ApiError>).response?.data;
    error.value = apiError?.message ?? "registration failed";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="panel">
    <h1 class="page-title">Create Account</h1>
    <p class="page-desc">Set up your profile and start using the secure portal.</p>

    <form @submit.prevent="submit">
      <div class="field">
        <label for="username">Username</label>
        <input id="username" v-model="username" type="text" autocomplete="username" required />
      </div>

      <div class="field">
        <label for="email">Email</label>
        <input id="email" v-model="email" type="email" autocomplete="email" required />
      </div>

      <div class="field">
        <label for="password">Password</label>
        <input id="password" v-model="password" type="password" autocomplete="new-password" required />
      </div>

      <div class="field">
        <label for="confirmPassword">Confirm Password</label>
        <input
          id="confirmPassword"
          v-model="confirmPassword"
          type="password"
          autocomplete="new-password"
          required
        />
      </div>

      <p class="page-desc" style="margin-top: -0.4rem">{{ passwordHint }}</p>

      <button class="primary-btn" type="submit" :disabled="loading">
        {{ loading ? "Creating..." : "Create Account" }}
      </button>

      <p v-if="error" class="error-text">{{ error }}</p>
    </form>

    <p>
      Already have an account?
      <RouterLink class="inline-link" to="/login">Go to login</RouterLink>
    </p>
  </section>
</template>
