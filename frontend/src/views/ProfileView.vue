<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { getMyBets } from "../api/bets";
import { useAuthStore } from "../stores/auth";
import type { Bet, BetStatus } from "../types";

/**
 * Accès au store Pinia.
 * Permet de récupérer les informations globales du joueur (pseudo, email, solde actuel).
 */
const auth = useAuthStore();

/**
 * Variables d'état local pour gérer l'historique des paris.
 */
const bets = ref<Bet[]>([]);
const loadingBets = ref(true);
const betFilter = ref<BetStatus | "ALL">("ALL");

/**
 * Constantes pour l'affichage de l'interface (onglets et libellés des statuts).
 */
const FILTERS: { value: BetStatus | "ALL"; label: string }[] = [
	{ value: "ALL", label: "Tous" },
	{ value: "PENDING", label: "En cours" },
	{ value: "WON", label: "Gagnés" },
	{ value: "LOST", label: "Perdus" },
];

const STATUS_LABELS: Record<BetStatus, string> = {
	PENDING: "En cours",
	WON: "Gagné",
	LOST: "Perdu",
};

/**
 * Chargement initial : On récupère tous les paris du joueur dès l'ouverture de la page.
 */
onMounted(async () => {
	const data = await getMyBets();
	bets.value = data.content;
	loadingBets.value = false;
});

/**
 * Tableau réactif filtré :
 * Au lieu de refaire un appel API quand on clique sur un onglet, on filtre directement
 * le tableau 'bets' localement en fonction de 'betFilter'.
 */
const filteredBets = computed<Bet[]>(() =>
	betFilter.value === "ALL" ? bets.value : bets.value.filter((b) => b.status === betFilter.value),
);

/**
 * Statistiques calculées à la volée.
 * - wonCount : Compte le nombre de paris ayant le statut "WON".
 * - totalWon : Additionne les gains de tous les paris gagnés à l'aide de la méthode reduce().
 */
const wonCount = computed(() => bets.value.filter((b) => b.status === "WON").length);
const totalWon = computed(() => bets.value.filter((b) => b.status === "WON").reduce((s, b) => s + b.potentialGain, 0));

// --- Fonctions de formatage --------------------------------------------------

function formatDate(iso: string | undefined): string {
	if (!iso) return "—";
	return new Date(iso).toLocaleDateString("fr-BE", {
		day: "numeric",
		month: "short",
		year: "numeric",
	});
}

function truncate(str: string, n: number): string {
	return str.length > n ? str.slice(0, n) + "..." : str;
}
</script>

<template>
	<div class="profile-page container">
		<!-- Header -->
		<div class="profile-header">
			<div class="avatar-lg">
				{{ auth.user?.username?.charAt(0).toUpperCase() }}
			</div>
			<div>
				<h1>{{ auth.user?.username }}</h1>
				<p class="text-muted">{{ auth.user?.email }}</p>
				<p class="text-dim text-sm">Parieur depuis {{ formatDate(auth.user?.createdAt) }}</p>
			</div>
		</div>

		<!-- Stats cards -->
		<div class="stats-row">
			<div class="stat-card card">
				<span class="sc-lbl">Solde actuel</span>
				<span class="sc-val text-gold">{{ auth.balance.toLocaleString("fr-BE") }} Bt</span>
			</div>
			<div class="stat-card card">
				<span class="sc-lbl">Paris placés</span>
				<span class="sc-val">{{ bets.length }}</span>
			</div>
			<div class="stat-card card">
				<span class="sc-lbl">Paris gagnés</span>
				<span class="sc-val text-success">{{ wonCount }}</span>
			</div>
			<div class="stat-card card">
				<span class="sc-lbl">Gains totaux</span>
				<span class="sc-val text-success">{{ totalWon.toLocaleString("fr-BE") }} Bt</span>
			</div>
		</div>

		<!-- Bet history -->
		<div class="section-block">
			<div class="section-head">
				<h2>Historique des paris</h2>
				<div class="filter-tabs">
					<button
						v-for="f in FILTERS"
						:key="f.value"
						class="tab-btn"
						:class="{ active: betFilter === f.value }"
						@click="betFilter = f.value"
					>
						{{ f.label }}
					</button>
				</div>
			</div>

			<div v-if="loadingBets" class="loading-center">
				<span class="spinner"></span>
			</div>

			<div v-else-if="filteredBets.length === 0" class="empty-state">
				<p class="text-muted">Aucun pari dans cette catégorie.</p>
			</div>

			<div v-else class="card">
				<table class="table">
					<thead>
						<tr>
							<th>Événement</th>
							<th>Issue</th>
							<th>Mise</th>
							<th>Gain potentiel</th>
							<th>Statut</th>
							<th>Date</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="bet in filteredBets" :key="bet.id">
							<td>
								<RouterLink :to="`/events/${bet.eventId}`" class="bet-event-link">
									{{ truncate(bet.eventTitle, 50) }}
								</RouterLink>
							</td>
							<td>
								<span class="outcome-pill">{{ bet.outcomeLabel }}</span>
							</td>
							<td class="font-medium">{{ bet.amount.toLocaleString("fr-BE") }} Bt</td>
							<td>
								<span v-if="bet.status === 'PENDING'" class="text-gold">
									{{ bet.potentialGain.toLocaleString("fr-BE") }} Bt
								</span>
								<span v-else-if="bet.status === 'WON'" class="text-success font-bold">
									+{{ bet.potentialGain.toLocaleString("fr-BE") }} Bt
								</span>
								<span v-else class="text-danger">— 0 Bt</span>
							</td>
							<td>
								<span class="status-pill" :class="`status-${bet.status.toLowerCase()}`">
									{{ STATUS_LABELS[bet.status] }}
								</span>
							</td>
							<td class="text-dim text-sm">{{ formatDate(bet.placedAt) }}</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</template>

