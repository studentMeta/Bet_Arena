<script setup lang="ts">
import { reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { useRouter, useRoute } from "vue-router";
import type { LoginCredentials } from "../types";

/**
 * Instanciation des outils globaux.
 * - auth: Permet d'appeler la mécanique de connexion dans Pinia.
 * - router: Le moteur de navigation (pour changer de page).
 * - route: L'objet contenant les infos de l'URL actuelle.
 */
const auth = useAuthStore();
const router = useRouter();
const route = useRoute();

/**
 * État réactif du formulaire et de l'interface.
 */
const form = reactive<LoginCredentials>({ email: "", password: "" });
const error = ref("");
const loading = ref(false);

/**
 * Gère la soumission du formulaire de connexion.
 */
async function handleSubmit(): Promise<void> {
	error.value = "";
	loading.value = true;
	try {
		await auth.loginAction(form);
		const redirect = (route.query.redirect as string) || "/lobby";
		router.push(redirect);
	} catch (e: unknown) {
		const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
		error.value = msg ?? "Identifiants incorrects. Veuillez réessayer.";
	} finally {
		loading.value = false;
	}
}
</script>

<template>
	<div class="auth-page">
		<div class="auth-card card">
			<div class="auth-header">
				<RouterLink to="/" class="back-link">← Accueil</RouterLink>
				<h1>Connexion</h1>
				<p class="text-muted">Bon retour parmi nous !</p>
			</div>

			<div v-if="error" class="alert alert-error mb-4">{{ error }}</div>

			<form @submit.prevent="handleSubmit" class="auth-form">
				<div class="form-group">
					<label class="form-label" for="email">Adresse e-mail</label>
					<input
						id="email"
						v-model="form.email"
						type="email"
						placeholder="john.doe@exemple.com"
						required
						autocomplete="email"
					/>
				</div>

				<div class="form-group">
					<label class="form-label" for="password">Mot de passe</label>
					<input
						id="password"
						v-model="form.password"
						type="password"
						placeholder="********"
						required
						autocomplete="current-password"
					/>
				</div>

				<button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading">
					<span v-if="loading" class="spinner"></span>
					<span v-else>Se connecter</span>
				</button>
			</form>

			<p class="auth-footer-text">
				Pas encore de compte ?
				<RouterLink to="/register" class="text-indigo font-medium">S'inscrire</RouterLink>
			</p>

			<div class="mock-hint">
				<strong>Comptes de démonstration</strong>
				<p class="mock-pass">Mot de passe commun : <code>Password1!</code></p>
				<ul class="mock-list">
					<li><code>bookmaker@betarena.local</code> - Bookmaker</li>
					<li><code>sacha@betarena.local</code> - Parieur</li>
					<li><code>timothy@betarena.local</code> - Parieur</li>
					<li><code>jeremy@betarena.local</code> - Parieur</li>
					<li><code>maxime@betarena.local</code> - Parieur</li>
				</ul>
				<p class="mock-disclaimer">
					Afficher les identifiants en clair, c'est vraiment pas ouf pour la sécurité tout ça... mais promis,
					c'est juste pour la démo !
				</p>
			</div>
		</div>
	</div>
</template>

<style scoped>
.auth-page {
	min-height: calc(100vh - var(--navbar-h));
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 2rem 1rem;
}

.auth-card {
	width: 100%;
	max-width: 420px;
}

.auth-header {
	margin-bottom: 2rem;
}

.back-link {
	font-size: 0.875rem;
	color: var(--text-3);
	display: inline-block;
	margin-bottom: 1rem;
	transition: color var(--t-base);
}
.back-link:hover {
	color: var(--text-2);
}

.auth-header h1 {
	font-size: 1.75rem;
	margin-bottom: 0.25rem;
}

.auth-form {
	display: flex;
	flex-direction: column;
	gap: 1.25rem;
	margin-bottom: 1.5rem;
}

.auth-footer-text {
	text-align: center;
	font-size: 0.9375rem;
	color: var(--text-2);
}

.mock-hint {
	margin-top: 1.25rem;
	padding: 0.875rem 1rem;
	background: rgba(252, 163, 17, 0.08);
	border: 1px solid rgba(252, 163, 17, 0.2);
	border-radius: var(--r-sm);
	font-size: 0.8125rem;
	color: var(--text-2);
	line-height: 1.5;
}

.mock-pass {
	margin: 0.375rem 0;
}

.mock-list {
	margin: 0;
	padding-left: 1.1rem;
	display: flex;
	flex-direction: column;
	gap: 0.125rem;
}

.mock-hint code {
	background: rgba(0, 0, 0, 0.2);
	padding: 0.05rem 0.3rem;
	border-radius: 4px;
	font-size: 0.78rem;
}

.mock-disclaimer {
	margin: 0.5rem 0 0;
	font-style: italic;
	color: var(--text-3);
}
</style>
