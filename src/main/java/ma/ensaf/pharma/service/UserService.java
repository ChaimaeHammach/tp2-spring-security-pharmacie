package ma.ensaf.pharma.service;

import ma.ensaf.pharma.dto.UserForm;
import ma.ensaf.pharma.model.Role;
import ma.ensaf.pharma.model.User;
import ma.ensaf.pharma.repository.RoleRepository;
import ma.ensaf.pharma.repository.UserRepository;
import ma.ensaf.pharma.repository.VenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gestion des utilisateurs (réservée à l'ADMIN, voir SecurityConfig : /admin/**).
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VenteRepository venteRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       VenteRepository venteRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.venteRepository = venteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> tous() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Role> tousLesRoles() {
        return roleRepository.findAll();
    }

    public User creer(UserForm form) {
        String username = form.getUsername() == null ? "" : form.getUsername().trim();
        if (username.length() < 3) {
            throw new IllegalArgumentException("Le nom d'utilisateur doit faire au moins 3 caractères");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Le nom d'utilisateur « " + username + " » existe déjà");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit faire au moins 6 caractères");
        }
        if (form.getRoleIds() == null || form.getRoleIds().isEmpty()) {
            throw new IllegalArgumentException("Choisissez au moins un rôle");
        }
        Set<Role> roles = new HashSet<>(roleRepository.findAllById(form.getRoleIds()));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(form.getPassword()));   // chiffrement BCrypt
        user.setRoles(roles);
        return userRepository.save(user);
    }

    /** Active / désactive un compte (un compte désactivé ne peut plus se connecter). */
    public User basculerActivation(Long id, String usernameConnecte) {
        User user = trouver(id);
        if (user.getUsername().equals(usernameConnecte)) {
            throw new IllegalArgumentException("Vous ne pouvez pas désactiver votre propre compte");
        }
        user.setEnabled(!user.isEnabled());
        return user;
    }

    public void supprimer(Long id, String usernameConnecte) {
        User user = trouver(id);
        if (user.getUsername().equals(usernameConnecte)) {
            throw new IllegalArgumentException("Vous ne pouvez pas supprimer votre propre compte");
        }
        if (venteRepository.existsByVendeurId(id)) {
            throw new IllegalArgumentException("« " + user.getUsername() + " » a enregistré des ventes : "
                    + "désactivez le compte au lieu de le supprimer");
        }
        userRepository.delete(user);
    }

    private User trouver(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    }
}
