# Setup de l'environnement

---

## 1. Prérequis

### Outils à installer

| Outil              | Version minimale | Lien                                                                                      |
| ------------------ | ---------------- | ----------------------------------------------------------------------------------------- |
| **Docker Desktop** | 24+              | https://www.docker.com/products/docker-desktop                                            |
| **IntelliJ IDEA**  | 2023+            | https://www.jetbrains.com/idea                                                            |
| **Node.js**        | 20+              | https://nodejs.org                                                                        |
| **Git**            | 2.x              | https://git-scm.com                                                                       |
| **make**           | -                | Inclus sur Linux/macOS - sur Windows, installer via [Git Bash](https://gitforwindows.org) |

### Vérifier les installations

```bash
docker --version        # Docker version 24.x.x
docker compose version  # Docker Compose version v2.x.x
node --version          # v20.x.x
npm --version           # 10.x.x
git --version           # git version 2.x.x
make --version          # GNU Make 3.x / 4.x
```

---

## 2. Lancer l'environnement

### Services démarrés

| Service    | Conteneur           | Port local |
| ---------- | ------------------- | ---------- |
| Frontend   | `betarena_frontend` | 5173       |
| Backend    | `betarena_backend`  | 8080       |
| PostgreSQL | `betarena_db`       | 5432       |
| pgAdmin    | `betarena_pgadmin`  | 5050       |

### Démarrage

```bash
# Cloner le dépôt
git clone <url-du-dépôt>
cd <nom-du-dossier>

# Première fois uniquement : configurer .env et construire les images
make setup

# Démarrer tous les services
make up
```

### Vérifier que tous les conteneurs tournent - `make`

```bash
make logs
```

Le backend est prêt quand les logs affichent :

```
Started BetarenaApplication in X.XXX seconds
```

### Vérifier que tous les conteneurs tournent - `Docker`

```bash
docker ps -a
```

Résultat attendu :

```
CONTAINER ID   IMAGE                COMMAND                  CREATED          STATUS                    PORTS                                         NAMES
275f484719a3   betarena-frontend    "docker-entrypoint.s…"   27 seconds ago   Up 15 seconds             0.0.0.0:5173->5173/tcp, [::]:5173->5173/tcp   betarena_frontend
adaf2c6c94db   dpage/pgadmin4:latest "/entrypoint.sh"         27 seconds ago   Up 15 seconds             0.0.0.0:5050->80/tcp, [::]:5050->80/tcp       betarena_pgadmin
8d60b8506212   betarena-backend     "/__cacert_entrypoin…"   27 seconds ago   Up 15 seconds             0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp   betarena_backend
cd53fff32fca   postgres:18          "docker-entrypoint.s…"   27 seconds ago   Up 26 seconds (healthy)   0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   betarena_db
```

Si un conteneur a un problème, son statut apparaîtra comme `Restarting` :

```
CONTAINER ID   IMAGE                COMMAND                  CREATED              STATUS                          PORTS                                         NAMES
4908373c7c8c   betarena-frontend    "docker-entrypoint.s…"   About a minute ago   Restarting (1) 19 seconds ago                                                 betarena_frontend
7ce9a0252eed   dpage/pgadmin4:latest "/entrypoint.sh"         About a minute ago   Restarting (1) 22 seconds ago                                                 betarena_pgadmin
76434ca85c29   betarena-backend     "/__cacert_entrypoin…"   About a minute ago   Up 49 seconds                   0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp   betarena_backend
c88b6e016f75   postgres:18          "docker-entrypoint.s…"   About a minute ago   Up 59 seconds (healthy)         0.0.0.0:5432->5432/tcp, [::]:5432->5432/tcp   betarena_db
```

N'hésitez pas à faire un `docker logs <nom_du_conteneur>` (colonne `NAMES`) pour inspecter les traces en cas d'erreur.

> Vous pouvez également effectuer les mêmes vérifications via l'interface graphique de Docker Desktop, si vous préférez.

### Vérifier les migrations Flyway

```bash
make db
```

Dans psql, vérifier que les données de seed sont présentes :

```sql
SELECT * FROM users;
```

Pour quitter psql :

```
\q
```

---

## 3. Configuration IntelliJ - Hot Reload

Le hot reload fonctionne ainsi : IntelliJ compile les `.java` → écrit dans `target/classes/` → le volume Docker expose ce dossier au conteneur → Spring DevTools détecte les nouveaux `.class` et redémarre le contexte (~2 s).

**Trois réglages à activer dans IntelliJ :**

### 3.1 Déléguer la compilation à Maven

`Settings → Build, Execution, Deployment → Build Tools → Maven → Runner`

→ cocher **Delegate IDE build/run actions to Maven**

> Cette option n'est pas disponible dans toutes les configurations. Si elle est absente, passer directement au point 3.2.

### 3.2 Compilation automatique

`Settings → Build, Execution, Deployment → Compiler`

→ cocher **Build project automatically**

### 3.3 Compilation pendant l'exécution

`Settings → Advanced Settings`

→ cocher **Allow auto-make to start even if developed application is currently running**

### 3.4 Tester le hot reload

1. Lancer `make logs-back` dans un terminal
2. Modifier un fichier `.java` et sauvegarder
3. Dans les logs, observer le redémarrage automatique :

```
Restarting due to devtools update
Started BetarenaApplication in 2.1 seconds
```

---

## 4. Vérifications

| Service  | URL                       | Résultat attendu                                         |
| -------- | ------------------------- | -------------------------------------------------------- |
| Frontend | http://localhost:5173     | Page Vue                                                 |
| Backend  | http://localhost:8080/api | `404` - normal, aucune route `/api` n'est encore définie |
| pgAdmin  | http://localhost:5050     | Interface pgAdmin                                        |

---

## 5. Commandes utiles

```bash
make setup        # Init .env + build des images (première fois)
make up           # Démarrer tous les services
make down         # Arrêter tous les services
make restart      # Redémarrer tous les services
make logs         # Suivre tous les logs
make logs-back    # Logs Spring Boot uniquement
make logs-front   # Logs Vite uniquement
make db           # Ouvrir psql dans le conteneur PostgreSQL
make clean        # Supprimer les conteneurs (conserve les volumes)
make reset        # Reset complet - supprime conteneurs ET volumes ( /!\ perte des données)
```

---

## 6. Comptes de test

Créés automatiquement par `V2__seed_dev_data.sql` au premier démarrage.

**Mot de passe pour tous les comptes : `Password1!`**

| Username  | Email                  | Rôle      | Solde |
| --------- | ---------------------- | --------- | ----- |
| bookmaker | bookmaker@betarena.dev | BOOKMAKER | 0     |
| alice     | alice@betarena.dev     | BETTOR    | 1 000 |
| bob       | bob@betarena.dev       | BETTOR    | 1 500 |
| charlie   | charlie@betarena.dev   | BETTOR    | 750   |

---

## 7. Résolution de problèmes

### `error getting credentials - docker-credential-desktop`

```bash
cat > ~/.docker/config.json << 'EOF'
{}
EOF
```

### Le hot reload ne fonctionne pas

Vérifier les trois réglages IntelliJ décrits en [section 3](#3-configuration-intellij--hot-reload), en particulier **Delegate IDE build/run actions to Maven** qui est la cause la plus fréquente.

### Reset complet de la base de données

Si `V1__init_schema.sql` est modifié, Flyway rejettera le démarrage car le checksum ne correspond plus. Pour repartir de zéro :

```bash
make reset   # supprime le volume PostgreSQL
make up      # recrée tout - Flyway rejoue V1 + V2
```

### Autres problèmes

> Pour tout autre problème ou question technique, n'hésitez pas à ouvrir une *Issue* sur le dépôt.
