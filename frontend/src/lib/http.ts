import axios, { type InternalAxiosRequestConfig } from "axios";
import type { Router } from "vue-router";

interface AuthStoreLike {
  accessToken: string | null;
  refreshSession: () => Promise<void>;
  clearSession: () => void;
}

interface RetryRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

const API_BASE_URL = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000
});

let isInitialized = false;
let isRefreshing = false;
const waitingQueue: Array<{
  resolve: (token: string) => void;
  reject: (reason?: unknown) => void;
}> = [];

function bypassRefresh(url?: string): boolean {
  if (!url) {
    return false;
  }
  return ["/api/auth/login", "/api/auth/register", "/api/auth/refresh"].some((path) => url.includes(path));
}

function flushQueue(error: unknown, token: string | null): void {
  while (waitingQueue.length > 0) {
    const item = waitingQueue.shift();
    if (!item) {
      continue;
    }
    if (error) {
      item.reject(error);
    } else if (token) {
      item.resolve(token);
    } else {
      item.reject(new Error("Token refresh failed"));
    }
  }
}

export function setupHttpInterceptors(getAuthStore: () => AuthStoreLike, router: Router): void {
  if (isInitialized) {
    return;
  }
  isInitialized = true;

  http.interceptors.request.use((config) => {
    const store = getAuthStore();
    if (store.accessToken) {
      config.headers = config.headers ?? {};
      config.headers.Authorization = `Bearer ${store.accessToken}`;
    }
    return config;
  });

  http.interceptors.response.use(
    (response) => response,
    async (error) => {
      const store = getAuthStore();
      const responseStatus = error.response?.status;
      const originalRequest = error.config as RetryRequestConfig | undefined;

      if (!originalRequest || responseStatus !== 401 || originalRequest._retry || bypassRefresh(originalRequest.url)) {
        throw error;
      }

      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          waitingQueue.push({
            resolve: (token) => {
              originalRequest.headers = originalRequest.headers ?? {};
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(http(originalRequest));
            },
            reject
          });
        });
      }

      isRefreshing = true;
      try {
        await store.refreshSession();
        const renewedToken = store.accessToken;
        flushQueue(null, renewedToken);

        if (renewedToken) {
          originalRequest.headers = originalRequest.headers ?? {};
          originalRequest.headers.Authorization = `Bearer ${renewedToken}`;
        }

        return http(originalRequest);
      } catch (refreshError) {
        flushQueue(refreshError, null);
        store.clearSession();
        await router.push({ name: "login" });
        throw refreshError;
      } finally {
        isRefreshing = false;
      }
    }
  );
}
