/* ============================================================================
 * Domain models
 * ========================================================================= */

export type BetStatus = "PENDING" | "WON" | "LOST";

export type EventStatus = "OPEN" | "CLOSED" | "RESOLVED";

export type EventCategory = string;

export interface OutcomeEntry {
	id: number;
	label: string;
	odds: number;
	totalBets: number;
}

export interface User {
	id: number;
	username: string;
	email?: string;
	role: "BETTOR" | "BOOKMAKER";
	balance: number;
	createdAt?: string;
}

export interface BetArenaEvent {
	id: number;
	title: string;
	description: string;
	category: string;
	closeAt: string;
	status: EventStatus;
	totalVolume: number;
	createdAt: string;
	resolvedAt?: string;
	outcomes: OutcomeEntry[];
}

export interface Bet {
	id: number;
	eventId: number;
	eventTitle: string;
	outcomeId: number;
	outcomeLabel: string;
	odds: number;
	amount: number;
	potentialGain: number;
	status: BetStatus;
	placedAt: string;
	newBalance?: number;
}

export interface LeaderboardEntry {
	rank: number;
	username: string;
	balance: number;
}

export interface ResolveResult {
	id: number;
	status: EventStatus;
	winningOutcome: { id: number; label: string; odds: number };
	totalDistributed: number;
	resolvedAt: string;
}

export interface AdminStats {
	totalUsers: number;
	totalEvents: number;
	openEvents: number;
	resolvedEvents: number;
	totalBetsVolume: number;
	totalBetsCount: number;
	totalDistributed: number;
}

/* ============================================================================
 * Payloads
 * ========================================================================= */

export interface LoginCredentials {
	email: string;
	password: string;
}

export interface RegisterPayload extends LoginCredentials {
	username: string;
}

export interface AuthResponse {
	token: string;
	user: User;
}

export interface RegisterResponse extends User {
	token: string;
}

export interface BetPayload {
	outcomeId: number;
	amount: number;
}

export interface OutcomePayload {
	label: string;
	odds: number;
}

export interface CreateEventPayload {
	title: string;
	description?: string;
	category?: string;
	closeAt: string;
	outcomes: OutcomePayload[];
}

/* ============================================================================
 * Pagination
 * ========================================================================= */

export interface PaginatedResponse<T> {
	content: T[];
	totalElements: number;
	totalPages: number;
	page: number;
	size: number;
}
