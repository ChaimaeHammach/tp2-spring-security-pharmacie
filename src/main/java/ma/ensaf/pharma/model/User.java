package ma.ensaf.pharma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Utilisateur de l'application.
 * Table "users" (et pas "user" : mot réservé dans plusieurs SGBD).
 */
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    // Mot de passe CHIFFRÉ avec BCrypt (jamais en clair !) -> 60 caractères "$2a$10$..."
    @Column(nullable = false)
    private String password;

    // Un compte désactivé ne peut plus se connecter (Spring Security le vérifie)
    private boolean enabled = true;

    // EAGER : les rôles sont nécessaires à chaque connexion pour construire les autorisations
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /** Pratique dans les pages : user.hasRole("ADMIN") */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getName().equals(roleName));
    }
}
