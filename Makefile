.PHONY: help setup up down restart logs logs-back logs-front clean reset db

help:
	@echo ""
	@echo "  BetArena — Commandes disponibles"
	@echo "  ─────────────────────────────────────────────"
	@echo "  make setup       Initialise .env + build les images"
	@echo "  make up          Démarre tous les services"
	@echo "  make down        Arrête tous les services"
	@echo "  make restart     Redémarre tous les services"
	@echo "  make logs        Suit tous les logs en temps réel"
	@echo "  make logs-back   Logs du backend uniquement"
	@echo "  make logs-front  Logs du frontend uniquement"
	@echo "  make clean       Supprime les containers (garde les volumes)"
	@echo "  make reset       Reset complet — supprime containers ET volumes"
	@echo "  make db          Ouvre psql dans le container PostgreSQL"
	@echo ""

setup:
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo ""; \
		echo "  >>  .env généré avec succès !"; \
		echo ""; \
		echo "  ┌─────────────────────────────────────────────────────┐"; \
		echo "  │  Informations de connexion (valeurs par défaut)     │"; \
		echo "  ├─────────────────────────────────────────────────────┤"; \
		echo "  │  PostgreSQL                                         │"; \
		echo "  │    Host     :  localhost:5432                       │"; \
		echo "  │    Base     :  betarena                             │"; \
		echo "  │    User     :  betarena                             │"; \
		echo "  │    Password :  changeme                             │"; \
		echo "  ├─────────────────────────────────────────────────────┤"; \
		echo "  │  pgAdmin                                            │"; \
		echo "  │    Email    :  admin@betarena.dev                   │"; \
		echo "  │    Password :  changeme                             │"; \
		echo "  └─────────────────────────────────────────────────────┘"; \
		echo ""; \
		echo "  /!\ Ces mots de passe sont des valeurs par défaut."; \
		echo "  /!\ Modifiez le fichier .env si vous souhaitez les changer."; \
		echo "  /!\ Ne modifiez pas le fichier .env.example."; \
		echo ""; \
	else \
		echo "  >>  .env existe déjà."; \
	fi
	docker compose build

up:
	docker compose up -d
	@echo ""
	@echo "   >> Services démarrés"
	@echo "  ┌────────────────────────────────────────────┐"
	@echo "  │  Frontend  >>  http://localhost:5173       │"
	@echo "  │  Backend   >>  http://localhost:8080/api   │"
	@echo "  │  pgAdmin   >>  http://localhost:5050       │"
	@echo "  └────────────────────────────────────────────┘"

down:
	docker compose down

restart:
	docker compose restart

logs:
	docker compose logs -f

logs-back:
	docker compose logs -f backend

logs-front:
	docker compose logs -f frontend

clean:
	docker compose down --remove-orphans

reset:
	@echo "Suppression de toutes les données. Confirmer ? [y/N]" && read ans && [ $${ans:-N} = y ]
	docker compose down -v --remove-orphans

db:
	docker compose exec postgres psql -U $${POSTGRES_USER:-betarena} -d $${POSTGRES_DB:-betarena}