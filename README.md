# TP2 — Sécurisation de la gestion de pharmacie (Spring Security)

Corrigé du TP2 du module *Spring Boot – MicroServices* (ENSA Fès).
Spring Boot 3.5.7 · Java 17 · Spring Security 6 · MySQL · Thymeleaf (+ `thymeleaf-extras-springsecurity6`).

---

## 1. Lancer le projet

1. Démarrer **MySQL** dans XAMPP (il doit être vert).
2. Ouvrir le dossier `pharma_security` dans VS Code (*File → Open Folder*) ou IntelliJ.
3. Lancer `PharmaSecurityApplication.java` (*Run*).
4. Aller sur **http://localhost:8080** → redirection automatique vers la page de connexion.

Au premier démarrage, `InitData` crée la base `pharma_security_db`, les 4 rôles, les utilisateurs de test et 6 médicaments.

### Comptes de test

| Utilisateur | Mot de passe | Rôle |
|---|---|---|
| admin | admin123 | ADMIN |
| pharma | pharma123 | PHARMACIEN |
| caisse | caisse123 | CAISSIER |
| client | client123 | CLIENT |

---

## 2. Qui a accès à quoi

| Page / action | ADMIN | PHARMACIEN | CAISSIER | CLIENT |
|---|:-:|:-:|:-:|:-:|
| Accueil `/home` | ✅ | ✅ | ✅ | ✅ |
| Voir les médicaments `/medicaments` | ✅ tous | ✅ tous | ❌ | ✅ disponibles seulement |
| Modifier un stock `/medicaments/{id}/stock` | ✅ | ✅ | ❌ | ❌ |
| Ajouter / modifier / supprimer un médicament | ✅ | ❌ | ❌ | ❌ |
| Vendre + historique `/ventes` | ✅ | ✅ | ✅ | ❌ |
| Gérer les utilisateurs `/admin` | ✅ | ❌ | ❌ | ❌ |

Un accès interdit affiche la page **403** (`access-denied.html`).

---

## 3. Structure (celle proposée par l'énoncé)

```
src/main/java/ma/ensaf/pharma
├── config/      SecurityConfig            ← règles d'accès, login, logout, BCrypt
├── model/       User, Role, Medicament, Vente
├── repository/  UserRepository, RoleRepository, MedicamentRepository, VenteRepository
├── service/     CustomUserDetailsService  ← charge l'utilisateur depuis MySQL
│                InitData                  ← données de test au démarrage
│                MedicamentService, VenteService, UserService
├── controller/  LoginController, MedicamentController, AdminController, VenteController
└── dto/         UserForm                  ← formulaire "nouvel utilisateur"

src/main/resources/templates
  login.html, home.html, admin.html, medicaments.html, access-denied.html
  + medicament-form.html, ventes.html, fragments/layout.html
```

`Vente` (+ repository, service, contrôleur, page) a été ajoutée : sans elle, le CAISSIER
(« enregistrer les ventes ») n'aurait rien à faire.

---

## 4. Comment ça marche

### Authentification (qui es-tu ?)

```
Formulaire /login (username + password)
      │  POST /login  ← traité par Spring Security, pas par un contrôleur
      ▼
CustomUserDetailsService.loadUserByUsername("pharma")
      │  cherche dans la table users → mot de passe haché + rôles
      ▼
BCrypt compare le mot de passe saisi avec le hash
      │
      ├─ OK      → session créée → redirection /home
      └─ échec   → /login?error
```

**Analogie** : Spring Security est le vigile à l'entrée de la pharmacie. `CustomUserDetailsService`
est le registre du personnel qu'il consulte : il y trouve ton badge (hash du mot de passe) et
tes autorisations (rôles).

### Autorisation (as-tu le droit ?)

Chaque requête passe par les règles de `SecurityConfig`, **dans l'ordre** : la première règle qui
correspond l'emporte. C'est pour ça que `/medicaments/nouveau` (ADMIN) est écrit **avant**
`/medicaments/**` (ADMIN, PHARMACIEN, CLIENT). Dans l'autre ordre, un CLIENT pourrait ouvrir le formulaire d'ajout.

### Le préfixe `ROLE_`
En base on stocke `PHARMACIEN`. `CustomUserDetailsService` le transforme en `ROLE_PHARMACIEN`,
et `hasRole("PHARMACIEN")` cherche justement `ROLE_PHARMACIEN`. Les deux doivent être cohérents.

### Mots de passe chiffrés (BCrypt)
Dans phpMyAdmin, la table `users` contient des valeurs du type `$2a$10$Xk3...` et jamais `admin123`.
BCrypt ajoute un « sel » aléatoire : deux utilisateurs avec le même mot de passe ont des hash différents.

### Deux niveaux de protection
1. **Serveur** (`SecurityConfig`) : bloque vraiment l'accès, même si on tape l'URL à la main.
2. **Affichage** (`sec:authorize` dans les pages) : cache les liens et boutons inutiles.

Le niveau 2 est du confort : seul le niveau 1 protège réellement.

---

## 5. Différences avec les exemples du cours (springmvc-security-v1/v2)

| Exemple du cours | Ici | Pourquoi |
|---|---|---|
| `csrf.disable()` | CSRF **activé** | Protection par défaut contre les requêtes forgées. Thymeleaf ajoute le jeton caché tout seul dans chaque `<form th:action=... method="post">`. |
| Lien `<a href="/logout">` | Bouton dans un `<form method="post">` | Avec CSRF actif, la déconnexion doit être un POST. |
| Bean `DaoAuthenticationProvider` | Pas besoin | Spring Boot branche automatiquement le `UserDetailsService` et le `PasswordEncoder` déclarés. Le résultat est identique. |
| `DataInit` à commenter après le 1er lancement | `InitData` vérifie avant d'insérer | On peut relancer sans erreur de doublon. |

Si ton prof préfère CSRF désactivé, ajoute `.csrf(csrf -> csrf.disable())` dans `SecurityConfig` : le reste fonctionne pareil.

---

## 6. Scénarios à tester

1. **admin** : tous les menus sont visibles. Crée un utilisateur, désactive-le, puis essaie de te connecter avec lui → refus.
2. **pharma** : modifie un stock. Ouvre `http://localhost:8080/admin` à la main → page 403.
3. **caisse** : seul le menu *Ventes* est visible. Vends 2 Doliprane, puis vérifie avec `pharma` que le stock a baissé de 2. `/medicaments` → 403.
4. **client** : voit seulement les médicaments disponibles (Nurofen en rupture et Voltaren expiré sont cachés). `/ventes` → 403.
5. Mauvais mot de passe → message d'erreur sur la page de connexion.
6. Vendre plus que le stock disponible → message « Stock insuffisant ».

---

## 7. Remettre à zéro
Dans phpMyAdmin : supprimer la base `pharma_security_db`, puis relancer l'application.
