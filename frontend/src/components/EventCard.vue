<script setup lang="ts">
import { computed } from "vue";
import type { BetArenaEvent, EventStatus } from "../types";


/**
 * Définition des "props" (propriétés).
 * nécessite un objet de type `BetArenaEvent` pour pouvoir s'afficher correctement.
 */
const props = defineProps<{ event: BetArenaEvent }>();

/**
 * Dictionnaires de traduction pour l'affichage brut de l'API vers un texte lisible.
 */
const CATEGORY_LABELS: Record<string, string> = {
	SPORT: "Sport",
	TECHNOLOGY: "Tech",
	FINANCE: "Finance",
	SCIENCE: "Science",
	POLITIQUE: "Politique",
	POLITICS: "Politique",
	CULTURE: "Culture",
	INTERNE: "Interne",
	MISC: "Divers",
};

const STATUS_LABELS: Record<EventStatus, string> = {
	OPEN: "Ouvert",
	CLOSED: "Clôturé",
	RESOLVED: "Résolu",
};

/**
 * Propriétés calculées réactives.
 * Elles traduisent la catégorie et le statut en temps réel.
 * Le `?? props.event.category` sert de sécurité : si la clé n'existe pas dans le dictionnaire,
 * on affiche la valeur brute envoyée par l'API au lieu d'afficher "undefined".
 */
const categoryLabel = computed(() => CATEGORY_LABELS[props.event.category] ?? props.event.category);
const statusLabel = computed(() => STATUS_LABELS[props.event.status] ?? props.event.status);

// --- Fonctions de formatage -----------------------------------------------------------

/**
 * Formate le volume total de paris (BetCoins) pour un affichage compact (ex: 1.5M, 20k).
 */
function formatPool(amount: number): string {
	if (amount >= 1_000_000) return `${(amount / 1_000_000).toFixed(1)}M Bt`;
	if (amount >= 1000) return `${(amount / 1000).toFixed(1)}k Bt`;
	return `${amount} Bt`;
}

/**
 * Formate la date vers format (ex: 12 juin 2026).
 */
function formatDate(iso: string): string {
	return new Date(iso).toLocaleDateString("fr-BE", {
		day: "numeric",
		month: "short",
		year: "numeric",
	});
}
</script>

<template>
	<RouterLink :to="`/events/${event.id}`" class="event-card">
		<!-- Header: badges -->
		<div class="ec-header">
			<span class="badge" :class="`badge-${event.category}`">
				{{ categoryLabel }}
			</span>
			<span class="badge" :class="`badge-${event.status.toLowerCase()}`">
				{{ statusLabel }}
			</span>
		</div>

		<!-- Title -->
		<h3 class="ec-title">{{ event.title }}</h3>

		<!-- Outcomes -->
		<div class="ec-outcomes" v-if="event.outcomes?.length">
			<span v-for="o in event.outcomes" :key="o.id" class="outcome-chip">
				{{ o.label }} <strong>{{ o.odds.toFixed(2) }}</strong>
			</span>
		</div>

		<!-- Footer: meta info -->
		<div class="ec-meta">
			<span>{{ formatPool(event.totalVolume) }}</span>
			<span class="ec-date">Clôture {{ formatDate(event.closeAt) }}</span>
		</div>
	</RouterLink>
</template>

<style scoped>
.event-card {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
	background: var(--bg-2);
	border: 1px solid var(--border);
	border-radius: var(--r-lg);
	padding: 1.25rem;
	transition:
		border-color var(--t-base),
		transform var(--t-base),
		box-shadow var(--t-base);
	cursor: pointer;
	text-decoration: none;
}

.event-card:hover {
	border-color: var(--border-md);
	transform: translateY(-2px);
	box-shadow: var(--shadow);
}

.ec-header {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
}

.ec-title {
	font-size: 1rem;
	font-weight: 600;
	color: var(--text-1);
	line-height: 1.4;
	display: -webkit-box;
	line-clamp: 2;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.ec-outcomes {
	display: flex;
	gap: 0.5rem;
	flex-wrap: wrap;
}

.outcome-chip {
	font-size: 0.8125rem;
	background: var(--bg-3, #f0f0f0);
	padding: 0.25rem 0.5rem;
	border-radius: var(--r-sm);
	color: var(--text-2);
}

.ec-meta {
	display: flex;
	gap: 0.75rem;
	font-size: 0.8125rem;
	color: var(--text-3);
	flex-wrap: wrap;
	margin-top: auto;
}

.ec-date {
	margin-left: auto;
}
</style>
