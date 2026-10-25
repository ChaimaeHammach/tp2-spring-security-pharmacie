package ma.ensaf.pharma.repository;

import ma.ensaf.pharma.model.Medicament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicamentRepository extends JpaRepository<Medicament, Long> {

    // Recherche par nom (insensible à la casse) : WHERE LOWER(nom) LIKE LOWER('%mot%')
    List<Medicament> findByNomContainingIgnoreCaseOrderByNomAsc(String mot);

    List<Medicament> findAllByOrderByNomAsc();

    // Médicaments vendables : stock > 0
    List<Medicament> findByQuantiteStockGreaterThanOrderByNomAsc(int quantite);
}
