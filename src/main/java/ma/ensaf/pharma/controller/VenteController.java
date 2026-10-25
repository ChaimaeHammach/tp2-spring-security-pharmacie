package ma.ensaf.pharma.controller;

import ma.ensaf.pharma.model.Vente;
import ma.ensaf.pharma.service.MedicamentService;
import ma.ensaf.pharma.service.VenteService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Ventes : ADMIN, PHARMACIEN, CAISSIER (règle "/ventes/**" dans SecurityConfig).
 */
@Controller
@RequestMapping("/ventes")
public class VenteController {

    private final VenteService venteService;
    private final MedicamentService medicamentService;

    public VenteController(VenteService venteService, MedicamentService medicamentService) {
        this.venteService = venteService;
        this.medicamentService = medicamentService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("medicaments", medicamentService.vendables());
        model.addAttribute("ventes", venteService.historique());
        model.addAttribute("caJour", venteService.chiffreAffairesDuJour());
        return "ventes";
    }

    /**
     * Authentication = l'utilisateur connecté, injecté par Spring Security.
     * auth.getName() donne son username -> enregistré comme vendeur.
     */
    @PostMapping
    public String vendre(@RequestParam("medicamentId") Long medicamentId,
                         @RequestParam("quantite") Integer quantite,
                         Authentication auth, RedirectAttributes ra) {
        try {
            Vente v = venteService.vendre(medicamentId, quantite == null ? 0 : quantite, auth.getName());
            ra.addFlashAttribute("succes", "Vente enregistrée : " + v.getQuantite() + " × "
                    + v.getMedicament().getNom() + " = " + v.getMontantTotal() + " DH");
        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/ventes";
    }
}
