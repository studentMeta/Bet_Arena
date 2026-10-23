import axios from "axios";

/* ----------------------------------------------------------------------------
 * Axios instance
 * ------------------------------------------------------------------------- */

export const api = axios.create({
	baseURL: "http://localhost:8080/api",
	timeout: 10000,
});

/* ----------------------------------------------------------------------------
 * Attach JWT token automatically on every request
 * ------------------------------------------------------------------------- */

api.interceptors.request.use((config) => {
	const token = localStorage.getItem("token");
	if (token) {
		config.headers.Authorization = `Bearer ${token}`;
	}
	return config;
});

/* ----------------------------------------------------------------------------
 * On 401, clear token and redirect to login
 * ------------------------------------------------------------------------- */

api.interceptors.response.use(
	(res) => res,
	(err: unknown) => {
		if (axios.isAxiosError(err) && err.response?.status === 401) {
			localStorage.removeItem("token");
			window.location.href = "/login";
		}
		return Promise.reject(err);
	},
);
