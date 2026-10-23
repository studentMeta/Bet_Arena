<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { getEvents } from "../api/events";
import EventCard from "../components/EventCard.vue";
import type { BetArenaEvent, EventStatus } from "../types";

/**
 * Dictionnaires pour l'affichage des filtres dans la barre latérale.
 */
const STATUSES: { value: EventStatus; label: string }[] = [
	{ value: "OPEN", label: "Ouvert" },
	{ value: "CLOSED", label: "Clôturé" },
	{ value: "RESOLVED", label: "Résolu" },
];

const CATEGORIES: { value: string; label: string }[] = [
	{ value: "SPORT", label: "Sport" },
	{ value: "POLITIQUE", label: "Politique" },
	{ value: "CULTURE", label: "Culture" },
	{ value: "INTERNE", label: "Interne" },
];

/**
 * Variables d'état pour le stockage des événements et la pagination.
 */
const events = ref<BetArenaEvent[]>([]);
const currentPage = ref(0);
const totalPages = ref(1);
const totalElements = ref(0);
const loading = ref(false);

/**
 * Variables d'état pour les filtres actuels de l'utilisateur.
 */
const selectedStatus = ref<EventStatus>("OPEN");
const selectedCategory = ref<string | null>(null);
const searchQuery = ref("");
const sortBy = ref<"closeAt" | "createdAt">("closeAt");

/**
 * Appel API principal : Récupère les événements depuis le serveur.
 * @param reset - Si true, vide la liste actuelle et repart à la page 0 (utilisé quand on change un filtre majeur).
 */
async function fetchEvents(reset = false): Promise<void> {
	if (reset) {
		currentPage.value = 0;
		events.value = [];
	}
	loading.value = true;
	try {
		const data = await getEvents({
			page: currentPage.value,
			size: 12,
			status: selectedStatus.value,
			category: selectedCategory.value ?? undefined,
		});
		events.value.push(...data.content);
		totalPages.value = data.totalPages;
		totalElements.value = data.totalElements;
	} finally {
		loading.value = false;
	}
}

/**
 * Propriété calculée hybride : Applique la recherche textuelle et le tri temporel
 * UNIQUEMENT sur les événements déjà chargés en local, sans refaire de requête serveur.
 */
const sortedEvents = computed<BetArenaEvent[]>(() => {
	const query = searchQuery.value.trim().toLowerCase();
	const arr = query ? events.value.filter((e) => e.title.toLowerCase().includes(query)) : [...events.value];
	if (sortBy.value === "closeAt")
		return arr.sort((a, b) => new Date(a.closeAt).getTime() - new Date(b.closeAt).getTime());
	if (sortBy.value === "createdAt")
		return arr.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
	return arr;
});

/**
 * Met à jour le filtre de statut et relance une recherche à zéro.
 */
function setStatus(s: EventStatus): void {
	selectedStatus.value = s;
	fetchEvents(true);
}

/**
 * Met à jour le filtre de catégorie et relance une recherche à zéro.
 */
function setCategory(c: string | null): void {
	selectedCategory.value = c;
	fetchEvents(true);
}

/**
 * Remet tous les filtres à leur état par défaut.
 */
function resetFilters(): void {
	selectedStatus.value = "OPEN";
	selectedCategory.value = null;
	searchQuery.value = "";
	fetchEvents(true);
}

/**
 * Passe à la page suivante et ajoute les nouveaux événements à la liste (pagination dynamique).
 */
function loadMore(): void {
	currentPage.value++;
	fetchEvents();
}

/**
 * Charge les données initiales dès l'affichage de la page.
 */
onMounted(() => fetchEvents());

