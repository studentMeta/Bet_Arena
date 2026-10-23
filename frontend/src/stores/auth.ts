import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { login, register, getMe } from "../api/auth";
import type { User, LoginCredentials, RegisterPayload } from "../types";

export const useAuthStore = defineStore("auth", () => {
	const token = ref<string | null>(localStorage.getItem("token"));
	const user = ref<User | null>(null);

	const isAuthenticated = computed(() => token.value !== null);
	const isBookmaker = computed(() => user.value?.role === "BOOKMAKER");
	const isBettor = computed(() => user.value?.role === "BETTOR");
	const balance = computed(() => user.value?.balance ?? 0);

	/**
	 * Must be called when the app loads.
	 * Restores the user's data after a hard page refresh using the stored token.
	 */
	async function init(): Promise<void> {
		if (token.value && !user.value) {
			try {
				user.value = await getMe();
			} catch {
				logout();
			}
		}
	}

	/**
	 * Authenticates the user with provided credentials.
	 * Updates the store state and persists the token in local storage.
	 */
	async function loginAction(credentials: LoginCredentials): Promise<void> {
		const data = await login(credentials);
		token.value = data.token;
		user.value = data.user;
		localStorage.setItem("token", data.token);
	}

	/**
	 * Registers a new user account.
	 * Automatically logs the user in upon successful registration.
	 */
	async function registerAction(payload: RegisterPayload): Promise<void> {
		const data = await register(payload);
		token.value = data.token;
		user.value = {
			id: data.id,
			username: data.username,
			email: data.email,
			role: data.role,
			balance: data.balance,
			createdAt: data.createdAt,
		};
		localStorage.setItem("token", data.token);
	}

	/**
	 * Clears the current user session.
	 * Resets store state and removes the authentication token.
	 */
	function logout(): void {
		token.value = null;
		user.value = null;
		localStorage.removeItem("token");
	}

	/**
	 * Updates the user's balance locally.
	 * Useful after a bet is placed to avoid an unnecessary API call to /me.
	 */
	function updateBalance(newBalance: number): void {
		if (user.value) {
			user.value = { ...user.value, balance: newBalance };
		}
	}

	return {
		token,
		user,
		isAuthenticated,
		isBookmaker,
		isBettor,
		balance,
		init,
		loginAction,
		registerAction,
		logout,
		updateBalance,
	};
});
