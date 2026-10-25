package ma.ensaf.pharma.controller;

import ma.ensaf.pharma.dto.UserForm;
import ma.ensaf.pharma.service.MedicamentService;
import ma.ensaf.pharma.service.UserService;
import ma.ensaf.pharma.service.VenteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Zone ADMIN (règle "/admin/**" -> hasRole("ADMIN") dans SecurityConfig).
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final MedicamentService medicamentService;
    private final VenteService venteService;

    public AdminController(UserService userService, MedicamentService medicamentService, VenteService venteService) {
        this.userService = userService;
        this.medicamentService = medicamentService;
        this.venteService = venteService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("users", userService.tous());
        model.addAttribute("roles", userService.tousLesRoles());
        model.addAttribute("nbMedicaments", medicamentService.compter());
        model.addAttribute("nbVentes", venteService.compter());
        model.addAttribute("caJour", venteService.chiffreAffairesDuJour());
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        return "admin";
    }

    @PostMapping("/users")
    public String creer(@ModelAttribute("userForm") UserForm form, RedirectAttributes ra) {
        try {
            userService.creer(form);
            ra.addFlashAttribute("succes", "Utilisateur « " + form.getUsername() + " » créé.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            form.setPassword(null);                    // on ne renvoie jamais le mot de passe
            ra.addFlashAttribute("userForm", form);    // garde ce qui a été saisi
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/activation")
    public String activation(@PathVariable("id") Long id, Authentication auth, RedirectAttributes ra) {
        try {
            var u = userService.basculerActivation(id, auth.getName());
            ra.addFlashAttribute("succes", "Compte « " + u.getUsername() + " » "
                    + (u.isEnabled() ? "activé." : "désactivé."));
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/supprimer")
    public String supprimer(@PathVariable("id") Long id, Authentication auth, RedirectAttributes ra) {
        try {
            userService.supprimer(id, auth.getName());
            ra.addFlashAttribute("succes", "Utilisateur supprimé.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/admin";
    }
}
