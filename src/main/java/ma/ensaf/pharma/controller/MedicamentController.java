package ma.ensaf.pharma.controller;

import jakarta.validation.Valid;
import ma.ensaf.pharma.model.Medicament;
import ma.ensaf.pharma.service.MedicamentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Les droits sont vérifiés AVANT d'arriver ici, par les règles de SecurityConfig :
 *   GET  /medicaments                 -> ADMIN, PHARMACIEN, CLIENT
 *   POST /medicaments/{id}/stock      -> ADMIN, PHARMACIEN
 *   nouveau / modifier / supprimer    -> ADMIN
 */
@Controller
@RequestMapping("/medicaments")
public class MedicamentController {

    private final MedicamentService medicamentService;

    public MedicamentController(MedicamentService medicamentService) {
        this.medicamentService = medicamentService;
    }

    @GetMapping
    public String liste(@RequestParam(name = "q", required = false) String q,
                        Authentication auth, Model model) {
        // Le CLIENT ne voit que les médicaments disponibles (en stock et non expirés)
        boolean gestionnaire = aRole(auth, "ROLE_ADMIN") || aRole(auth, "ROLE_PHARMACIEN");
        model.addAttribute("medicaments", medicamentService.rechercher(q, !gestionnaire));
        model.addAttribute("q", q);
        model.addAttribute("seuil", Medicament.SEUIL_ALERTE);
        return "medicaments";
    }

    // ----------------------------- ADMIN : formulaire -----------------------------

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("medicament", new Medicament());
        return "medicament-form";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable("id") Long id, Model model) {
        model.addAttribute("medicament", medicamentService.trouver(id));
        return "medicament-form";
    }

    /**
     * @Valid déclenche les contrôles @NotBlank, @Min... de l'entité.
     * S'il y a des erreurs, on réaffiche le formulaire avec les messages (th:errors).
     */
    @PostMapping("/enregistrer")
    public String enregistrer(@Valid @ModelAttribute("medicament") Medicament medicament,
                              BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "medicament-form";
        }
        boolean creation = medicament.getId() == null;
        medicamentService.enregistrer(medicament);
        ra.addFlashAttribute("succes", "« " + medicament.getNom() + " » "
                + (creation ? "ajouté." : "modifié."));
        return "redirect:/medicaments";
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable("id") Long id, RedirectAttributes ra) {
        try {
            medicamentService.supprimer(id);
            ra.addFlashAttribute("succes", "Médicament supprimé.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/medicaments";
    }

    // ----------------------------- ADMIN + PHARMACIEN : stock -----------------------------

    @PostMapping("/{id}/stock")
    public String modifierStock(@PathVariable("id") Long id,
                                @RequestParam("quantite") Integer quantite,
                                RedirectAttributes ra) {
        try {
            Medicament m = medicamentService.modifierStock(id, quantite == null ? -1 : quantite);
            ra.addFlashAttribute("succes", "Stock de « " + m.getNom() + " » mis à jour : " + m.getQuantiteStock());
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/medicaments";
    }

    private boolean aRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
