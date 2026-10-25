package ma.ensaf.pharma.service;

import ma.ensaf.pharma.model.Medicament;
import ma.ensaf.pharma.model.User;
import ma.ensaf.pharma.model.Vente;
import ma.ensaf.pharma.repository.MedicamentRepository;
import ma.ensaf.pharma.repository.UserRepository;
import ma.ensaf.pharma.repository.VenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class VenteService {

    private final VenteRepository venteRepository;
    private final MedicamentRepository medicamentRepository;
    private final UserRepository userRepository;

    public VenteService(VenteRepository venteRepository, MedicamentRepository medicamentRepository,
                        UserRepository userRepository) {
        this.venteRepository = venteRepository;
        this.medicamentRepository = medicamentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Vente> historique() {
        return venteRepository.findAllByOrderByDateVenteDesc();
    }

    @Transactional(readOnly = true)
    public long compter() {
        return venteRepository.count();
    }

    @Transactional(readOnly = true)
    public BigDecimal chiffreAffairesDuJour() {
        BigDecimal ca = venteRepository.chiffreAffairesDepuis(LocalDate.now().atStartOfDay());
        return ca == null ? BigDecimal.ZERO : ca;   // SUM sur aucune ligne -> null
    }

    /**
     * Enregistre une vente ET diminue le stock, dans UNE SEULE transaction :
     * si une étape échoue, rien n'est enregistré (ni la vente, ni la baisse du stock).
     *
     * @param username nom de l'utilisateur connecté (fourni par Spring Security)
     */
    public Vente vendre(Long medicamentId, int quantite, String username) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantité doit être au moins 1");
        }
        Medicament m = medicamentRepository.findById(medicamentId)
                .orElseThrow(() -> new IllegalArgumentException("Médicament introuvable"));
        if (m.isExpire()) {
            throw new IllegalArgumentException("« " + m.getNom() + " » est expiré : vente interdite");
        }
        if (m.getQuantiteStock() < quantite) {
            throw new IllegalArgumentException("Stock insuffisant pour « " + m.getNom() + " » : "
                    + m.getQuantiteStock() + " disponible(s)");
        }
        User vendeur = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Utilisateur connecté introuvable"));

        m.setQuantiteStock(m.getQuantiteStock() - quantite);

        Vente v = new Vente();
        v.setMedicament(m);
        v.setQuantite(quantite);
        v.setPrixUnitaire(m.getPrix());
        v.setMontantTotal(m.getPrix().multiply(BigDecimal.valueOf(quantite)));
        v.setDateVente(LocalDateTime.now());
        v.setVendeur(vendeur);
        return venteRepository.save(v);
    }
}
