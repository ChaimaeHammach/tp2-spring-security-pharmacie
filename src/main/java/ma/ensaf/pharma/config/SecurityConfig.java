package ma.ensaf.pharma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration centrale de la sécurité.
 *
 *  1. AUTHENTIFICATION (qui es-tu ?)  : formulaire /login + utilisateurs en base MySQL
 *     -> Spring détecte tout seul le bean CustomUserDetailsService (UserDetailsService)
 *        et le PasswordEncoder ci-dessous, et les branche dans son DaoAuthenticationProvider.
 *
 *  2. AUTORISATION (as-tu le droit ?) : règles par URL dans filterChain().
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** BCrypt : hachage lent + "sel" aléatoire -> deux mots de passe identiques n'ont pas le même hash. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Pages publiques
                .requestMatchers("/login", "/access-denied", "/error", "/css/**").permitAll()

                // ADMIN : gestion des utilisateurs
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // ADMIN : créer / modifier / supprimer un médicament
                .requestMatchers("/medicaments/nouveau", "/medicaments/enregistrer",
                                 "/medicaments/*/modifier", "/medicaments/*/supprimer").hasRole("ADMIN")

                // ADMIN + PHARMACIEN : modifier le stock
                .requestMatchers("/medicaments/*/stock").hasAnyRole("ADMIN", "PHARMACIEN")

                // ADMIN + PHARMACIEN + CLIENT : consulter les médicaments (pas le CAISSIER)
                .requestMatchers("/medicaments/**").hasAnyRole("ADMIN", "PHARMACIEN", "CLIENT")

                // ADMIN + PHARMACIEN + CAISSIER : ventes (pas le CLIENT)
                .requestMatchers("/ventes/**").hasAnyRole("ADMIN", "PHARMACIEN", "CAISSIER")

                // Tout le reste (/, /home) : il suffit d'être connecté
                .anyRequest().authenticated()
                // ⚠ L'ORDRE COMPTE : Spring prend la PREMIÈRE règle qui correspond.
                //   Les règles précises doivent être avant "/medicaments/**".
            )
            .formLogin(form -> form
                .loginPage("/login")                  // notre page personnalisée (GET)
                .loginProcessingUrl("/login")         // le POST du formulaire est traité par Spring
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")                 // POST /logout (avec jeton CSRF)
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")   // connecté mais pas le bon rôle -> 403
            );
        // CSRF reste ACTIVÉ (protection par défaut) : Thymeleaf ajoute automatiquement
        // le jeton caché dans chaque <form th:action="..." method="post">.

        return http.build();
    }
}
