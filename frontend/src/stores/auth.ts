import { defineStore } from "pinia";
import { http } from "@/lib/http";
import { clearStoredTokens, loadStoredTokens, saveStoredTokens } from "@/lib/token-storage";
import type { AuthResponse, LoginRequest, RegisterRequest, UserProfile } from "@/types/auth";

interface AuthState {
  user: UserProfile | null;
  accessToken: string | null;
  refreshToken: string | null;
  initialized: boolean;
}

export const useAuthStore = defineStore("auth", {
  state: (): AuthState => ({
    user: null,
    accessToken: null,
    refreshToken: null,
    initialized: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken && state.user),
    isAdmin: (state) => state.user?.role === "ADMIN"
  },
  actions: {
    async initialize(): Promise<void> {
      if (this.initialized) {
        return;
      }

      const stored = loadStoredTokens();
      this.accessToken = stored.accessToken;
      this.refreshToken = stored.refreshToken;

      if (this.accessToken) {
        try {
          await this.fetchProfile();
        } catch {
          if (this.refreshToken) {
            try {
              await this.refreshSession();
            } catch {
              this.clearSession();
            }
          } else {
            this.clearSession();
          }
        }
      }

      this.initialized = true;
    },

    async register(payload: RegisterRequest): Promise<void> {
      await http.post("/api/auth/register", payload);
    },

    async login(payload: LoginRequest): Promise<void> {
      const { data } = await http.post<AuthResponse>("/api/auth/login", payload);
      this.applyAuthResponse(data);
      await this.fetchProfile();
    },

    async refreshSession(): Promise<void> {
      if (!this.refreshToken) {
        throw new Error("refresh token missing");
      }

      const { data } = await http.post<AuthResponse>("/api/auth/refresh", {
        refreshToken: this.refreshToken
      });

      this.applyAuthResponse(data);
      await this.fetchProfile();
    },

    async fetchProfile(): Promise<void> {
      const { data } = await http.get<UserProfile>("/api/users/me");
      this.user = data;
    },

    async logout(): Promise<void> {
      try {
        if (this.refreshToken) {
          await http.post("/api/auth/logout", { refreshToken: this.refreshToken });
        }
      } finally {
        this.clearSession();
      }
    },

    clearSession(): void {
      this.user = null;
      this.accessToken = null;
      this.refreshToken = null;
      clearStoredTokens();
    },

    applyAuthResponse(response: AuthResponse): void {
      this.accessToken = response.accessToken;
      this.refreshToken = response.refreshToken;
      saveStoredTokens({
        accessToken: response.accessToken,
        refreshToken: response.refreshToken
      });
    }
  }
});
