import { api } from "./index";
import type { User, LoginCredentials, RegisterPayload, AuthResponse, RegisterResponse } from "../types";

/**
 * Authentifie l'utilisateur et sauvegarde le token d'accès en local.
 * @param credentials - Les identifiants de connexion.
 * @returns La réponse de l'API contenant le token.
 */
export async function login(credentials: LoginCredentials): Promise<AuthResponse> {
	const res = await api.post<AuthResponse>("/auth/login", credentials);
	localStorage.setItem("token", res.data.token);
	return res.data;
}

/**
 * Crée un nouveau compte utilisateur.
 * Attention le backend feras un login a la volée partant d'ici.
 * @param payload - Les informations requises pour l'inscription.
 * @returns La confirmation de l'inscription et le token.
 */
export async function register(payload: RegisterPayload): Promise<RegisterResponse> {
	const res = await api.post<RegisterResponse>("/auth/register", payload);
	localStorage.setItem("token", res.data.token);
	return res.data;
}

/**
 * Récupère les informations du profil de l'utilisateur actuellement connecté.
 * @returns L'objet User contenant les données du profil.
 */
export async function getMe(): Promise<User> {
	const res = await api.get<User>("/auth/me");
	return res.data;
}
