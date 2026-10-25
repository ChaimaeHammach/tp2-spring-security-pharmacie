package ma.ensaf.pharma.service;

import ma.ensaf.pharma.model.Medicament;
import ma.ensaf.pharma.model.Role;
import ma.ensaf.pharma.model.User;
import ma.ensaf.pharma.repository.MedicamentRepository;
import ma.ensaf.pharma.repository.RoleRepository;
import ma.ensaf.pharma.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Remplit la base au démarrage : rôles, utilisateurs de test, médicaments.
 * Chaque élément n'est créé QUE s'il n'existe pas encore
 * -> on peut redémarrer l'application autant de fois qu'on veut (pas besoin de commenter @Component).
 */
@Component
public class InitData implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MedicamentRepository medicamentRepository;
    private final PasswordEncoder passwordEncoder;

    public InitData(RoleRepository roleRepository, UserRepository userRepository,
                    MedicamentRepository medicamentRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.medicamentRepository = medicamentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role admin = creerRole("ADMIN");
        Role pharmacien = creerRole("PHARMACIEN");
        Role caissier = creerRole("CAISSIER");
        Role client = creerRole("CLIENT");

        // Scénarios de test de l'énoncé (+ un client pour tester la lecture seule)
        creerUser("admin", "admin123", admin);
        creerUser("pharma", "pharma123", pharmacien);
        creerUser("caisse", "caisse123", caissier);
        creerUser("client", "client123", client);

        if (medicamentRepository.count() == 0) {
            medicamentRepository.save(new Medicament("Amoxicilline 500mg", "Antibiotiques",
                    "Antibiotique à large spectre.", new BigDecimal("45.00"), 40, LocalDate.of(2027, 3, 10)));
            medicamentRepository.save(new Medicament("Doliprane 500mg", "Antalgiques",
                    "Antalgique et antipyrétique.", new BigDecimal("25.50"), 120, LocalDate.of(2027, 11, 1)));
            medicamentRepository.save(new Medicament("Vitamine C 1000mg", "Vitamines",
                    "Complément pour renforcer les défenses.", new BigDecimal("30.00"), 8, LocalDate.of(2027, 6, 15)));
            medicamentRepository.save(new Medicament("Bétadine Solution 10%", "Antiseptiques",
                    "Désinfectant pour usage externe.", new BigDecimal("18.00"), 25, LocalDate.of(2027, 1, 30)));
            medicamentRepository.save(new Medicament("Nurofen 400mg", "Antalgiques",
                    "Soulage la douleur et la fièvre.", new BigDecimal("42.00"), 0, LocalDate.of(2027, 2, 20)));
            medicamentRepository.save(new Medicament("Voltaren 50mg", "Anti-inflammatoires",
                    "Anti-inflammatoire pour douleurs musculaires.", new BigDecimal("58.90"), 15, LocalDate.of(2026, 1, 15)));
        }
    }

    private Role creerRole(String nom) {
        return roleRepository.findByName(nom)
                .orElseGet(() -> roleRepository.save(new Role(nom)));
    }

    private void creerUser(String username, String motDePasse, Role role) {
        if (userRepository.existsByUsername(username)) return;
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(motDePasse));   // JAMAIS en clair en base
        user.setRoles(new HashSet<>(Set.of(role)));
        userRepository.save(user);
    }
}
