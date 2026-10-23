<script setup lang="ts">
import { ref, reactive, computed, watch } from "vue";
import { useRoute } from "vue-router";
import { getEvent } from "../api/events";
import { placeBet } from "../api/bets";
import { useAuthStore } from "../stores/auth";
import type { BetArenaEvent, EventStatus } from "../types";

/**
 * Utilitaires globaux.
 * - route : Permet de lire l'URL actuelle (ex: pour récupérer l'ID /events/123).
 * - auth : Accès au store Pinia pour lire le solde et vérifier le statut du joueur.
 */
const route = useRoute();
const auth = useAuthStore();

/**
 * États de la page et de l'interface de pari.
 */
const event = ref<BetArenaEvent | null>(null);
const loading = ref(true);
const betError = ref("");
const betSuccess = ref(false);
const betLoading = ref(false);

/**
 * État du formulaire de pari.
 * Utilisation de `reactive` car c'est un objet regroupant plusieurs champs liés.
 */
const betForm = reactive<{ outcomeId: number | null; amount: number }>({
	outcomeId: null,
	amount: 100,
});

/**
 * Dictionnaires de traduction pour l'interface.
 */
const CATEGORY_LABELS: Record<string, string> = {
	SPORT: "Sport",
	POLITIQUE: "Politique",
	CULTURE: "Culture",
	INTERNE: "Interne",
};
const STATUS_LABELS: Record<EventStatus, string> = {
	OPEN: "Ouvert",
	CLOSED: "Clôturé",
	RESOLVED: "Résolu",
};

/**
 * Écoute les changements de l'ID dans l'URL.
 * Dès que l'ID change, on va chercher les données de l'événement correspondant via l'API.
 */
watch(
	() => route.params.id,
	async (id) => {
		loading.value = true;
		betSuccess.value = false;
		betError.value = "";
		event.value = await getEvent(id as string);
		if (event.value?.outcomes?.length) {
			betForm.outcomeId = event.value.outcomes[0].id;
		}
		loading.value = false;
	},
	{ immediate: true },
);

/**
 * Propriétés calculées pour le ticket de pari.
 * - selectedOutcome : Retrouve l'objet complet de l'issue sélectionnée pour lire sa cote.
 * - potentialWin : Calcule dynamiquement le gain en multipliant la mise par la cote.
 */
const selectedOutcome = computed(() => event.value?.outcomes?.find((o) => o.id === betForm.outcomeId));

const potentialWin = computed<number>(() => {
	if (!selectedOutcome.value || !betForm.amount) return 0;
	return Math.round(betForm.amount * selectedOutcome.value.odds);
});

/**
 * Soumet le pari à l'API.
 */
async function submitBet(): Promise<void> {
	if (!event.value || !betForm.outcomeId) return;
	betError.value = "";
	betLoading.value = true;
	try {
		const bet = await placeBet({ outcomeId: betForm.outcomeId, amount: betForm.amount });
		if (bet.newBalance !== undefined) auth.updateBalance(bet.newBalance);
		betSuccess.value = true;
	} catch (e: unknown) {
		const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
		betError.value = msg ?? "Le pari a échoué. Veuillez réessayer.";
	} finally {
		betLoading.value = false;
	}
}

/**
 * Formate la date / heure.
 */
function formatDate(iso: string): string {
	return new Date(iso).toLocaleDateString("fr-BE", {
		day: "numeric",
		month: "long",
		year: "numeric",
		hour: "2-digit",
		minute: "2-digit",
	});
}
</script>

