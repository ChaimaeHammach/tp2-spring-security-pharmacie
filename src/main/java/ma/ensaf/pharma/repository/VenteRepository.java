package ma.ensaf.pharma.repository;

import ma.ensaf.pharma.model.Vente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    List<Vente> findAllByOrderByDateVenteDesc();

    boolean existsByMedicamentId(Long medicamentId);

    boolean existsByVendeurId(Long userId);

    // Chiffre d'affaires depuis une date (ex : depuis minuit = CA du jour). Renvoie null s'il n'y a aucune vente.
    @Query("SELECT SUM(v.montantTotal) FROM Vente v WHERE v.dateVente >= :depuis")
    BigDecimal chiffreAffairesDepuis(@Param("depuis") LocalDateTime depuis);
}
