package de.propra.splitter.web;

import de.propra.splitter.domain.person.Person;
import de.propra.splitter.services.SplitterService;
import de.propra.splitter.services.restschnittstelle.RestAusgaben;
import de.propra.splitter.services.restschnittstelle.RestAusgleich;
import de.propra.splitter.services.restschnittstelle.RestGruppe;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class WebController {

    SplitterService splitterService;

    public WebController(SplitterService splitterService) {
        this.splitterService = splitterService;
    }

    @GetMapping("/")
    public String index(Model m, OAuth2AuthenticationToken auth) {
        m.addAttribute("loginName", auth.getPrincipal().getAttribute("login"));
        m.addAttribute("ProfilePic", auth.getPrincipal().getAttribute("avatar_url"));
        m.addAttribute("GruppenOF", splitterService.alleOffenenGruppen(auth.getPrincipal().getAttribute("login")));
        m.addAttribute("GruppenGE", splitterService.alleGeschlossenenGruppen(auth.getPrincipal().getAttribute("login")));
        return "main";
    }

    @PostMapping("/")
    public String gruppeErstellen(Model m, String name, OAuth2AuthenticationToken auth) {
        splitterService.gruppeHinzufuegen(name, auth.getPrincipal().getAttribute("login"));
        m.addAttribute("GruppenOF", splitterService.alleOffenenGruppen(auth.getPrincipal().getAttribute("login")));
        m.addAttribute("GruppenGE", splitterService.alleGeschlossenenGruppen(auth.getPrincipal().getAttribute("login")));
        return "redirect:/";
    }

    @GetMapping("/details")
    public String indexDetails(Model m, Integer id, OAuth2AuthenticationToken auth) {
        if (!splitterService.findByID(id).getMitgliederNamen().contains(auth.getPrincipal().getAttribute("login"))) {
            return "redirect:/";
        }
        m.addAttribute("Mitglieder", splitterService.getMitgliederListe(id));
        m.addAttribute("Transaktionen", splitterService.getEigeneTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("TransaktionenB", splitterService.getAndereTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("aktuelleID", id);
        m.addAttribute("aktuelleGruppe", splitterService.findByID(id));
        m.addAttribute("ueberweisung", splitterService.getUeberweisungsdetail(id));
        return "details";
    }

    @PostMapping("/details/hinzufuegen")
    public String mitgliedHinzufuegen(Model m, String name, Integer id, OAuth2AuthenticationToken auth) {
        splitterService.mitgliedHinzufuegen(new Person(name), id);
        m.addAttribute("Mitglieder", splitterService.getMitgliederListe(id));
        m.addAttribute("Transaktion", splitterService.getEigeneTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("TransaktionB", splitterService.getAndereTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("aktuelleID", id);
        m.addAttribute("aktuelleGruppe", splitterService.findByID(id));
        m.addAttribute("ueberweisung", splitterService.getUeberweisungsdetail(id));
        return "redirect:/details?id=" + id;
    }

    @PostMapping("/details/transaktion")
    public String transaktionHinzufuegen(Model m, String empfaenger, double betrag, Integer id, OAuth2AuthenticationToken auth, String grund) {
        splitterService.transaktionHinzufuegen(splitterService.findByID(id).getMitgliederDurchName(auth.getPrincipal().getAttribute("login")), empfaenger, betrag, id, grund);
        m.addAttribute("Mitglieder", splitterService.getMitgliederListe(id));
        m.addAttribute("Transaktion", splitterService.getEigeneTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("TransaktionB", splitterService.getAndereTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("aktuelleID", id);
        m.addAttribute("aktuelleGruppe", splitterService.findByID(id));
        m.addAttribute("ueberweisung", splitterService.getUeberweisungsdetail(id));
        return "redirect:/details?id=" + id;
    }

    @PostMapping("/details/schliessen")
    public String gruppeSchliessen(Model m, Integer id, OAuth2AuthenticationToken auth) {
        m.addAttribute("Mitglieder", splitterService.getMitgliederListe(id));
        m.addAttribute("Transaktion", splitterService.getEigeneTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("TransaktionB", splitterService.getAndereTransaktionen(id, auth.getPrincipal().getAttribute("login")));
        m.addAttribute("aktuelleID", id);
        m.addAttribute("aktuelleGruppe", splitterService.findByID(id));
        m.addAttribute("ueberweisung", splitterService.gruppeSchliessen(id));
        return "redirect:/details?id=" + id;
    }

    @PostMapping("/api/gruppen")
    public ResponseEntity<Integer> apiGruppeErstellen(@RequestBody RestGruppe restGruppe) {
        if (restGruppe.personen().length > 0) {
            int id = splitterService.gruppeHinzufuegen(restGruppe.name(), restGruppe.personen()[0]);
            for (int i = 1; i < restGruppe.personen().length; i++) {
                splitterService.mitgliedHinzufuegen(new Person(restGruppe.personen()[i]), id);
            }
            return new ResponseEntity<>(id, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/api/user/{GITHUB-LOGIN}/gruppen")
    public ResponseEntity<List<RestGruppe>> apiGruppenAnzeigen(@PathVariable("GITHUB-LOGIN") String name) {
        List<RestGruppe> restGruppe = splitterService.restGruppen(name);
        if (restGruppe.size() > 0) {
            return new ResponseEntity<>(restGruppe, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/gruppen/{ID}")
    public ResponseEntity<RestGruppe> apiGruppenDetails(@PathVariable("ID") String gruppenId) {
        int id = 0;
        try {
            id = Integer.parseInt(gruppenId);
            splitterService.findByID(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String gruppenName = splitterService.findByID(id).getGruppenName();
        String[] personen = splitterService.personenSuchen(id);
        List<RestAusgaben> restAusgaben = new ArrayList<>();
        RestGruppe restGruppe = new RestGruppe(id, gruppenName, personen, false, restAusgaben);
        return new ResponseEntity<>(restGruppe, HttpStatus.OK);
    }

    @PostMapping("/api/gruppen/{ID}/schliessen")
    public ResponseEntity apiGruppeSchliessen(@PathVariable("ID") String gruppenId) {
        int id = 0;
        try {
            id = Integer.parseInt(gruppenId);
            splitterService.findByID(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        splitterService.gruppeSchliessen(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/api/gruppen/{ID}/auslagen")
    public ResponseEntity apiAuslagen(@PathVariable("ID") String gruppenId, @RequestBody RestAusgaben restAusgaben) {
        int id = 0;
        if (restAusgaben.schuldner() == null || restAusgaben.glaeubiger() == null || restAusgaben.grund() == null || restAusgaben.cent() == 0 || restAusgaben.glaeubiger().length() == 0 || restAusgaben.schuldner().size() == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            id = Integer.parseInt(gruppenId);
            splitterService.findByID(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (splitterService.findByID(id).getGruppeGeschlossen()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        Double betrag = (double) restAusgaben.cent() / 100;
        splitterService.restTransaktionen(id, restAusgaben, betrag);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/gruppen/{ID}/ausgleich")
    public ResponseEntity<List<RestAusgleich>> apiAusgleich(@PathVariable("ID") String gruppenId) {
        int id = 0;
        try {
            id = Integer.parseInt(gruppenId);
            splitterService.findByID(id);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<RestAusgleich> restAusgleich = new ArrayList<>();
        splitterService.restUeberweisungen(id, restAusgleich);
        return new ResponseEntity<>(restAusgleich, HttpStatus.OK);
    }
}