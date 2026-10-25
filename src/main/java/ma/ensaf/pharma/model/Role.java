package ma.ensaf.pharma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rôle : ADMIN, PHARMACIEN, CAISSIER, CLIENT.
 * On stocke le nom SANS le préfixe "ROLE_" : il est ajouté dans CustomUserDetailsService.
 */
@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
