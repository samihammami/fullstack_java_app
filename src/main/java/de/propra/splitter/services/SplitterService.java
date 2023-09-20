package de.propra.splitter.services;

import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.transaktion.Transaktion;
import de.propra.splitter.domain.person.Person;
import de.propra.splitter.services.restschnittstelle.RestAusgaben;
import de.propra.splitter.services.restschnittstelle.RestAusgleich;
import de.propra.splitter.services.restschnittstelle.RestGruppe;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SplitterService {

    private GruppenRepo gruppenRepository;

    private ArrayList<Gruppe> gruppen = new ArrayList<>();

    @PersistenceCreator
    public SplitterService(GruppenRepo g) {
        gruppenRepository = g;
    }

    public int gruppeHinzufuegen(String gruppeName, String name) {
        int id = gruppenRepository.alleGruppen().size();
        Gruppe gruppe = new Person(name).gruppeErstellen(gruppeName, id);
        gruppen.add(gruppe);
        gruppenRepository.speichern(gruppe);
        return id;
    }

    public Gruppe findByID(int id) {
        for (Gruppe g : gruppenRepository.alleGruppen()) {
            if (g.getId() == id) {
                return g;
            }
        }
        throw new NichtVorhandenException("Gruppe nicht gefunden");
    }

    public List<Gruppe> alleGruppen(String p) {
        List<Gruppe> gr = new ArrayList<>();
        if (gruppenRepository.alleGruppen() == null) {
            return gr;
        }
        for (Gruppe g : gruppenRepository.alleGruppen()) {
            if (g.getMitgliederNamen().contains(p)) {
                gr.add(g);
            }
        }
        return gr;
    }

    public List<Gruppe> alleOffenenGruppen(String p) {
        List<Gruppe> gr = new ArrayList<>();
        for (Gruppe g : alleGruppen(p)) {
            if (!g.getGruppeGeschlossen()) {
                gr.add(g);
            }
        }
        return gr;
    }

    public List<Gruppe> alleGeschlossenenGruppen(String p) {
        List<Gruppe> gr = new ArrayList<>();
        for (Gruppe g : alleGruppen(p)) {
            if (g.getGruppeGeschlossen()) {
                gr.add(g);
            }
        }
        return gr;
    }

    public List<Transaktion> getTransaktionen(int id) {
        return gruppenRepository.getfuerIdTransaktion(id);
    }

    public List<Transaktion> getEigeneTransaktionen(int id, String name) {
        List<Transaktion> liste = new ArrayList<>();
        for (Transaktion t: gruppenRepository.getfuerIdTransaktion(id)) {
            if(t.alleNamen().contains(name) || t.sender().getPersonName().equals(name)){
                liste.add(t);
            }
        }
        return liste;
    }

    public List<Transaktion> getAndereTransaktionen(int id, String name) {
        List<Transaktion> liste = new ArrayList<>();
        for (Transaktion t: gruppenRepository.getfuerIdTransaktion(id)) {
            if(!t.alleNamen().contains(name) &&! t.sender().getPersonName().equals(name)){
                liste.add(t);
            }
        }
        return liste;
    }

    public List<Person> getMitgliederListe(Integer id) {
        return gruppenRepository.gruppeFinden(id).getMitglieder();
    }

    public void mitgliedHinzufuegen(Person person, int id) {
        if(gruppenRepository.getfuerIdTransaktion(id) == null || gruppenRepository.getfuerIdTransaktion(id).isEmpty()){
            gruppenRepository.mitgliedHinzufuegen(person, id);
        }
    }

    public void transaktionHinzufuegen(Person sender, String empfaenger, double betrag, int id, String grund) {
        ArrayList<Person> empfaengerListe = empfaengerTeilen(empfaenger, id);
        gruppenRepository.transaktionHinzufuegen(id, betrag, sender, empfaengerListe, grund);
    }

    private ArrayList<Person> empfaengerTeilen(String empfaenger, int id) {
        String[] mitglieder = empfaenger.split(",");
        ArrayList<Person> empfaengerListe = new ArrayList<>();
        for (String s : mitglieder) {
            if (findByID(id).getMitgliederNamen().contains(s)) {
                empfaengerListe.add(findByID(id).getDurchNamen(s));
            }
        }
        return empfaengerListe;
    }

    public ArrayList<Ueberweisungsdetail> gruppeSchliessen(int id) {
        Gruppe gruppe = findByID(id);
        ArrayList<Ueberweisungsdetail> detail = new ArrayList<>();
        for (int i = 0; i < gruppe.ueberweisungenAngeben().size(); i++) {
            detail.add(new Ueberweisungsdetail(gruppe.getUeberweisungGeld(i), gruppe.getUeberweisungSender(i), gruppe.getUeberweisungEmpfaenger(i)));
        }
        gruppenRepository.gruppeSchliessen(id);
        return detail;
    }

    public ArrayList<Ueberweisungsdetail> getUeberweisungsdetail(int id) {
        Gruppe gruppe = findByID(id);
        boolean geschlossen = gruppe.getKeineAusgabe();
        gruppe.setKeineAusgabe(true);
        if (!gruppenRepository.getfuerIdTransaktion(id).isEmpty()) {
            gruppe.setMitglieder(gruppenRepository.getMitglieder(id));
            gruppe.setTransaktionen(gruppenRepository.getfuerIdTransaktion(id));
        }
        gruppe.setKeineAusgabe(geschlossen);
        gruppe.ueberweisungenAngeben();
        ArrayList<Ueberweisungsdetail> detail = new ArrayList<>();
        for (int i = 0; i < gruppe.ueberweisungenAngeben().size(); i++) {
            detail.add(new Ueberweisungsdetail(gruppe.getUeberweisungGeld(i), gruppe.getUeberweisungSender(i), gruppe.getUeberweisungEmpfaenger(i)));
        }
        return detail;

    }

    public void restUeberweisungen(int id, List<RestAusgleich> restAusgleich) {
        for (int i = 0; i < findByID(id).ueberweisungenAngeben().size(); i++) {
            Ueberweisungsdetail ueberweisung = getUeberweisungsdetail(id).get(i);
            restAusgleich.add(new RestAusgleich(ueberweisung.sender(), ueberweisung.empfaenger(), (int) ueberweisung.geld()));
        }
    }

    public void restTransaktionen(int id, RestAusgaben restAusgaben, double betrag) {
        for (int i = 0; i < restAusgaben.schuldner().size(); i++) {
            transaktionHinzufuegen(new Person(restAusgaben.glaeubiger()), restAusgaben.schuldner().get(i), (betrag), id, restAusgaben.grund());
        }
    }

    public List<RestGruppe> restGruppen(String name) {
        List<RestGruppe> restGruppe = new ArrayList<>();
        for (int i = 0; i < alleGruppen(name).size(); i++) {
            Gruppe gruppe = alleGruppen(name).get(i);
            String[] personen = new String[gruppe.getMitglieder().size()];
            personen[i] = gruppe.getMitglieder().get(i).getPersonName();
            restGruppe.add(new RestGruppe(gruppe.getId(), gruppe.getName(), personen, false, null));
        }
        return restGruppe;
    }

    public String[] personenSuchen(int id){
        String[] personen = new String[getMitgliederListe(id).size()];
        for (int i = 0; i < getMitgliederListe(id).size(); i++) {
            personen[i] = getMitgliederListe(id).get(i).getPersonName();
        }
        return personen;
    }
}
