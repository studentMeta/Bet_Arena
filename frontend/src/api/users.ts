import { api } from "./index";
import type { LeaderboardEntry, AdminStats } from "../types";

/**
 * Récupère le classement des meilleurs joueurs.
 * @param limit - Le nombre maximum de joueurs à récupérer.
 * @returns La liste des joueurs classés.
 */
export async function getLeaderboard(limit = 10): Promise<LeaderboardEntry[]> {
	const res = await api.get<LeaderboardEntry[]>("/users/leaderboard", { params: { limit } });
	return res.data;
}

/**
 * Récupère les statistiques pour le tableau de bord administrateur.
 * @returns Un objet contenant les métriques (définie contract API).
 */
export async function getAdminStats(): Promise<AdminStats> {
	const res = await api.get<AdminStats>("/admin/stats");
	return res.data;
}
