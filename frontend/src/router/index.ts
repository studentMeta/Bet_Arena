import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { useAuthStore } from "../stores/auth";

/**
 * Lazy-loaded views to optimize the initial bundle size.
 * Components are fetched only when the route is visited.
 */
const HomeView = () => import("../views/HomeView.vue");
const LoginView = () => import("../views/LoginView.vue");
const RegisterView = () => import("../views/RegisterView.vue");
const LobbyView = () => import("../views/LobbyView.vue");
const EventDetailView = () => import("../views/EventDetailView.vue");
const ProfileView = () => import("../views/ProfileView.vue");
const AdminView = () => import("../views/AdminView.vue");

/**
 * Application routes configuration.
 * Uses meta tags to handle access permissions (auth needed, role required, ...).
 */
const routes: RouteRecordRaw[] = [
	{
		path: "/",
		name: "home",
		component: HomeView,
	},
	{
		path: "/login",
		name: "login",
		component: LoginView,
		meta: { guestOnly: true },
	},
	{
		path: "/register",
		name: "register",
		component: RegisterView,
		meta: { guestOnly: true },
	},
	{
		path: "/lobby",
		name: "lobby",
		component: LobbyView,
		meta: { requiresAuth: true },
	},
	{
		path: "/events/:id",
		name: "event-detail",
		component: EventDetailView,
		meta: { requiresAuth: true },
	},
	{
		path: "/profile",
		name: "profile",
		component: ProfileView,
		meta: { requiresAuth: true },
	},
	{
		path: "/admin",
		name: "admin",
		component: AdminView,
		meta: { requiresAuth: true, requiresBookmaker: true },
	},
	{
		path: "/:pathMatch(.*)*",
		redirect: "/",
	},
];

/**
 * Vue Router instance creation.
 */
const router = createRouter({
	history: createWebHistory(),
	routes: routes,
	scrollBehavior: () => ({ top: 0 }),
});

/**
 * Global navigation guard.
 * Checks route meta tags against the user's authentication state and role
 * before letting them access the requested view.
 */
router.beforeEach((to) => {
	const auth = useAuthStore();

	// Redirect unauthenticated users trying to access protected routes to login
	if (to.meta.requiresAuth && !auth.isAuthenticated) {
		return { name: "login", query: { redirect: to.fullPath } };
	}

	// Redirect non-bookmaker users trying to access admin routes to home
	if (to.meta.requiresBookmaker && !auth.isBookmaker) {
		return { name: "home" };
	}

	// Redirect authenticated users trying to access guest pages (login/register) to lobby
	if (to.meta.guestOnly && auth.isAuthenticated) {
		return { name: "lobby" };
	}
});

export default router;
