import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";

// Importation des styles globaux (variables CSS, système de grille, thèmes)
import "./assets/style.css";

/**
 * Initialisation de l'application.
 * On crée une nouvelle instance de Vue en lui passant le composant racine (App.vue).
 * À ce stade, l'application existe en mémoire, mais n'est pas encore affichée ni fonctionnelle.
 */
const app = createApp(App);

/**
 * Injection des plugins.
 * C'est ici qu'on donne des fonctionnalité à l'application avant de l'afficher.
 * - Pinia : Permet à tous tes composants d'utiliser `useAuthStore()` pour partager des variables.
 * - Router : Permet à tous tes composants d'utiliser `<RouterLink>` et `useRouter()` pour naviguer.
 * * ⚠️  toujours faire `.use()` AVANT de faire `.mount()`.
 */
app.use(createPinia());
app.use(router);

/**
 * Le Montage final (L'affichage).
 * Vue prend toute la logique, tout le HTML et tout le CSS générés par tes composants,
 * et les injecte physiquement dans le fichier brut `index.html` du projet,
 * exactement à l'endroit où se trouve la balise `<div id="app"></div>`.
 */
app.mount("#app");
