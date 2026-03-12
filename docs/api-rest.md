# API REST Backend

## Base URL

- `http://localhost:8080/api`

## Conventions

- JSON uniquement.
- `Content-Type: application/json`.
- Validation uniforme avec erreurs structurées.
- Ressources stables:
  - `profiles`
  - `ranges/hero`
  - `ranges/villain`
  - `rules`
  - `decision`
  - `history`
  - `settings`

## Format d'erreur

```json
{
  "timestamp": "2026-03-11T22:00:00Z",
  "status": 400,
  "code": "REQUEST_VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/decision",
  "method": "POST",
  "details": [
    {
      "field": "heroCards",
      "message": "must not be empty"
    }
  ]
}
```

Codes principaux:

- `RESOURCE_NOT_FOUND`
- `BUSINESS_VALIDATION_ERROR`
- `REQUEST_VALIDATION_ERROR`
- `CONSTRAINT_VALIDATION_ERROR`
- `INTERNAL_SERVER_ERROR`

## Endpoints

### Profils

- `GET /profiles` -> liste résumée.
- `GET /profiles/active` -> profil actif.
- `GET /profiles/{profileId}` -> détail.
- `POST /profiles` -> création.
- `PUT /profiles/{profileId}` -> mise à jour.
- `PUT /profiles/{profileId}/activate` -> activation.
- `POST /profiles/{profileId}/duplicate` -> duplication.
- `DELETE /profiles/{profileId}` -> suppression.

### Ranges Hero

- `GET /ranges/hero?profileId={uuid}` -> liste.
- `GET /ranges/hero/{rangeId}` -> détail.
- `GET /ranges/hero/context?...` -> résolution par contexte.
- `POST /ranges/hero` -> création.
- `PUT /ranges/hero/{rangeId}` -> mise à jour.
- `PUT /ranges/hero/{rangeId}/cells` -> mise à jour des cellules.
- `DELETE /ranges/hero/{rangeId}` -> suppression.

### Ranges Villain

- `GET /ranges/villain?profileId={uuid}` -> liste.
- `GET /ranges/villain/{rangeId}` -> détail.
- `GET /ranges/villain/context?...` -> résolution par contexte.
- `POST /ranges/villain` -> création.
- `POST /ranges/villain/bulk` -> création/édition avec scopes multi-spots.
- `PUT /ranges/villain/{rangeId}` -> mise à jour.
- `PUT /ranges/villain/{rangeId}/cells` -> mise à jour des cellules.
- `DELETE /ranges/villain/{rangeId}` -> suppression.

### Règles

- `GET /rules` -> liste filtrable.
- `GET /rules/{ruleId}` -> détail.
- `POST /rules` -> création.
- `PUT /rules/{ruleId}` -> mise à jour.
- `POST /rules/{ruleId}/duplicate` -> duplication.
- `DELETE /rules/{ruleId}` -> suppression.

### Décision

- `POST /decision` -> calcul de décision.

Champs d'entrée minimum pertinents:

- `strategyProfileId`
- `gameType`
- `street`
- `heroPosition`
- `villainPosition`
- `heroCards`
- `scenarioType`
- `effectiveStackInBigBlinds`
- `potSizeInBigBlinds`
- `actionEvents` (optionnel mais recommandé)

Champs de sortie:

- `status`
- `recommendedAction`
- `recommendedSizingValue`
- `matchedRuleId`
- `matchedRuleName`
- `explanation`
- `warnings`
- `matchedCandidates`

### Historique

- `GET /history` -> derniers reviews.
- `GET /history/{reviewId}` -> détail review.
- `POST /history` -> création d'une review observée.

Payload principal de création:

- `strategyProfileId`
- `heroHandCode`
- `boardCards` (optionnel)
- `decisionStatus`
- `recommendedAction`
- `actualAction`
- `netResultInBigBlinds`
- `issue` (`WIN`, `LOSS`, `BREAKEVEN`)
- `situationSummary` (optionnel)
- `engineExplanation` (optionnel)
- `reviewNote` (optionnel)

### Paramètres

- `GET /settings` -> configuration courante.
- `PUT /settings` -> mise à jour configuration.
`scopes` (optionnel) sur `POST /ranges/villain` et `POST /ranges/villain/bulk`:

- `heroPosition` (nullable)
- `villainPosition` (nullable)
- `triggerActionCode` (nullable)
- `lineSignature` (nullable)
- `scopeWeight` (optionnel, défaut `0`)

Les valeurs `null` sont traitées comme wildcard lors de la résolution.
