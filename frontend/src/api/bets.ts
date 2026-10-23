import { api } from "./index";
import type { Bet, BetStatus, BetPayload, PaginatedResponse } from "../types";

/**
 * Paramètres de filtrage et de pagination pour l'historique des paris.
 */
export interface GetMyBetsParams {
	// état actuel du pari
	status?: BetStatus;
	// Numéro de la page
	page?: number;
	// Nombre de paris affichés par page
	size?: number;
}

/**
 * Enregistre un nouveau pari.
 * @param payload - Les détails du pari.
 * @returns Le pari validé et créé par le serveur.
 */
export async function placeBet(payload: BetPayload): Promise<Bet> {
	const res = await api.post<Bet>("/bets", payload);
	return res.data;
}

/**
 * Récupère la liste paginée des paris de l'utilisateur actuellement connecté.
 * @param params - Les critères de filtrage (statut) et de pagination.
 * @returns Les paris de l'utilisateur sous forme de réponse paginée.
 */
export async function getMyBets(params: GetMyBetsParams = {}): Promise<PaginatedResponse<Bet>> {
	const res = await api.get<PaginatedResponse<Bet>>("/bets/me", { params });
	return res.data;
}
