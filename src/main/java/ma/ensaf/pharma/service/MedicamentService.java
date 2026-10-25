package ma.ensaf.pharma.service;

import ma.ensaf.pharma.model.Medicament;
import ma.ensaf.pharma.repository.MedicamentRepository;
import ma.ensaf.pharma.repository.VenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MedicamentService {

    private final MedicamentRepository medicamentRepository;
    private final VenteRepository venteRepository;

    public MedicamentService(MedicamentRepository medicamentRepository, VenteRepository venteRepository) {
        this.medicamentRepository = medicamentRepository;
        this.venteRepository = venteRepository;
    }

    /**
     * @param mot            texte recherché (peut être vide)
     * @param seulementDispo true pour le CLIENT : uniquement en stock et non expirés
     */
    @Transactional(readOnly = true)
    public List<Medicament> rechercher(String mot, boolean seulementDispo) {
        List<Medicament> liste = (mot == null || mot.isBlank())
                ? medicamentRepository.findAllByOrderByNomAsc()
                : medicamentRepository.findByNomContainingIgnoreCaseOrderByNomAsc(mot.trim());
        if (seulementDispo) {
            liste = liste.stream().filter(Medicament::isDisponible).toList();
        }
        return liste;
    }

    @Transactional(readOnly = true)
    public List<Medicament> vendables() {
        return medicamentRepository.findByQuantiteStockGreaterThanOrderByNomAsc(0).stream()
                .filter(m -> !m.isExpire())
                .toList();
    }

    @Transactional(readOnly = true)
    public Medicament trouver(Long id) {
        return medicamentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Médicament " + id + " introuvable"));
    }

    @Transactional(readOnly = true)
    public long compter() {
        return medicamentRepository.count();
    }

    /** Création (id null) ou modification (id existant) */
    public Medicament enregistrer(Medicament medicament) {
        return medicamentRepository.save(medicament);
    }

    public Medicament modifierStock(Long id, int nouvelleQuantite) {
        if (nouvelleQuantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }
        Medicament m = trouver(id);
        m.setQuantiteStock(nouvelleQuantite);   // dirty checking : UPDATE automatique à la fin
        return m;
    }

    public void supprimer(Long id) {
        Medicament m = trouver(id);
        // Une vente référence ce médicament (clé étrangère) : on garde l'historique
        if (venteRepository.existsByMedicamentId(id)) {
            throw new IllegalStateException("« " + m.getNom() + " » a déjà été vendu : suppression impossible "
                    + "(mettez plutôt son stock à 0).");
        }
        medicamentRepository.delete(m);
    }
}
