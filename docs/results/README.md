# Résultats et preuves d'exécution

## Périmètre des preuves

Cette synthèse est extraite des XML Surefire présents localement, datés du **22 octobre 2026**, et du rapport Robot généré le **21 octobre 2026**. Les XML sont les derniers fichiers disponibles par classe : leur somme ne représente pas nécessairement une seule exécution continue, ni le même commit applicatif. Les versions exactes de l'application ne sont pas établies par cet export.

## Selenium / JUnit

**35 tests : 29 sans échec ni erreur, 0 échec d'assertion, 6 erreurs, 0 ignoré.**

Les quatre tests Page Object utilisent des pages HTML autonomes. Ils vérifient les helpers de test ; ils ne valident pas les API de l'application.

| Classe | Tests | Sans erreur | Erreurs |
| --- | ---: | ---: | ---: |
| `EmptyLoginValidationTest` | 1 | 1 | 0 |
| `ForgotPasswordValidationTest` | 1 | 1 | 0 |
| `InvalidLoginTest` | 1 | 1 | 0 |
| `LogoutTest` | 1 | 1 | 0 |
| `ResetPasswordValidationTest` | 2 | 2 | 0 |
| `ValidLoginTest` | 1 | 1 | 0 |
| `PageObjectRegressionTest` | 4 | 4 | 0 |
| `DepartmentCrudTest` | 1 | 1 | 0 |
| `KanbanTaskWorkflowTest` | 1 | 0 | 1 |
| `KanbanUiTest` | 3 | 3 | 0 |
| `AdminRouteAccessTest` | 1 | 1 | 0 |
| `ProtectedRouteTest` | 1 | 1 | 0 |
| `PermissionUpdateTest` | 1 | 0 | 1 |
| `RoleListTest` | 2 | 2 | 0 |
| `TeamManagementTest` | 1 | 1 | 0 |
| `UserCreationTest` | 2 | 1 | 1 |
| `UserDeletionTest` | 1 | 0 | 1 |
| `UserEditTest` | 1 | 0 | 1 |
| `UserEmailValidationTest` | 1 | 1 | 0 |
| `UserListTest` | 1 | 1 | 0 |
| `UserRequiredFieldsTest` | 5 | 4 | 1 |
| `UserRoleAssignmentTest` | 1 | 1 | 0 |
| `UserSearchTest` | 1 | 1 | 0 |

## Erreurs à diagnostiquer

| Classe | Type d'erreur observé |
| --- | --- |
| `KanbanTaskWorkflowTest` | `StaleElementReferenceException` |
| `PermissionUpdateTest` | `StaleElementReferenceException` |
| `UserCreationTest` | `NoSuchSessionException` |
| `UserDeletionTest` | `TimeoutException` |
| `UserEditTest` | `TimeoutException` |
| `UserRequiredFieldsTest` | `IllegalStateException` |

Une erreur Selenium ne prouve pas à elle seule un défaut métier. Il faut distinguer synchronisation du DOM, session navigateur, configuration, disponibilité des services et régression applicative. Les traces brutes contenant des propriétés d'environnement ne sont pas exportées.

## Robot Framework

Rapport local : **2 réussites, 0 échec, 0 ignoré**. Deux scénarios JWT : connexion et refus des identifiants invalides. Le scénario négatif exige actuellement seulement un statut différent de 200 ; il peut accepter un 500. Son contrat doit être renforcé avant de présenter cette suite comme une validation complète de l'authentification.

## k6

Les trois tests Node du validateur de cible passent. Cela valide les garde-fous de configuration, **pas les performances de l'API**. Aucun résultat de charge ou de stress n'est revendiqué.

## Fichiers publiés

- [Résumé JSON](summary.json) : compteurs et classes d'erreurs autorisés.
- [Résumé CSV](selenium-summary.csv) : résultats par classe.
- [Rapport HTML de synthèse](report.html) : ouvrir après téléchargement ou clonage ; GitHub ne rend pas le HTML comme une page Web.
- Les XML Surefire, logs Robot et résultats Allure bruts restent locaux. Les workflows peuvent produire des artefacts ; contrôler leur contenu avant de les partager.

## Vérification avant publication

Le 28 octobre 2026 : compilation Maven hors ligne réussie (test-compile), trois tests Node du validateur k6 réussis et vérification des liens locaux. Aucune nouvelle campagne Selenium contre l'application ni mesure de charge n'a été lancée pour cette publication.
