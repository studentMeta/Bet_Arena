<script setup lang="ts">
import { ref, computed, onMounted, watch } from "vue";
import { getAdminEvents, createEvent, closeEvent, resolveEvent } from "../api/events";
import { getAdminStats } from "../api/users";
import type { BetArenaEvent, EventStatus, AdminStats, CreateEventPayload, OutcomePayload } from "../types";

/**
 * États globaux de la page et de l'interface.
 */
const stats = ref<AdminStats | null>(null);
const events = ref<BetArenaEvent[]>([]);
const loadingEvents = ref(true);
const showForm = ref(false);
const formError = ref("");
const formLoading = ref(false);
const tableFilter = ref<EventStatus>("OPEN");
const actingOn = ref<number | null>(null);

/**
 * Dictionnaires et listes constantes pour l'UI (menus déroulants, badges).
 */
const CATEGORIES: { value: string; label: string }[] = [
	{ value: "SPORT", label: "Sport" },
	{ value: "POLITIQUE", label: "Politique" },
	{ value: "CULTURE", label: "Culture" },
	{ value: "INTERNE", label: "Interne" },
];

const STATUSES: { value: EventStatus; label: string }[] = [
	{ value: "OPEN", label: "Ouverts" },
	{ value: "CLOSED", label: "Clôturés" },
	{ value: "RESOLVED", label: "Résolus" },
];

const CATEGORY_LABELS: Record<string, string> = Object.fromEntries(CATEGORIES.map((c) => [c.value, c.label]));

const STATUS_LABELS: Record<EventStatus, string> = {
	OPEN: "Ouvert",
	CLOSED: "Clôturé",
	RESOLVED: "Résolu",
};


/**
 * Variables réactives liées aux champs du formulaire de création (v-model).
 */
const formTitle = ref("");
const formDescription = ref("");
const formCategory = ref("SPORT");
const formCloseAt = ref("");
const formOutcomes = ref<OutcomePayload[]>([
	{ label: "", odds: 1.5 },
	{ label: "", odds: 2.0 },
]);


/**
 * Calcule la date et l'heure actuelles au format ISO tronqué pour l'attribut "min" de l'input datetime.
 * Empêche de créer un événement dans le passé.
 */
const minDateTime = computed<string>(() => {
	const now = new Date();
	now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
	return now.toISOString().slice(0, 16);
});


/**
 * Charge la liste des événements en fonction du filtre actuel.
 */
async function loadEvents(): Promise<void> {
	loadingEvents.value = true;
	try {
		const data = await getAdminEvents({ status: tableFilter.value, size: 50 });
		events.value = data.content;
	} finally {
		loadingEvents.value = false;
	}
}

// Initialisation au montage du composant
onMounted(async () => {
	stats.value = await getAdminStats();
	await loadEvents();
});


/**
 * Écouteur réactif : relance le chargement des événements dès que l'onglet de filtre change.
 */
watch(tableFilter, loadEvents);

/**
 * Ajoute un nouveau choix au formulaire.
 */
function addOutcome(): void {
	formOutcomes.value.push({ label: "", odds: 2.0 });
}

/**
 * Supprime une issue du formulaire (en gardant toujours un minimum de 2 issues).
 * @param index - Position de l'issue dans le tableau.
 */
function removeOutcome(index: number): void {
	if (formOutcomes.value.length > 2) formOutcomes.value.splice(index, 1);
}


/**
 * Soumet le formulaire pour créer un nouvel événement via l'API.
 */
async function handleCreate(): Promise<void> {
	formError.value = "";
	formLoading.value = true;
	try {
		const payload: CreateEventPayload = {
			title: formTitle.value,
			description: formDescription.value || undefined,
			category: formCategory.value || undefined,
			closeAt: new Date(formCloseAt.value).toISOString(),
			outcomes: formOutcomes.value,
		};
		const newEvent = await createEvent(payload);
		if (tableFilter.value === "OPEN") events.value.unshift(newEvent);
		showForm.value = false;
		formTitle.value = "";
		formDescription.value = "";
		formCategory.value = "SPORT";
		formCloseAt.value = "";
		formOutcomes.value = [
			{ label: "", odds: 1.5 },
			{ label: "", odds: 2.0 },
		];
	} catch (e: unknown) {
		const msg = (e as { response?: { data?: { message?: string } } })?.response?.data?.message;
		formError.value = msg ?? "La création de l'événement a échoué.";
	} finally {
		formLoading.value = false;
	}
}

/**
 * Clôture un événement (ferme les paris).
 * @param id - Identifiant de l'événement.
 */
async function handleClose(id: number): Promise<void> {
	actingOn.value = id;
	try {
		await closeEvent(id);
		await loadEvents();
	} finally {
		actingOn.value = null;
	}
}

