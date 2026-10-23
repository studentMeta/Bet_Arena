<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getLeaderboard } from "../api/users";
import { useAuthStore } from "../stores/auth";
import type { LeaderboardEntry } from "../types";

/**
 * Initialisation du store d'authentification.
 * Permet de savoir globalement si l'utilisateur est connecté ou non.
 */
const auth = useAuthStore();

/**
 * Variables d'état réactives.
 * - leaders : Stocke le tableau des meilleurs joueurs. Vide par défaut.
 * - loading : Gère l'état de chargement (true au démarrage pour afficher le spinner).
 */
const leaders = ref<LeaderboardEntry[]>([]);
const loading = ref(true);

/**
 * Hook de cycle de vie : S'exécute automatiquement dès que le composant est inséré dans le DOM.
 * fait les appels API initiaux.
 */
onMounted(async () => {
	leaders.value = await getLeaderboard(8);
	loading.value = false;
});

/**
 * Retourne la classe CSS correspondante pour styliser le podium.
 * @param rank - La position du joueur (1, 2, ou 3).
 * @returns Le nom de la classe CSS associée, ou une chaîne vide si le joueur n'est pas sur le podium.
 */
function getRankClass(rank: number): string {
	if (rank === 1) return "rank-gold";
	if (rank === 2) return "rank-silver";
	if (rank === 3) return "rank-bronze";
	return "";
}
</script>

<template>
	<div class="home">
		<!-- Hero -->
		<section class="hero">
			<div class="hero-inner container">
				<div class="hero-badge">UNamur - Prediction Market virtuel</div>

				<h1 class="hero-title">
					Pariez sur l'avenir,<br />
					<span class="text-gold">sans risque financier.</span>
				</h1>

				<p class="hero-sub">
					BetArena est une plateforme de paris en monnaie virtuelle (BetCoins). Rejoignez des milliers de
					parieurs, créez vos propres événements et découvrez si votre vision du futur est meilleure que celle
					des autres.
				</p>

				<div class="hero-actions" v-if="!auth.isAuthenticated">
					<RouterLink to="/register" class="btn btn-gold btn-lg"> Commencer à parier </RouterLink>
					<RouterLink to="/login" class="btn btn-ghost btn-lg"> Se connecter </RouterLink>
				</div>

				<div class="hero-actions" v-else>
					<RouterLink to="/lobby" class="btn btn-primary btn-lg"> Voir les événements → </RouterLink>
				</div>

				<div class="hero-stats">
					<div class="hero-stat">
						<span class="stat-val">1 247</span>
						<span class="stat-lbl">Parieurs actifs</span>
					</div>
					<div class="hero-stat">
						<span class="stat-val">38</span>
						<span class="stat-lbl">Événements</span>
					</div>
					<div class="hero-stat">
						<span class="stat-val">2,84M Bt</span>
						<span class="stat-lbl">Volume total</span>
					</div>
				</div>
			</div>
		</section>

		<!-- How it works -->
		<section class="how-it-works container">
			<h2 class="section-title">Comment ça fonctionne ?</h2>
			<div class="steps-grid">
				<div class="step-card card">
					<div class="step-icon">1</div>
					<h3>Inscrivez-vous</h3>
					<p>
						Créez votre compte gratuitement et recevez <strong>1 000 BetCoins</strong> de départ pour
						commencer à parier.
					</p>
				</div>
				<div class="step-card card">
					<div class="step-icon">2</div>
					<h3>Choisissez un événement</h3>
					<p>
						Parcourez les événements ouverts - sport, politique, tech, sciences - et analysez les cotes en
						temps réel.
					</p>
				</div>
				<div class="step-card card">
					<div class="step-icon">3</div>
					<h3>Pariez et gagnez</h3>
					<p>
						Misez vos BetCoins sur l'issue que vous anticipez. Si vous avez raison, multipliez votre mise
						par la cote !
					</p>
				</div>
			</div>
		</section>

		<!-- Leaderboard -->
		<section class="leaderboard-section container">
			<h2 class="section-title">Classement des meilleurs parieurs</h2>

			<div class="card" v-if="loading">
				<div class="loading-center">
					<span class="spinner"></span>
				</div>
			</div>

			<div class="card" v-else>
				<table class="table">
					<thead>
						<tr>
							<th>#</th>
							<th>Joueur</th>
							<th>Solde</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="player in leaders" :key="player.rank"> <!--possible reverse avec <tr v-for="player in [...leaders].reverse()" :key="player.rank"> -->
							<td>
								<span class="rank-badge">
									{{ player.rank }}
								</span>
							</td>
							<td class="font-medium" :class="getRankClass(player.rank)">{{ player.username }}</td>
							<td class="text-gold font-bold">{{ player.balance.toLocaleString("fr-BE") }} Bt</td>
						</tr>
					</tbody>
				</table>
			</div>
		</section>
	</div>