<template>
	<div class="event-detail-page container">
		<!-- Loading -->
		<div v-if="loading" class="loading-center">
			<span class="spinner"></span>
		</div>

		<!-- Not found -->
		<div v-else-if="!event" class="empty-state">
			<h2>Événement introuvable</h2>
			<RouterLink to="/lobby" class="btn btn-ghost mt-4">← Retour au lobby</RouterLink>
		</div>

		<!-- Event content -->
		<template v-else>
			<RouterLink to="/lobby" class="back-link">← Retour au lobby</RouterLink>

			<div class="event-layout">
				<!-- Left: event info -->
				<div class="event-main">
					<!-- Header badges -->
					<div class="event-badges">
						<span class="badge" :class="`badge-${event.category}`">
							{{ CATEGORY_LABELS[event.category] ?? event.category }}
						</span>
						<span class="badge" :class="`badge-${event.status.toLowerCase()}`">
							{{ STATUS_LABELS[event.status] }}
						</span>
					</div>

					<h1 class="event-title">{{ event.title }}</h1>

					<p class="event-description">{{ event.description }}</p>

					<!-- Outcomes list -->
					<div class="event-outcomes" v-if="event.outcomes?.length">
						<h3 class="outcomes-title">Issues possibles</h3>
						<div class="outcomes-grid">
							<div v-for="o in event.outcomes" :key="o.id" class="outcome-card">
								<span class="outcome-label">{{ o.label }}</span>
								<span class="outcome-odds">{{ o.odds.toFixed(2) }}x</span>
								<span class="outcome-bets">{{ o.totalBets.toLocaleString("fr-BE") }} Bt misés</span>
							</div>
						</div>
					</div>

					<!-- Stats row -->
					<div class="event-stats">
						<div class="estat">
							<span class="estat-val text-gold">{{ event.totalVolume.toLocaleString("fr-BE") }} Bt</span>
							<span class="estat-lbl">Volume total</span>
						</div>
						<div class="estat">
							<span class="estat-val">{{ formatDate(event.closeAt) }}</span>
							<span class="estat-lbl">Clôture des paris</span>
						</div>
						<div class="estat">
							<span class="estat-val">{{ event.outcomes?.length ?? 0 }}</span>
							<span class="estat-lbl">Issues</span>
						</div>
					</div>

					<!-- Resolved outcome banner -->
					<div v-if="event.status === 'RESOLVED'" class="resolved-banner resolved-yes">
						<span>Événement résolu</span>
					</div>
				</div>

				<!-- Right: bet slip -->
				<aside class="bet-slip card">
					<!-- Already resolved -->
					<div v-if="event.status === 'RESOLVED'" class="bet-slip-closed">
						<p class="text-muted text-sm">Cet événement est résolu — les paris sont fermés.</p>
					</div>

					<!-- Closed -->
					<div v-else-if="event.status === 'CLOSED'" class="bet-slip-closed">
						<p class="text-muted text-sm">Les paris sont clôturés en attente de résolution.</p>
					</div>

					<!-- Active bet slip -->
					<template v-else>
						<h2 class="bet-slip-title">Placer un pari</h2>

						<!-- Outcome selection -->
						<div class="outcome-toggle">
							<button
								v-for="o in event.outcomes"
								:key="o.id"
								class="outcome-btn"
								:class="{ active: betForm.outcomeId === o.id }"
								@click="betForm.outcomeId = o.id"
							>
								{{ o.label }} — {{ o.odds.toFixed(2) }}x
							</button>
						</div>

						<!-- Amount input -->
						<div class="form-group mt-4">
							<label class="form-label">Montant (Bt)</label>
							<input
								v-model.number="betForm.amount"
								type="number"
								:min="10"
								:max="auth.balance"
								step="10"
								placeholder="100"
							/>
							<span class="form-hint"
								>Solde disponible : {{ auth.balance.toLocaleString("fr-BE") }} Bt</span
							>
						</div>

						<!-- Quick amount buttons -->
						<div class="quick-amounts">
							<button
								v-for="q in [100, 250, 500, 1000]"
								:key="q"
								class="quick-btn"
								:disabled="q > auth.balance"
								@click="betForm.amount = q"
							>
								{{ q }}
							</button>
							<button class="quick-btn" @click="betForm.amount = auth.balance">Max</button>
						</div>

						<!-- Potential win -->
						<div class="potential-win">
							<span class="text-muted text-sm">Gain potentiel</span>
							<span class="pw-value text-success">{{ potentialWin.toLocaleString("fr-BE") }} Bt</span>
						</div>

						<div v-if="betError" class="alert alert-error mt-4">{{ betError }}</div>

						<div v-if="betSuccess" class="alert alert-success mt-4">
							Pari placé avec succès ! Bonne chance !
						</div>

						<button
							v-if="!betSuccess"
							class="btn btn-primary btn-block btn-lg mt-4"
							:disabled="
								betLoading ||
								betForm.amount <= 0 ||
								betForm.amount > auth.balance ||
								!auth.isBettor ||
								!betForm.outcomeId
							"
							@click="submitBet"
						>
							<span v-if="betLoading" class="spinner"></span>
							<span v-else-if="!auth.isBettor">Réservé aux parieurs</span>
							<span v-else>Confirmer le pari</span>
						</button>
					</template>
				</aside>
			</div>
		</template>
	</div>
</template>