/**
 * Résout un événement en déclarant un gagnant et déclenche le paiement.
 * @param id - Identifiant de l'événement.
 * @param outcomeId - Identifiant de l'issue gagnante (depuis le `<select>`).
 */
async function handleResolve(id: number, outcomeId: string): Promise<void> {
	if (!outcomeId) return;
	actingOn.value = id;
	try {
		await resolveEvent(id, Number(outcomeId));
		await loadEvents();
	} finally {
		actingOn.value = null;
	}
}

// --- Fonctions utilitaires de formatage ------------------------------------

function formatPool(amount: number): string {
	if (amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(1)}M Bt`;
	if (amount >= 1000) return `${(amount / 1000).toFixed(1)}k Bt`;
	return `${amount} Bt`;
}

function formatDate(iso: string): string {
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
	<div class="admin-page container">
		<div class="admin-header">
			<h1>Console d'administration</h1>
			<p class="text-muted">Gestion des événements et supervision de la plateforme.</p>
		</div>

		<!-- Stats bar -->
		<div class="admin-stats" v-if="stats">
			<div class="astat card">
				<span class="astat-val">{{ stats.totalUsers.toLocaleString("fr-BE") }}</span>
				<span class="astat-lbl">Utilisateurs</span>
			</div>
			<div class="astat card">
				<span class="astat-val">{{ stats.openEvents }}</span>
				<span class="astat-lbl">Événements ouverts</span>
			</div>
			<div class="astat card">
				<span class="astat-val">{{ stats.totalBetsCount.toLocaleString("fr-BE") }}</span>
				<span class="astat-lbl">Paris placés</span>
			</div>
			<div class="astat card">
				<span class="astat-val text-gold">{{ formatPool(stats.totalBetsVolume) }}</span>
				<span class="astat-lbl">Volume total</span>
			</div>
			<div class="astat card">
				<span class="astat-val text-success">{{ formatPool(stats.totalDistributed) }}</span>
				<span class="astat-lbl">Gains distribués</span>
			</div>
		</div>

		<!-- Create event -->
		<div class="section-block">
			<div class="section-head">
				<h2>Événements</h2>
				<button class="btn btn-primary" @click="showForm = !showForm">
					{{ showForm ? "Annuler" : "+ Créer un événement" }}
				</button>
			</div>

			<Transition name="slide-down">
				<div v-show="showForm" class="create-form card">
					<h3 class="form-title">Nouvel événement</h3>

					<div v-if="formError" class="alert alert-error mb-4">{{ formError }}</div>

					<form @submit.prevent="handleCreate" class="create-form-fields">
						<div class="form-group">
							<label class="form-label">Titre *</label>
							<input
								v-model="formTitle"
								type="text"
								placeholder="Question à prédire…"
								required
								maxlength="200"
							/>
						</div>

						<div class="form-group">
							<label class="form-label">Description</label>
							<textarea
								v-model="formDescription"
								rows="3"
								placeholder="Contexte et règles de résolution…"
								maxlength="2000"
							/>
						</div>

						<div class="form-row">
							<div class="form-group">
								<label class="form-label">Catégorie</label>
								<select v-model="formCategory">
									<option v-for="c in CATEGORIES" :key="c.value" :value="c.value">
										{{ c.label }}
									</option>
								</select>
							</div>

							<div class="form-group">
								<label class="form-label">Clôture des paris *</label>
								<input v-model="formCloseAt" type="datetime-local" :min="minDateTime" required />
							</div>
						</div>

						<div class="form-group">
							<label class="form-label">Issues possibles (min. 2) *</label>
							<div class="outcomes-form-list">
								<div v-for="(o, i) in formOutcomes" :key="i" class="outcome-form-row">
									<input
										v-model="o.label"
										type="text"
										placeholder="Libellé (ex: Club Bruges)"
										required
									/>
									<input
										v-model.number="o.odds"
										type="number"
										step="0.01"
										min="1.01"
										placeholder="Cote"
										required
										class="odds-input"
									/>
									<button
										v-if="formOutcomes.length > 2"
										type="button"
										class="btn btn-sm btn-ghost"
										@click="removeOutcome(i)"
									>
										✕
									</button>
								</div>
							</div>
							<button type="button" class="btn btn-sm btn-ghost mt-2" @click="addOutcome">
								+ Ajouter une issue
							</button>
						</div>

						<div class="form-actions">
							<button type="submit" class="btn btn-primary" :disabled="formLoading">
								<span v-if="formLoading" class="spinner"></span>
								<span v-else>Publier l'événement</span>
							</button>
							<button type="button" class="btn btn-ghost" @click="showForm = false">Annuler</button>
						</div>
					</form>
				</div>
			</Transition>
		</div>

		<!-- Events table -->
		<div class="section-block">
			<div class="table-filters">
				<button
					v-for="s in STATUSES"
					:key="s.value"
					class="tab-btn"
					:class="{ active: tableFilter === s.value }"
					@click="tableFilter = s.value"
				>
					{{ s.label }}
				</button>
			</div>

			<div v-if="loadingEvents" class="loading-center">
				<span class="spinner"></span>
			</div>

			<div v-else class="card">
				<table class="table">
					<thead>
						<tr>
							<th>Événement</th>
							<th>Catégorie</th>
							<th>Volume</th>
							<th>Clôture</th>
							<th>Statut</th>
							<th>Actions</th>
						</tr>
					</thead>
					<tbody>
						<tr v-for="event in events" :key="event.id">
							<td>
								<RouterLink :to="`/events/${event.id}`" class="event-link">
									{{ truncate(event.title, 45) }}
								</RouterLink>
							</td>
							<td>
								<span class="badge" :class="`badge-${event.category}`">
									{{ CATEGORY_LABELS[event.category] ?? event.category }}
								</span>
							</td>
							<td class="text-muted">{{ formatPool(event.totalVolume) }}</td>
							<td class="text-dim text-sm">{{ formatDate(event.closeAt) }}</td>
							<td>
								<span class="badge" :class="`badge-${event.status.toLowerCase()}`">
									{{ STATUS_LABELS[event.status] }}
								</span>
							</td>
							<td>
								<div class="action-btns">
									<button
										v-if="event.status === 'OPEN'"
										class="btn btn-sm btn-danger"
										:disabled="actingOn === event.id"
										@click="handleClose(event.id)"
									>
										Clôturer
									</button>
									<template v-else-if="event.status === 'CLOSED'">
										<select
											@change="
												handleResolve(event.id, ($event.target as HTMLSelectElement).value)
											"
											:disabled="actingOn === event.id"
											class="resolve-select"
										>
											<option value="" disabled selected>Résoudre...</option>
											<option v-for="o in event.outcomes" :key="o.id" :value="String(o.id)">
												{{ o.label }}
											</option>
										</select>
									</template>
									<span v-else class="text-dim text-sm">—</span>
								</div>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</template>

<style scoped>
.admin-page {
	padding: 2.5rem 1.5rem 5rem;
}

.admin-header {
	margin-bottom: 2rem;
}
.admin-header h1 {
	font-size: 1.75rem;
	margin-bottom: 0.25rem;
}

/* Stats */
.admin-stats {
	display: grid;
	grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
	gap: 1rem;
	margin-bottom: 2.5rem;
}

.astat {
	display: flex;
	flex-direction: column;
	gap: 0.375rem;
	padding: 1.25rem;
}

.astat-val {
	font-size: 1.5rem;
	font-weight: 800;
}

.astat-lbl {
	font-size: 0.8125rem;
	color: var(--text-3);
	font-weight: 600;
	text-transform: uppercase;
	letter-spacing: 0.04em;
}

/* Section */
.section-block {
	margin-bottom: 2.5rem;
}

.section-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 1.25rem;
}

.section-head h2 {
	font-size: 1.25rem;
}

/* Create form */
.create-form {
	margin-bottom: 1.5rem;
}

.form-title {
	font-size: 1.125rem;
	margin-bottom: 1.5rem;
}

.create-form-fields {
	display: flex;
	flex-direction: column;
	gap: 1.25rem;
}

.form-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 1.25rem;
}

textarea {
	resize: vertical;
	min-height: 80px;
}

.outcomes-form-list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.outcome-form-row {
	display: flex;
	gap: 0.5rem;
	align-items: center;
}

.odds-input {
	width: 100px;
	flex-shrink: 0;
}

.form-actions {
	display: flex;
	gap: 0.75rem;
	padding-top: 0.5rem;
}

/* Table filters */
.table-filters {
	display: flex;
	gap: 0.375rem;
	background: var(--bg-2);
	border: 1px solid var(--border);
	border-radius: var(--r-sm);
	padding: 0.25rem;
	width: fit-content;
	margin-bottom: 1rem;
}

.tab-btn {
	padding: 0.375rem 1rem;
	border-radius: calc(var(--r-sm) - 2px);
	border: none;
	background: transparent;
	color: var(--text-2);
	font-size: 0.875rem;
	font-weight: 500;
	cursor: pointer;
	transition: all var(--t-base);
}

.tab-btn.active {
	background: var(--bg-4);
	color: var(--text-1);
	font-weight: 600;
}

.event-link {
	color: var(--text-1);
	transition: color var(--t-base);
}
.event-link:hover {
	color: var(--indigo);
}

.action-btns {
	display: flex;
	gap: 0.5rem;
}

/* Transition */
.slide-down-enter-active,
.slide-down-leave-active {
	transition: all 0.25s ease;
}
.slide-down-enter-from,
.slide-down-leave-to {
	opacity: 0;
	transform: translateY(-10px);
}
</style>
