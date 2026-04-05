# Conventions

## But

Ce document fixe les conventions de code et de modelisation a appliquer sur ce projet.

## Principes generaux

- privilegier la lisibilite, la testabilite et la separation des responsabilites
- preferer une solution simple et extensible a un hack rapide
- ne jamais improviser une logique metier floue
- documenter les hypotheses si une zone de spec reste ambigue
- produire du code pret a evoluer sans reecriture massive

## Separation des responsabilites

### Frontend Angular

- gere la presentation, la navigation, les formulaires reactifs et l'affichage
- centralise les appels HTTP dans des services dedies
- utilise des modeles TypeScript explicites
- evite la logique metier complexe dans les composants
- preferez des composants lisibles et `OnPush` quand pertinent
- prepare l'integration desktop via Electron sans coupler les composants a des APIs natives
- utilise Angular Material comme socle UI principal sauf besoin justifie contraire

### Backend Spring Boot

- porte la logique metier
- porte les validations metier
- reconstruit le contexte de decision
- execute le moteur de decision range-first
- execute le moteur de regles comme couche secondaire de trace ou de debug
- calcule l'equity uniquement comme information annexe
- gere la persistance et l'historisation

## Conventions de modelisation

- utiliser des types forts, enums et referentiels plutot que du texte libre
- ne jamais utiliser une couleur comme donnee metier
- normaliser les `handCode` ranges sur un referentiel canonique embarque de 169 mains (`AA`, `AKS`, `AKO`)
- distinguer les donnees de reference, de configuration, de contexte et d'historique
- chaque entite persistable doit posseder `id`, `createdAt`, `updatedAt`
- identifier clairement ce qui est une configuration utilisateur et ce qui est un resultat derive
- le catalogue public des conditions de regle reste ferme et type en v1 (`RuleConditionType`)

## Conventions de code

- classes courtes et coherentes
- methodes courtes avec une responsabilite claire
- noms explicites pour classes, DTO, services, variables et methodes
- eviter la duplication
- eviter les commentaires evidents
- ajouter une documentation courte sur les classes metier importantes et les services structurants
- documenter les algorithmes non triviaux et les choix d'architecture importants

## Conventions backend

- ne pas melanger les DTO REST avec les entites metier si leurs responsabilites divergent
- valider les entrees cote backend
- produire des erreurs explicites quand le contexte est invalide ou incomplet
- garder le moteur deterministe en cas de conflit de regles
- appliquer un ordre de resolution stable : priorite, specificite, nombre de conditions satisfaites, date de mise a jour, id stable
- la premiere `RuleAction` ordonnee reste l'action principale exposee dans le `DecisionResult`
- ne jamais laisser l'equity modifier `recommendedAction`
- la source nominale de decision est : cellule hero, puis ajustement hero vs villain, puis fallback `FOLD`

## Conventions frontend

- utiliser les reactive forms
- garder les composants principalement declaratifs
- deplacer l'orchestration vers des services ou facades de feature
- typer toutes les reponses API et modeles de formulaire
- preparer le frontend pour un usage desktop local
- garder la frontiere Angular <-> Electron explicite via services adaptes
- les composants de matrice restent UI-only : selection, toggle et edition locale; la validation metier reste backend
- dans les ecrans de regles, n'exposer a l'utilisateur que des labels lisibles et des listes controlees, jamais les codes bruts du domaine
- dans l'assistant, faire de la range hero la surface principale de travail; les details techniques restent secondaires

## Tests attendus

- tests unitaires sur les services critiques
- tests sur le moteur de decision et la resolution de conflits
- tests sur les validations backend
- tests sur les facades/frontend services qui orchestrent les appels critiques

## Documentation attendue

- documenter les endpoints REST importants
- maintenir ce dossier `docs/` a jour lorsqu'un choix structurant evolue
- faire converger le code avec `architecture.md`, `domain.md` et `conventions.md`
