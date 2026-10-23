<script setup lang="ts">
import { onMounted } from "vue";
import { useAuthStore } from "./stores/auth";
import NavBar from "./components/NavBar.vue";
import AppFooter from "./components/AppFooter.vue";

/**
 * Accès au store d'authentification global.
 */
const auth = useAuthStore();

/**
 * Dès que l'application démarre (montage du composant racine),
 * on déclenche l'initialisation de l'authentification.
 * Cela permet de vérifier immédiatement si un token est présent dans le localStorage
 * et de restaurer la session de l'utilisateur avant même qu'il ne commence à naviguer.
 */
onMounted(() => auth.init());
</script>

<template>
	<NavBar />
	<main class="app-main">
		<RouterView />
	</main>
	<AppFooter />
</template>
