/// <reference types="vite/client" />

// Vue SFC shim
declare module "*.vue" {
	import type { DefineComponent } from "vue";
	const component: DefineComponent;
	export default component;
}

/**
 * Augment vue-router RouteMeta so meta fields are typed in guards
 */

import "vue-router";

declare module "vue-router" {
	interface RouteMeta {
		requiresAuth?: boolean;
		requiresBookmaker?: boolean;
		guestOnly?: boolean;
	}
}
