<script setup lang="ts">
import { useAuthStore } from "../stores/auth";
import { useRouter } from "vue-router";

const auth = useAuthStore();
const router = useRouter();

function handleLogout(): void {
	auth.logout();
	router.push({ name: "home" });
}
</script>

<template>
	<nav class="navbar">
		<RouterLink to="/" class="navbar-brand">
			<img src="/logo.svg" class="brand-icon" alt="BetArena" />
			BetArena
		</RouterLink>

		<div class="navbar-links">
			<RouterLink to="/lobby" v-if="auth.isAuthenticated">Lobby</RouterLink>
			<RouterLink to="/admin" v-if="auth.isBookmaker">Admin</RouterLink>
		</div>

		<div class="navbar-right">
			<template v-if="auth.isAuthenticated">
				<span class="balance-chip" v-if="auth.isBettor"> {{ auth.balance.toLocaleString("fr-BE") }} Bt </span>
				<RouterLink to="/profile" class="avatar-btn" :title="auth.user?.username">
					{{ auth.user?.username?.charAt(0).toUpperCase() }}
				</RouterLink>
				<button class="btn btn-ghost btn-sm" @click="handleLogout">Déconnexion</button>
			</template>

			<template v-else>
				<RouterLink to="/login" class="btn btn-ghost btn-sm"> Connexion </RouterLink>
				<RouterLink to="/register" class="btn btn-primary btn-sm"> S'inscrire </RouterLink>
			</template>
		</div>
	</nav>
</template>
