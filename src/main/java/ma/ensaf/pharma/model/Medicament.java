package ma.ensaf.pharma.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medicaments")
@Getter @Setter @NoArgsConstructor
public class Medicament {

    /** En dessous de ce seuil, le stock est signalé comme faible */
    public static final int SEUIL_ALERTE = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Les annotations de validation sont vérifiées par @Valid dans le contrôleur
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nom;

    @Size(max = 100)
    private String categorie;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", message = "Le prix doit être positif")
    @Column(precision = 10, scale = 2)
    private BigDecimal prix;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 0, message = "La quantité ne peut pas être négative")
    private Integer quantiteStock = 0;

    // Format attendu par <input type="date"> : yyyy-MM-dd
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateExpiration;

    public Medicament(String nom, String categorie, String description, BigDecimal prix,
                      int quantiteStock, LocalDate dateExpiration) {
        this.nom = nom;
        this.categorie = categorie;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
        this.dateExpiration = dateExpiration;
    }

    public boolean isDisponible() {
        return quantiteStock != null && quantiteStock > 0 && !isExpire();
    }

    public boolean isExpire() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now());
    }

    public boolean isStockFaible() {
        return quantiteStock != null && quantiteStock > 0 && quantiteStock <= SEUIL_ALERTE;
    }
}
