<script setup lang="ts">
import { reactive, ref, computed } from "vue";
import { useAuthStore } from "../stores/auth";
import { useRouter } from "vue-router";

/**
 * Initialisation des varaibles globales.
 */
const auth = useAuthStore();
const router = useRouter();

/**
 * État réactif du formulaire d'inscription.
 * Regroupe toutes les saisies utilisateur dans un seul objet logique.
 */
const form = reactive({ username: "", email: "", password: "", confirm: "" });
const error = ref("");
const loading = ref(false);

/**
 * Propriété calculée pour la validation en temps réel.
 * Vérifie si le champ "confirm" a été rempli ET s'il est différent du mot de passe.
 * @returns true si les mots de passe ne correspondent pas, false sinon.
 */
const passwordMismatch = computed(() => form.confirm.length > 0 && form.password !== form.confirm);

/**
 * Traite la soumission du formulaire d'inscription.
 */
async function handleSubmit(): Promise<void> {
	if (passwordMismatch.value) return;
	error.value = "";
	loading.value = true;
	try {
		await auth.registerAction({
			username: form.username,
			email: form.email,
			password: form.password,
		});
		router.push({ name: "lobby" });
	} catch (e: unknown) {
		const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
		error.value = msg ?? "L'inscription a échoué. Veuillez réessayer.";
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
				<h1>Créer un compte</h1>
				<p class="text-muted">Recevez <strong class="text-gold">1 000 BetCoins</strong> à l'inscription !</p>
			</div>

			<div v-if="error" class="alert alert-error mb-4">{{ error }}</div>

			<form @submit.prevent="handleSubmit" class="auth-form">
				<div class="form-group">
					<label class="form-label" for="username">Nom d'utilisateur</label>
					<input
						id="username"
						v-model="form.username"
						type="text"
						placeholder="CoolBettor42"
						required
						minlength="3"
						maxlength="30"
						autocomplete="username"
					/>
				</div>

				<div class="form-group">
					<label class="form-label" for="email">Adresse e-mail</label>
					<input
						id="email"
						v-model="form.email"
						type="email"
						placeholder="vous@exemple.com"
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
						minlength="8"
						autocomplete="new-password"
					/>
					<span class="form-hint">8 caractères minimum</span>
				</div>

				<div class="form-group">
					<label class="form-label" for="confirm">Confirmer le mot de passe</label>
					<input
						id="confirm"
						v-model="form.confirm"
						type="password"
						placeholder="********"
						required
						autocomplete="new-password"
					/>
					<span v-if="passwordMismatch" class="form-error"> Les mots de passe ne correspondent pas. </span>
				</div>

				<button type="submit" class="btn btn-primary btn-block btn-lg" :disabled="loading || passwordMismatch">
					<span v-if="loading" class="spinner"></span>
					<span v-else>Créer mon compte</span>
				</button>
			</form>

			<p class="auth-footer-text">
				Déjà inscrit ?
				<RouterLink to="/login" class="text-indigo font-medium">Se connecter</RouterLink>
			</p>
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
</style>