</script>
<template>
	<div class="lobby-page">
		<!-- Sidebar -->
		<aside class="lobby-sidebar">
			<div class="sidebar-section">
				<div class="form-group">
					<input type="search" placeholder="Rechercher un événement…" v-model="searchQuery" />
				</div>
			</div>

			<div class="sidebar-section">
				<p class="sidebar-label">Statut</p>
				<div class="filter-group">
					<button
						v-for="s in STATUSES"
						:key="s.value"
						class="filter-btn"
						:class="{ active: selectedStatus === s.value }"
						@click="setStatus(s.value)"
					>
						{{ s.label }}
					</button>
				</div>
			</div>

			<div class="sidebar-section">
				<p class="sidebar-label">Catégorie</p>
				<div class="filter-group">
					<button
						class="filter-btn"
						:class="{ active: selectedCategory === null }"
						@click="setCategory(null)"
					>
						Tous
					</button>
					<button
						v-for="c in CATEGORIES"
						:key="c.value"
						class="filter-btn"
						:class="{ active: selectedCategory === c.value }"
						@click="setCategory(c.value)"
					>
						{{ c.label }}
					</button>
				</div>
			</div>

			<div class="sidebar-section">
				<p class="sidebar-label">Trier par</p>
				<select v-model="sortBy">
					<option value="closeAt">Clôture (plus proche)</option>
					<option value="createdAt">Récent (plus récent)</option>
				</select>
			</div>
		</aside>

		<!-- Main content -->
		<main class="lobby-content">
			<div class="lobby-header">
				<h1>Événements</h1>
				<span class="text-muted text-sm">
					{{ totalElements }} résultat{{ totalElements !== 1 ? "s" : "" }}
				</span>
			</div>

			<!-- Loading skeleton -->
			<div v-if="loading && events.length === 0" class="events-grid">
				<div v-for="n in 6" :key="n" class="skeleton-card"></div>
			</div>

			<!-- Empty state -->
			<div v-else-if="!loading && sortedEvents.length === 0" class="empty-state">
				<p class="text-muted">Aucun événement ne correspond à vos filtres.</p>
				<button class="btn btn-ghost btn-sm mt-4" @click="resetFilters">Réinitialiser les filtres</button>
			</div>

			<!-- Events grid -->
			<div v-else class="events-grid">
				<EventCard v-for="event in sortedEvents" :key="event.id" :event="event" />
			</div>

			<!-- Load more -->
			<div class="load-more-wrap" v-if="currentPage < totalPages - 1">
				<button class="btn btn-ghost" @click="loadMore" :disabled="loading">
					<span v-if="loading" class="spinner"></span>
					<span v-else>Charger plus</span>
				</button>
			</div>
		</main>
	</div>
</template>

<style scoped>
.lobby-page {
	display: grid;
	grid-template-columns: 14rem 1fr;
	gap: 1.5rem;
	max-width: var(--content-max);
	margin: 0 auto;
	padding: 2rem 1.5rem;
	min-height: calc(100vh - var(--navbar-h));
}

/* Sidebar */
.lobby-sidebar {
	position: sticky;
	top: calc(var(--navbar-h) + 1.5rem);
	align-self: start;
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}

.sidebar-section {
	padding: 0.875rem 0;
	border-bottom: 1px solid var(--border);
}

.sidebar-section:last-child {
	border-bottom: none;
}

.sidebar-label {
	font-size: 0.75rem;
	font-weight: 700;
	text-transform: uppercase;
	letter-spacing: 0.06em;
	color: var(--text-3);
	margin-bottom: 0.625rem;
}

.filter-group {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
}

.filter-btn {
	background: transparent;
	border: none;
	color: var(--text-2);
	font-size: 0.9rem;
	font-weight: 500;
	text-align: left;
	padding: 0.4375rem 0.75rem;
	border-radius: var(--r-sm);
	transition:
		background var(--t-base),
		color var(--t-base);
	cursor: pointer;
	width: 100%;
}

.filter-btn:hover {
	background: var(--bg-3);
	color: var(--text-1);
}
.filter-btn.active {
	background: var(--bg-3);
	color: var(--indigo);
	font-weight: 600;
}

/* Main content */
.lobby-content {
	min-width: 0;
}

.lobby-header {
	display: flex;
	align-items: baseline;
	gap: 1rem;
	margin-bottom: 1.5rem;
}

.lobby-header h1 {
	font-size: 1.5rem;
}

.events-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
	gap: 1rem;
}

/* Skeleton loader */
.skeleton-card {
	height: 200px;
	background: var(--bg-2);
	border: 1px solid var(--border);
	border-radius: var(--r-lg);
	animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
	0%,
	100% {
		opacity: 1;
	}
	50% {
		opacity: 0.5;
	}
}

.empty-state {
	text-align: center;
	padding: 4rem 2rem;
}

.load-more-wrap {
	display: flex;
	justify-content: center;
	padding: 2rem 0;
}
</style>