</template>

<style scoped>
/* -- Hero -- */
.hero {
	background:
		radial-gradient(ellipse 80% 60% at 50% -10%, rgba(129, 140, 248, 0.15) 0%, transparent 70%), var(--bg-0);
	padding: 5rem 0 4rem;
	border-bottom: 1px solid var(--border);
}

.hero-inner {
	text-align: center;
	max-width: 720px;
	margin: 0 auto;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 1.5rem;
}

.hero-badge {
	display: inline-flex;
	align-items: center;
	padding: 0.375rem 1rem;
	background: rgba(129, 140, 248, 0.12);
	border: 1px solid rgba(129, 140, 248, 0.25);
	border-radius: 99px;
	font-size: 0.875rem;
	font-weight: 600;
	color: var(--indigo);
}

.hero-title {
	font-size: clamp(2rem, 5vw, 3.25rem);
	font-weight: 800;
	line-height: 1.15;
}

.hero-sub {
	font-size: 1.0625rem;
	color: var(--text-2);
	line-height: 1.7;
	max-width: 560px;
}

.hero-actions {
	display: flex;
	gap: 1rem;
	flex-wrap: wrap;
	justify-content: center;
}

.hero-stats {
	display: flex;
	gap: 3rem;
	margin-top: 1rem;
	padding-top: 2rem;
	border-top: 1px solid var(--border);
	width: 100%;
	justify-content: center;
	flex-wrap: wrap;
}

.hero-stat {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 0.25rem;
}

.stat-val {
	font-size: 1.625rem;
	font-weight: 800;
	color: var(--text-1);
}

.stat-lbl {
	font-size: 0.875rem;
	color: var(--text-3);
}

/* -- How it works -- */
.how-it-works {
	padding: 4rem 1.5rem;
}

.section-title {
	font-size: 1.75rem;
	font-weight: 800;
	margin-bottom: 2rem;
}

.steps-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
    gap: 1.25rem;
}

.step-card {
	text-align: center;
}

.step-icon {
	width: 3rem;
	height: 3rem;
	background: linear-gradient(135deg, var(--indigo-dark), var(--gold));
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 1.25rem;
	font-weight: 800;
	color: #fff;
	margin: 0 auto 1rem;
}

.step-card h3 {
	margin-bottom: 0.5rem;
	font-size: 1.0625rem;
}

.step-card p {
	color: var(--text-2);
	font-size: 0.9375rem;
	line-height: 1.6;
}

/* -- Leaderboard -- */
.leaderboard-section {
	padding: 0 1.5rem 5rem;
}

.rank-badge {
	font-size: 1.125rem;
}

.rank-gold {
	color: #ffd700;
}
.rank-silver {
	color: #c0c0c0;
}
.rank-bronze {
	color: #cd7f32;
}

.winrate-bar {
	display: flex;
	align-items: center;
	gap: 0.75rem;
}

.winrate-fill {
	height: 6px;
	background: var(--indigo);
	border-radius: 99px;
	min-width: 4px;
	flex-shrink: 0;
}

.winrate-label {
	font-size: 0.875rem;
	font-weight: 600;
	color: var(--text-2);
	white-space: nowrap;
}
</style>
