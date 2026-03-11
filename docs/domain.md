# Domain

## But

Ce document rappelle les objets metier centraux et les invariants a respecter dans le code.

## Blocs fonctionnels

Le domaine est structure en 7 blocs :

1. referentiel poker
2. profils strategiques
3. ranges hero
4. ranges villain
5. situations et sequences d'actions
6. moteur de regles et de decision
7. historisation et analyse

## Referentiels obligatoires

Le systeme doit privilegier des codes metier explicites et types pour :

- `GameType`
- `Street`
- `Position`
- `ActionType`
- `SizingType`
- `StrategyLegend`
- `ScenarioType`
- `RuleOperator`
- `DecisionStatus`
- `RuleConditionType`
- `ConditionValueType`

Exemples importants issus des specs :

- `GameType` : `CASH`, `TOURNAMENT`
- `Street` : `PREFLOP`, `FLOP`, `TURN`, `RIVER`
- `ActionType` : `FOLD`, `CHECK`, `CALL`, `BET`, `RAISE`, `OPEN`, `THREE_BET`, `FOUR_BET`, `SHOVE`, `LIMP`
- `DecisionStatus` : `SUCCESS`, `NO_MATCH`, `INCOMPLETE_CONFIGURATION`, `CONFLICTING_RULES`, `INVALID_INPUT`

## Entites metier principales

### StrategyProfile

Represente un ensemble coherent de strategie utilisateur.

Attributs attendus :

- `id`
- `name`
- `description`
- `gameTypeCode`
- `isActive`
- `isArchived`
- `versionLabel`
- `createdAt`
- `updatedAt`

Relations :

- un profil possede plusieurs `HeroRangeSet`
- un profil possede plusieurs `VillainRangeSet`
- un profil possede plusieurs `DecisionRule`
- un profil possede plusieurs `HandReview`

Regles de gestion retenues :

- un seul profil actif par `GameType`
- la duplication d'un profil recopie sa configuration strategique (`HeroRangeSet`, `VillainRangeSet`, `DecisionRule`)
- la duplication ne recopie pas l'historique `HandReview`

### HeroRangeSet

Definit une range hero dans un contexte donne.

Contexte minimal :

- `profileId`
- `gameTypeCode`
- `streetCode`
- `heroPositionCode`
- `scenarioTypeCode`

Structure :

- metadonnees du set
- liste de `HeroRangeCell`

Invariants supplementaires :

- unicite logique d'une range par contexte (`profileId`, `gameType`, `street`, `heroPosition`, `scenarioType`, `subScenarioCode`)
- `handCode` canonique limite au referentiel 169 mains (`AA`, `AKS`, `AKO`)

### HeroRangeCell

Represente une main annotee dans une range hero.

Attributs typiques :

- `handCode`
- `enabled`
- `legendCode`
- `colorCode`
- `note`
- `actionWeight`

Invariant critique :

- `colorCode` est purement visuel
- `legendCode` ou un code d'action strategique est la vraie donnee metier
- une main ne peut apparaitre qu'une seule fois dans une meme range

### VillainRangeSet

Represente une hypothese de range adverse dans un contexte donne.

Contexte typique :

- `profileId`
- `gameTypeCode`
- `streetCode`
- `villainPositionCode`
- `heroPositionCode`
- `scenarioTypeCode`
- `triggerActionCode`
- `lineSignature`

Le moteur villain enrichit la decision, mais ne decide pas a la place du moteur central.

### VillainRangeCell

Represente une main potentielle dans une range villain.

Attributs typiques :

- `handCode`
- `enabled`
- `weight`
- `tagCode`
- `note`

Invariants critiques :

- une main ne peut apparaitre qu'une seule fois dans une meme range villain
- `weight` reste optionnel et purement descriptif tant qu'aucun moteur probabiliste ne l'exploite

### ActionEvent / ActionSequence

Permet de representer l'historique ordonne des actions d'une main.

Exemple logique :

- `PREFLOP: BTN open 2.5bb`
- `PREFLOP: BB call`
- `FLOP: BB check`
- `FLOP: BTN bet 50% pot`
- `FLOP: BB raise 3x`

Invariants retenus :

- `orderIndex` est unique dans une sequence
- l'ordre de lecture est strictement celui de `orderIndex`
- `potSizeAfter >= potSizeBefore`
- `stackAfter <= stackBefore` pour l'acteur de l'evenement

### DecisionContext

Objet metier central du moteur.

Il doit a minima contenir :

- `profileId`
- `gameType`
- `currentStreet`
- `heroPosition`
- `villainPosition`
- `heroHand`
- `board`
- `actionSequence`
- `facingAction`
- `currentSizing`
- `initiative`
- `streetRaiseCount`
- flags derives
- range hero applicable
- label hero applicable
- range villain applicable
- scenarios derives

Exemples de flags derives :

- `heroWasPreflopAggressor`
- `villainWasPreflopAggressor`
- `villainCheckedToHero`
- `heroBetCurrentStreet`
- `villainRaisedHeroBet`
- `heroFacingSecondBarrel`
- `heroFacingThirdBarrel`
- `lineSignature`

### DecisionRule

Une regle doit rester lisible et deterministe.

Elle peut contenir :

- contexte de profil et de jeu
- street
- positions
- scenario
- main exacte
- label de range
- reference de range
- action adverse
- line derivee
- condition de sizing
- preconditions supplementaires
- une liste ordonnee de `RuleCondition`
- une liste ordonnee de `RuleAction`
- `enabled`
- `priority`
- `stopOnMatch`
- note

Regles importantes :

- une regle doit porter au moins une `RuleAction`
- `conditionOrder` doit etre unique dans la regle
- `executionOrder` doit etre unique dans la regle
- le catalogue `RuleConditionType` est ferme en v1
- les operateurs autorises dependent du `ConditionValueType`

### DecisionResult

Sortie explicite du moteur.

Doit contenir :

- action recommandee
- sizing recommande si disponible
- statut
- regle retenue
- resume des candidates matchées
- message lisible
- avertissements
- motif d'absence de decision si applicable

### HandReview

Supporte l'historisation et l'analyse future.

Objectifs a court terme :

- enregistrer la situation traitee
- enregistrer la decision recommandee
- enregistrer la decision jouee
- enregistrer le resultat obtenu

## Regles de domaine a ne pas casser

- le backend reste souverain sur la decision finale
- le frontend ne fait jamais d'inference metier complexe
- une situation de poker ne se deduit pas d'un simple trio main/position/street
- les sequences d'actions sont indispensables pour produire un contexte stable
- une absence de configuration doit etre explicite et jamais silencieuse
