import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "@/router";
import { useAuthStore } from "@/stores/auth";
import { setupHttpInterceptors } from "@/lib/http";
import "./style.css";

async function bootstrap(): Promise<void> {
  const app = createApp(App);
  const pinia = createPinia();
  app.use(pinia);
  app.use(router);

  const authStore = useAuthStore(pinia);
  setupHttpInterceptors(() => authStore, router);
  await authStore.initialize();

  app.mount("#app");
}

void bootstrap();
