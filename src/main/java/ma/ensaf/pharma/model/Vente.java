package ma.ensaf.pharma.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une vente : quel médicament, combien, à quel prix, quand, et PAR QUI (traçabilité).
 */
@Entity
@Table(name = "ventes")
@Getter @Setter @NoArgsConstructor
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "medicament_id")
    private Medicament medicament;

    @Column(nullable = false)
    private Integer quantite;

    // On garde le prix AU MOMENT de la vente (il peut changer ensuite)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateVente;

    // L'utilisateur connecté qui a enregistré la vente
    @ManyToOne(optional = false)
    @JoinColumn(name = "vendeur_id")
    private User vendeur;
}
