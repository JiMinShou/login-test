import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import type { Role } from "@/types/auth";
import LoginView from "@/views/LoginView.vue";
import RegisterView from "@/views/RegisterView.vue";
import ProfileView from "@/views/ProfileView.vue";
import AdminUsersView from "@/views/AdminUsersView.vue";

declare module "vue-router" {
  interface RouteMeta {
    requiresAuth?: boolean;
    roles?: Role[];
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/profile" },
    { path: "/login", name: "login", component: LoginView },
    { path: "/register", name: "register", component: RegisterView },
    {
      path: "/profile",
      name: "profile",
      component: ProfileView,
      meta: { requiresAuth: true, roles: ["USER", "ADMIN"] }
    },
    {
      path: "/admin/users",
      name: "admin-users",
      component: AdminUsersView,
      meta: { requiresAuth: true, roles: ["ADMIN"] }
    }
  ]
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();

  if (!authStore.initialized) {
    await authStore.initialize();
  }

  if (to.meta.requiresAuth && !authStore.accessToken) {
    return { name: "login", query: { redirect: to.fullPath } };
  }

  if (to.meta.roles && (!authStore.user || !to.meta.roles.includes(authStore.user.role))) {
    return { name: "profile" };
  }

  if ((to.name === "login" || to.name === "register") && authStore.accessToken) {
    return { name: "profile" };
  }

  return true;
});

export default router;
