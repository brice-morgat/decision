# Refonte UX/UI - Poker Decision (Spot Oriented)

## Notes de cadrage

- Les fichiers `README-PROJECT.md`, `README-DOMAIN.md`, `README-ARCHITECTURE.md`, `CONVENTIONS.md`, `ROADMAP.md` n'ont pas ete trouves dans ce repo.
- Cette proposition est alignee sur:
  - `docs/architecture.md`
  - `docs/conventions.md`
  - `docs/domain.md`
- Objectif respecte: conserver moteur backend, structure metier profils/ranges/regles/decision, sans logique solver ni stats GTO.

## 1) Diagnostic UX de l'existant

### Ce qui fonctionne

- Architecture front/back propre et separations de responsabilites respectees.
- Services Angular centralises et modeles types.
- Matrice 13x13 deja presente et reutilisable.
- Workflow metier complet disponible (profiles, ranges, rules, decision, history).

### Frictions UX majeures

- Edition des ranges centree sur des champs techniques (scenarioType, lineSignature, triggerActionCode) avant le besoin poker concret.
- Ranges hero/villain separentes, avec contexte a reconstruire mentalement par l'utilisateur.
- Villain trop "cell-by-cell" des l'entree, donc cout de maintenance eleve.
- Assistant decision encore proche du schema moteur au lieu d'un parcours poker guidé.
- Trop de decisions UI sont exposees au niveau "structure interne" au lieu du niveau "spot".

## 2) Principes de refonte

1. Spot-first:
- point d'entree principal = spot lisible poker (`BB vs BTN open`, `BTN open first in`, etc).

2. Progressive disclosure:
- mode standard simple;
- mode avance optionnel (edition fine cellule/ligne).

3. One screen, one intent:
- separer "Configurer un spot" et "Consulter/ajuster technique".

4. Reduction de charge cognitive:
- labels poker lisibles;
- presets contextuels;
- moins de champs "engine internals" visibles par defaut.

5. Compatibilite:
- conserver DTO metiers existants;
- ajouter des vues/facades orientees spot sans casser les endpoints core.

## 3) Nouvelle organisation des ecrans

## Navigation cible

- `Assistant` (central, action rapide)
- `Spots` (nouveau hub UX)
  - `Spots Hero`
  - `Spots Villain`
- `Regles` (avance)
- `Historique`
- `Profils`
- `Parametres`

## Ecrans cibles

1. `Spot Catalog`:
- liste de spots compréhensibles;
- filtres: profil, game type, street;
- actions: "Configurer Hero", "Configurer Villain", "Tester dans assistant".

2. `Spot Editor Hero`:
- contexte spot resume en haut;
- matrice 13x13 + palette d'actions rapides (`OPEN`, `CALL`, `3BET`, `FOLD`, ...);
- metadonnees secondaires repliees.

3. `Spot Editor Villain`:
- mode standard:
  - templates/presets par spot (`tight`, `standard`, `loose`);
  - sliders globaux (range width, agressivite);
  - override rapide par groupes.
- mode avance:
  - edition 13x13 actuelle conservee.

4. `Assistant Decision`:
- formulaire compact orienté spot:
  - profil
  - spot
  - positions
  - hero hand
  - action adverse + sizing
  - sequence simplifiee (optionnelle)
- carte resultat claire:
  - action
  - sizing
  - regle
  - message/statut

## 4) Nouvelles interactions pour editer les ranges

- Click-drag paint mode dans la matrice (pas seulement clic unitaire).
- Palette action rapide (appliquee a la selection):
  - hero: legendes strategiques
  - villain: tags/profils de frequence
- Selection par groupes:
  - paires, broadways, suited connectors, Ax suited, etc.
- Barre de resume live:
  - nb combos actifs, distribution par action/tag.
- Undo local edition avant sauvegarde.

## 5) Strategie specifique ranges villain

Mode Standard (par defaut):
- pas d'edition main par main obligatoire;
- creation d'une "range de spot" basee sur preset + curseurs.

Mode Avance:
- ouvre l'editeur detail existant (13x13 + poids/tag/note).

Stockage:
- toujours persiste dans `VillainRangeSet` + `VillainRangeCell`.
- Le mode standard genere les cellules automatiquement.

Compatibilite:
- aucune rupture schema necessaire pour debuter.
- possible extension future:
  - ajouter `villain_range_profiles` pour memoriser presets.

