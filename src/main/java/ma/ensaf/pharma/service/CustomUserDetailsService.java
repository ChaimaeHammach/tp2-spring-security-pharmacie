package ma.ensaf.pharma.service;

import ma.ensaf.pharma.model.User;
import ma.ensaf.pharma.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pont entre NOTRE table users et Spring Security.
 * À chaque tentative de connexion, Spring appelle loadUserByUsername(...)
 * puis compare lui-même le mot de passe saisi avec le hash BCrypt renvoyé.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));

        // "PHARMACIEN" en base -> autorité "ROLE_PHARMACIEN" pour Spring
        // (hasRole("PHARMACIEN") cherche justement "ROLE_PHARMACIEN")
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())     // hash BCrypt
                .disabled(!user.isEnabled())      // compte désactivé -> connexion refusée
                .authorities(authorities)
                .build();
    }
}
