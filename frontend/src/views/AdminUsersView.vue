<script setup lang="ts">
import { onMounted, ref } from "vue";
import { AxiosError } from "axios";
import { http } from "@/lib/http";
import type { ApiError, UserSummary } from "@/types/auth";

const users = ref<UserSummary[]>([]);
const loading = ref(false);
const error = ref("");

async function loadUsers(): Promise<void> {
  loading.value = true;
  error.value = "";
  try {
    const { data } = await http.get<UserSummary[]>("/api/admin/users");
    users.value = data;
  } catch (err) {
    const apiError = (err as AxiosError<ApiError>).response?.data;
    error.value = apiError?.message ?? "failed to load users";
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  await loadUsers();
});
</script>

<template>
  <section class="panel">
    <div class="header-row">
      <div>
        <h1 class="page-title">Admin User List</h1>
        <p class="page-desc">Only users with ADMIN role can access this resource.</p>
      </div>
      <button class="refresh-btn" :disabled="loading" @click="loadUsers">
        {{ loading ? "Loading..." : "Refresh" }}
      </button>
    </div>

    <p v-if="error" class="error-text">{{ error }}</p>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Email</th>
            <th>Role</th>
            <th>Created At</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id }}</td>
            <td>{{ user.username }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.role }}</td>
            <td>{{ new Date(user.createdAt).toLocaleString() }}</td>
          </tr>
          <tr v-if="!loading && users.length === 0">
            <td colspan="5">No users found.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.refresh-btn {
  border: 1px solid #b7c7e4;
  background: #f5f9ff;
  color: #274577;
  border-radius: 10px;
  padding: 0.5rem 0.8rem;
  font-weight: 600;
  cursor: pointer;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.table-wrap {
  margin-top: 0.7rem;
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 680px;
}

th,
td {
  text-align: left;
  padding: 0.65rem;
  border-bottom: 1px solid #d8e2f4;
}

th {
  font-weight: 700;
  color: #254677;
}
</style>
