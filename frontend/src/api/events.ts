import { api } from "./index";
import type { BetArenaEvent, CreateEventPayload, PaginatedResponse, ResolveResult } from "../types";


/**
 * Paramètres optionnels pour le filtrage, le tri et la pagination des événements.
 */
export interface GetEventsParams {
	// Numéro de la page
	page?: number;
	// Nombre d'éléments par page
	size?: number;
	// status (Open, close, resolve)
	status?: string;
	// catégorie (politique, sport,...)
	category?: string;
	// Critére de tri
	sort?: string;
}

/**
 * Récupère liste d'événements pour l'affichage public.
 * * @param params - Les paramètres de pagination et de filtrage (par défaut: page 0, taille 15).
 * @returns Une promesse contenant la réponse paginée des événements.
 */
export async function getEvents(params: GetEventsParams = {}): Promise<PaginatedResponse<BetArenaEvent>> {
	const { page = 0, size = 15, status, category, sort } = params;
	const res = await api.get<PaginatedResponse<BetArenaEvent>>("/events", {
		params: { page, size, status, category, sort },
	});
	return res.data;
}

/**
 * Récupère une liste d'événements avec des droits d'administration.
 * * @param params - Les paramètres de pagination et de filtrage (par défaut: page 0, taille 20).
 * @returns événements (vue admin).
 */
export async function getAdminEvents(params: GetEventsParams = {}): Promise<PaginatedResponse<BetArenaEvent>> {
	const { page = 0, size = 20, status, category } = params;
	const res = await api.get<PaginatedResponse<BetArenaEvent>>("/admin/events", {
		params: { page, size, status, category },
	});
	return res.data;
}

/**
 * Récupère les détails d'un événement spécifique via son identifiant.
 * * @param id - L'identifiant unique de l'événement (nombre ou chaîne).
 * @returns l'événement, ou null s'il n'est pas trouvé.
 */
export async function getEvent(id: number | string): Promise<BetArenaEvent | null> {
	const res = await api.get<BetArenaEvent>(`/events/${id}`);
	return res.data;
}

/**
 * Crée un nouvel événement dans le système.
 * * @param payload - Les données nécessaires à la création de l'événement.
 * @returns événement fraîchement créé.
 */
export async function createEvent(payload: CreateEventPayload): Promise<BetArenaEvent> {
	const res = await api.post<BetArenaEvent>("/events", payload);
	return res.data;
}

/**
 * Ferme un événement (ex: bloque les nouveaux paris/inscriptions).
 * * @param id - L'identifiant de l'événement à fermer.
 * @returns événement mis à jour.
 */
export async function closeEvent(id: number): Promise<BetArenaEvent> {
	const res = await api.patch<BetArenaEvent>(`/events/${id}/close`);
	return res.data;
}

/**
 * Résout un événement en déclarant un résultat gagnant.
 * * @param id - L'identifiant de l'événement à résoudre.
 * @param winningOutcomeId - L'identifiant du résultat déclaré gagnant.
 * @returns résultat de la résolution.
 */
export async function resolveEvent(id: number, winningOutcomeId: number): Promise<ResolveResult> {
	const res = await api.patch<ResolveResult>(`/events/${id}/resolve`, { winningOutcomeId });
	return res.data;
}
