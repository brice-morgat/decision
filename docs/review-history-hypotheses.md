# Hypotheses - Review History (Lot 15)

## Objectif retenu

Le module `history` capture une review comme un enregistrement manuel et persistant d'une situation + recommandation + exécution réelle + résultat.

## Hypotheses techniques

- `issue` est stockée explicitement (`WIN`, `LOSS`, `BREAKEVEN`) pour préparer les agrégations futures sans recalcul.
- `netResultInBigBlinds` est la métrique numérique de base pour l'agrégation v2.
- cohérence métier imposée: `WIN > 0`, `LOSS < 0`, `BREAKEVEN = 0`.
- `recommendedAction` est persistée via `DecisionResult`.
- `actualAction` + `issue` + `netResultInBigBlinds` sont persistés via `HandOutcome`.
- `situationSummary` est stocké dans `HandOutcome.note`.
- `reviewNote` est stocké dans `HandReview.reviewNote`.

## Limites volontaires en v1

- La reconstruction complète de `DecisionInput` n'est pas recalculée par le moteur lors de la création manuelle d'une review.
- Les champs de contexte avancé (séquence détaillée, sizing détaillé, positions exactes de la main reviewée) restent simplifiés.
- L'agrégation (stats globales, par profil, par action, par issue) n'est pas implémentée ici, mais le modèle est préparé.