## 6) Strategie specifique selection de mains

- Conserver la matrice 13x13 mais moderniser les interactions:
  - hover preview,
  - drag selection,
  - shortcuts (`1`=OPEN, `2`=CALL, etc.),
  - chips de groupes predefinis.
- Ajouter un "Hand Quick Picker":
  - input typeahead (`AKs`, `QJo`) + selection instantanee.
- Clarifier visuellement:
  - couleur = UI uniquement;
  - badge/legend = donnee metier.

## 7) Impacts techniques frontend (Angular)

## A garder

- architecture modules/features actuelle.
- services API existants.
- composant `poker-hand-matrix` comme base.

## A ajouter

- `features/spots/`:
  - `pages/spot-catalog.component.*`
  - `pages/spot-editor-hero.component.*`
  - `pages/spot-editor-villain.component.*`
  - `services/spot-facade.service.ts`
- `shared/components/range-palette/`
- `shared/components/spot-selector/`
- `shared/components/hand-quick-picker/`

## A simplifier

- `features/ranges/pages/hero-ranges.component.*`:
  - extraire les champs techniques dans un panneau "Avance".
- `features/ranges/pages/villain-ranges.component.*`:
  - mode standard par presets/sliders.

## 8) Impacts techniques backend (minimaux)

Objectif: adaptations minimales, non disruptives.

1. Ajouter endpoint de mapping spot (optionnel mais recommande):
- `GET /spots/catalog?profileId=...`
- retourne label + contexte resolu (gameType/street/positions/scenarioType/...).

2. Ajouter endpoint de generation villain standard (optionnel):
- `POST /ranges/villain/from-template`
- input: spot + template + sliders
- output: `VillainRangeDetailDto` (cells generees)

3. Ne pas toucher au moteur de decision ni aux regles core.

4. Conserver formats DTO existants pour compatibilite des donnees.

## 9) Plan de migration progressif (sans casse)

Phase 1 - UI overlay:
- introduire `Spots` et parcours spot-first;
- conserver routes actuelles `ranges/hero`, `ranges/villain`, `rules`.

Phase 2 - villain standard:
- presets + sliders;
- fallback mode avance existant.

Phase 3 - assistant optimise:
- spot picker + formulaire reduit;
- mapping automatique vers DTO decision existant.

Phase 4 - deprecation douce:
- marquer les pages techniques actuelles "Mode avance";
- rediriger le flux principal vers `Spots`.

## 10) Proposition de patchs minimaux

## Frontend

1. Routing/navigation:
- modifier `front/src/app/layout/components/app-shell.component.ts`
  - ajouter entree `Spots`.
- modifier `front/src/app/app-routing.module.ts`
  - ajouter lazy route `features/spots`.

2. Nouveau module spots:
- ajouter:
  - `front/src/app/features/spots/spots.module.ts`
  - `front/src/app/features/spots/pages/spot-catalog.component.ts|html|scss`
  - `front/src/app/features/spots/services/spot-facade.service.ts`

3. Ranges pages:
- patch minimal sur:
  - `front/src/app/features/ranges/pages/hero-ranges.component.html`
  - `front/src/app/features/ranges/pages/villain-ranges.component.html`
  - deplacer champs techniques dans `mat-expansion-panel` "Avance".

4. Matrice:
- patch minimal sur:
  - `front/src/app/features/ranges/components/poker-hand-matrix.component.ts`
  - ajout drag selection + selection de groupes.

5. Assistant:
- patch minimal sur:
  - `front/src/app/features/decision-assistant/components/decision-context-form.component.*`
  - remplacer `scenarioType` explicite par `spot` (mapping facade vers scenario).

## Backend (optionnel phase 2)

- ajouter:
  - `back/src/main/java/.../controller/SpotController.java`
  - `back/src/main/java/.../dto/spot/SpotCatalogItemDto.java`
  - `back/src/main/java/.../service/SpotCatalogService.java`
- aucun changement obligatoire sur le moteur de decision.

## 11) Ce qu'on garde / simplifie / fusionne / deplace

Garder:
- moteur decision, regles, referentiels, DTO core ranges.

Simplifier:
- edition villain par defaut.
- creation range via spot au lieu de champs bruts.

Fusionner:
- entree hero/villain dans un hub `Spots`.

Deplacer:
- options techniques vers panneaux "Avance".

