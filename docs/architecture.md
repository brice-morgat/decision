# Architecture

## But

Ce document sert de garde-fou technique pour l'application desktop locale d'aide a la decision poker.
Il synthese les specifications presentes dans `.tmp-pdf/converted_text.txt` et `.tmp-pdf/converted_text_1.txt`.

## Vision d'ensemble

L'application repose sur une separation stricte entre :

- `front/` : application Angular desktop-first, responsable de la presentation, de la navigation, des formulaires reactifs et de l'appel aux APIs.
- `back/` : application Spring Boot, responsable de la logique metier, des validations metier, de la persistance locale et du moteur de decision.
- wrapper desktop Windows : Electron embarque l'application Angular et dialogue avec le backend local.

La logique poker complexe doit rester dans le backend.

## Couches metier cibles

Le systeme doit rester organise autour des couches suivantes :

1. referentiel poker
2. moteur de ranges
3. moteur de reconstruction d'etat
4. moteur de decision range-first
5. moteur de regles secondaire
6. moteur d'analyse et d'equity informative
7. historisation et analyse
8. orchestration applicative
9. presentation

Flux nominal :

1. le frontend selectionne une base hero, une main hero et eventuellement une base vilain
2. le backend reconstruit l'etat de jeu
3. le backend lit la cellule hero correspondante dans la range selectionnee
4. le backend ajuste la lecture hero contre la range vilain si elle est fournie
5. le backend calcule un bloc `analysis`
6. le backend calcule un bloc `equity` purement informatif si la range vilain est exploitable
7. le backend evalue les regles uniquement comme detail technique secondaire
8. le backend produit un `DecisionResult`
6. le frontend affiche la decision et ses explications

## Mapping technique cible

### Frontend Angular

- `core/` : config, services API, modeles partages, bridge desktop
- `shared/` : composants reutilisables, pipes, directives, utilitaires UI
- `layout/` : shell desktop, navigation, zones fixes de l'application
- `features/decision-assistant` : saisie de contexte et affichage du resultat
- `features/profiles` : profils strategiques
- `features/ranges` : ranges hero et villain
- `features/rules` : gestion des regles de decision
- `features/reviews` : historique et analyse
- `features/settings` : parametres applicatifs
- Angular Material : librairie UI de reference pour le poste desktop
- composant cle UI ranges : une matrice poker reutilisable 13x13, agnostique hero/villain
- composant cle UI rules : un editeur reactif dense, pilote par listes controlees, pour le contexte global, les conditions et les actions

Packaging desktop retenu :

- Angular pour l'UI
- Electron pour la distribution Windows en `.exe`
- backend Spring Boot local expose via HTTP local

Le frontend ne prend pas de decision metier. Il collecte, affiche et orchestre.

### Backend Spring Boot

Le backend doit converger vers des modules/cohesions proches de :

- `referential` : enums, referentiels, validations de codification
- `profile` : gestion des profils strategiques
- `range` : ranges hero, ranges villain, cellules, resolvers
- `situation` : action sequence, board state, decision input, decision context
- `decision/state-reconstruction` : derivation deterministic du `DecisionContext` a partir de `DecisionInput` et `ActionSequence`
- `rule` : regles, conditions, actions, priorites
- `decision/range-first` : `HeroRangeDecisionResolver`, `VillainRangeAdjustmentService`, `DecisionAnalysisAssembler`
- `decision/rule-engine` : `ConditionEvaluator`, `RuleMatcher`, `PriorityResolver`, `DecisionAssembler`, `RuleEngineFacade` pour enrichissement secondaire
- `equity` : `EquityAnalysisService`, `MonteCarloEquityCalculator`, evaluation de showdown et tirage Monte Carlo
- `decision` : orchestration du moteur
- `review` : historisation, analyses, feedback
- `settings` : parametres applicatifs

## Coeur du moteur de decision

Le moteur doit rester decoupe en sous-responsabilites explicites :

- `HeroRangeDecisionResolver` : lire la cellule hero et produire la lecture de base
- `VillainRangeAdjustmentService` : ajuster la lecture hero contre la base vilain selectionnee
- `DecisionAnalysisAssembler` : produire le bloc `analysis` du resultat
- `EquityAnalysisService` : produire un bloc `equity` informatif sans impact sur la decision
- `ContextInterpreter` : produire les indicateurs derives a partir du contexte et de la sequence d'actions
- `StateReconstructionService` : reconstruire les flags de contexte et la `lineSignature` a partir de la sequence d'actions
- `RuleMatcher` : filtrer les regles compatibles pour la trace technique
- `PriorityResolver` : resoudre les conflits de regles si la couche de trace est exploitee
- `DecisionAssembler` : construire la partie `matchedCandidates` / details techniques
- `DecisionEngineService` : orchestrer validation, reconstruction, resolution de ranges, analyse et enrichissements

## Contraintes d'architecture

- aucune logique metier complexe dans Angular
- aucune dependance du moteur a l'interface ou a des composants UI
- aucune decision basee sur une couleur
- aucun texte libre si un referentiel ou un type fort existe
- la decision nominale doit venir d'abord de la range hero, puis de l'opposition hero vs villain
- le calcul d'equity ne doit jamais modifier l'action recommandee
- en cas d'absence de decision, le backend renvoie un statut explicite (`NO_MATCH`, `INCOMPLETE_CONFIGURATION`, `CONFLICTING_RULES`, `INVALID_INPUT`)
- le `DecisionResult` doit exposer la regle retenue et un resume des candidates evaluees

## Evolutivite attendue

Le produit doit pouvoir evoluer sans reecriture massive vers :

- ranges villain filtrees et ponderees
- scenarios plus fins preflop et postflop
- enrichissement du `DecisionContext`
- traces de decision exploitables
- analyses et feedback empirique sur l'historique