<style scoped>
.profile-page {
	padding: 2.5rem 1.5rem 5rem;
	max-width: 1200px;
}

.profile-header {
	display: flex;
	align-items: center;
	gap: 1.5rem;
	margin-bottom: 2.5rem;
}

.avatar-lg {
	width: 5rem;
	height: 5rem;
	background: linear-gradient(135deg, var(--indigo-dark), var(--gold));
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 2rem;
	font-weight: 800;
	color: #fff;
	flex-shrink: 0;
}

.profile-header h1 {
	font-size: 1.75rem;
	margin-bottom: 0.25rem;
}

/* Stats */
.stats-row {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
	gap: 1rem;
	margin-bottom: 2.5rem;
}

.stat-card {
	display: flex;
	flex-direction: column;
	gap: 0.375rem;
	padding: 1.25rem;
}

.sc-lbl {
	font-size: 0.8125rem;
	color: var(--text-3);
	font-weight: 600;
	text-transform: uppercase;
	letter-spacing: 0.04em;
}

.sc-val {
	font-size: 1.625rem;
	font-weight: 800;
}

/* Section */

.section-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 1.25rem;
	flex-wrap: wrap;
	gap: 1rem;
}

.section-head h2 {
	font-size: 1.25rem;
}

.filter-tabs {
	display: flex;
	gap: 0.375rem;
	background: var(--bg-2);
	border: 1px solid var(--border);
	border-radius: var(--r-sm);
	padding: 0.25rem;
}

.tab-btn {
	padding: 0.3125rem 0.875rem;
	border-radius: calc(var(--r-sm) - 2px);
	border: none;
	background: transparent;
	color: var(--text-2);
	font-size: 0.875rem;
	font-weight: 500;
	cursor: pointer;
	transition:
		background var(--t-base),
		color var(--t-base);
}

.tab-btn.active {
	background: var(--bg-4);
	color: var(--text-1);
	font-weight: 600;
}

/* Table specifics */
.bet-event-link {
	color: var(--text-1);
	transition: color var(--t-base);
}
.bet-event-link:hover {
	color: var(--indigo);
}

.outcome-pill {
	display: inline-flex;
	align-items: center;
	padding: 0.125rem 0.625rem;
	border-radius: 99px;
	font-size: 0.8rem;
	font-weight: 700;
	background: rgba(99, 102, 241, 0.15);
	color: var(--indigo);
}

.status-pill {
	display: inline-flex;
	align-items: center;
	padding: 0.125rem 0.625rem;
	border-radius: 99px;
	font-size: 0.8rem;
	font-weight: 600;
}
.status-pending {
	background: rgba(56, 189, 248, 0.15);
	color: var(--info);
}
.status-won {
	background: rgba(34, 197, 94, 0.15);
	color: var(--success);
}
.status-lost {
	background: rgba(239, 68, 68, 0.15);
	color: var(--danger);
}

.empty-state {
	text-align: center;
	padding: 3rem;
}
</style>