<style scoped>
.event-detail-page {
	padding: 2rem 1.5rem 4rem;
}

.back-link {
	display: inline-block;
	color: var(--text-3);
	font-size: 0.9rem;
	margin-bottom: 1.5rem;
	transition: color var(--t-base);
}
.back-link:hover {
	color: var(--text-2);
}

.event-layout {
	display: grid;
	grid-template-columns: 1fr 360px;
	gap: 2rem;
	align-items: start;
}

/* Event info */
.event-badges {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
	margin-bottom: 1rem;
}

.event-title {
	font-size: 1.75rem;
	font-weight: 800;
	line-height: 1.3;
	margin-bottom: 1rem;
}

.event-description {
	color: var(--text-2);
	line-height: 1.75;
	font-size: 1rem;
	margin-bottom: 1.5rem;
}

.event-outcomes {
	margin-bottom: 1.5rem;
}

.outcomes-title {
	font-size: 1rem;
	font-weight: 700;
	margin-bottom: 0.75rem;
}

.outcomes-grid {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
}

.outcome-card {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	padding: 0.5rem 0.75rem;
	background: var(--bg-2);
	border: 1px solid var(--border);
	border-radius: var(--r-sm);
	font-size: 0.9rem;
}

.outcome-card.outcome-winner {
	border-color: var(--success);
	background: rgba(34, 197, 94, 0.08);
}

.outcome-odds {
	font-weight: 700;
	color: var(--indigo, #6366f1);
}

.outcome-badge-win {
	font-size: 0.75rem;
	font-weight: 700;
	color: var(--success);
}

.event-stats {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 1rem;
	padding: 1.25rem;
	background: var(--bg-2);
	border: 1px solid var(--border);
	border-radius: var(--r-md);
}

.estat {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}
.estat-val {
	font-weight: 700;
	font-size: 1.125rem;
}
.estat-lbl {
	font-size: 0.8125rem;
	color: var(--text-3);
}

.resolved-banner {
	margin-top: 1.25rem;
	padding: 0.875rem 1.25rem;
	border-radius: var(--r-md);
	font-size: 1rem;
	display: flex;
	gap: 0.5rem;
	align-items: center;
}
.resolved-yes {
	background: rgba(34, 197, 94, 0.12);
	border: 1px solid rgba(34, 197, 94, 0.25);
	color: var(--success);
}
.resolved-no {
	background: rgba(239, 68, 68, 0.12);
	border: 1px solid rgba(239, 68, 68, 0.25);
	color: var(--danger);
}

/* Bet slip */
.bet-slip {
	position: sticky;
	top: calc(var(--navbar-h) + 1.5rem);
}

.bet-slip-title {
	font-size: 1.25rem;
	margin-bottom: 1.25rem;
}

.bet-slip-closed {
	text-align: center;
	padding: 1.5rem 0;
}

.outcome-toggle {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.outcome-btn {
	padding: 0.75rem;
	border-radius: var(--r-sm);
	font-weight: 700;
	font-size: 0.9rem;
	border: 2px solid var(--border-md);
	background: transparent;
	color: var(--text-2);
	cursor: pointer;
	transition: all var(--t-base);
}

.outcome-btn:hover {
	border-color: var(--border-lg);
	color: var(--text-1);
}

.outcome-btn.active {
	border-color: var(--indigo, #6366f1);
	background: rgba(99, 102, 241, 0.1);
	color: var(--indigo, #6366f1);
}

.quick-amounts {
	display: flex;
	gap: 0.375rem;
	flex-wrap: wrap;
	margin-top: 0.5rem;
}

.quick-btn {
	flex: 1;
	min-width: 50px;
	padding: 0.375rem 0.5rem;
	background: var(--bg-3);
	border: 1px solid var(--border-md);
	border-radius: var(--r-sm);
	color: var(--text-2);
	font-size: 0.8125rem;
	font-weight: 600;
	cursor: pointer;
	transition: all var(--t-base);
}

.quick-btn:hover:not(:disabled) {
	background: var(--bg-4);
	color: var(--text-1);
}

.quick-btn:disabled {
	opacity: 0.4;
	cursor: not-allowed;
}

.potential-win {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 1rem;
	padding: 0.875rem 1rem;
	background: var(--bg-3);
	border-radius: var(--r-sm);
}

.pw-value {
	font-size: 1.375rem;
	font-weight: 800;
}

.empty-state {
	text-align: center;
	padding: 6rem 2rem;
}
</style>
