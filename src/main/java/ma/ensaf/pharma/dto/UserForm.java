package ma.ensaf.pharma.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Données du formulaire "Nouvel utilisateur" (page admin).
 * On n'utilise pas directement l'entité User : le formulaire reçoit le mot de passe EN CLAIR
 * et des ids de rôles, alors que l'entité contient le hash et des objets Role.
 */
@Getter @Setter
public class UserForm {
    private String username;
    private String password;
    private List<Long> roleIds = new ArrayList<>();
}
