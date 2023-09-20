package de.propra.splitter.datenbanken;


import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.transaktion.Transaktion;
import de.propra.splitter.domain.person.Person;
import de.propra.splitter.services.GruppenRepo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GruppenImpl implements GruppenRepo {

    GruppenRepository repo;
    TransaktionRepository tRepo;

    public GruppenImpl(GruppenRepository gruppenRepo, TransaktionRepository tRepo) {
        this.repo = gruppenRepo;
        this.tRepo = tRepo;
    }

    public List<Gruppe> alleGruppen() {
        List<GruppeEntity> list = repo.findAll();
        List<Gruppe> gruppe = list.stream().map(this::toGruppe).toList();
        return gruppe;
    }

    private Gruppe toGruppe(GruppeEntity gruppeE) {
        ArrayList<Person> domainPersonen = new ArrayList<>();
        for (String g : gruppeE.mitglieder()) {
            domainPersonen.add(new Person(g));
        }
        Gruppe gruppe = new Gruppe(domainPersonen, gruppeE.gruppenName(), gruppeE.gruppenId(), gruppeE.geschlossen(), gruppeE.ausgabe());
        return gruppe;
    }

    @Override
    public void speichern(Gruppe gruppe) {
        String[] namen = new String[gruppe.getMitgliederNamen().size()];
        for (int i = 0; i< gruppe.getMitgliederNamen().size(); i++) {
            namen[i] = gruppe.getMitgliederNamen().get(i);
        }

        GruppeEntity g = new GruppeEntity(null, gruppe.getId(), gruppe.getGruppenName(), gruppe.getGruppeGeschlossen(), namen, gruppe.getKeineAusgabe());
        repo.save(g);
    }

    public void mitgliedHinzufuegen(Person person, int id){
        Gruppe gruppe = gruppeFinden(id);
        if(gruppe == null){
            return;
        }
        gruppe.hinzufuegen(person);
        update(id, gruppe);
    }

    public Gruppe gruppeFinden(int id) {
        List<Gruppe> gruppen = alleGruppen();
        Gruppe gruppe = null;
        for (Gruppe g: gruppen) {
            if (g.getId() == id){
                gruppe = g;
            }
        }
        return gruppe;
    }

    private void update(int id, Gruppe gruppe){
        List<GruppeEntity> gruppeE = repo.findAll();
        for (GruppeEntity g: gruppeE) {
            if(g.gruppenId() == id){
                repo.delete(g);
            }
        }
        speichern(gruppe);
    }

    public void transaktionHinzufuegen(int id, double betrag, Person sender, ArrayList<Person> empfaenger, String grund){
        Gruppe gruppe = gruppeFinden(id);
        if(gruppe == null){
            return;
        }
        if(gruppe.getGruppeGeschlossen()){
            return;
        }
        String[] namen = new String[empfaenger.size()];
        for(int i = 0; i < empfaenger.size(); i++){
            namen[i] = empfaenger.get(i).getPersonName();
        }
        TransaktionEntity t = new TransaktionEntity(null, id, sender.getPersonName(),  namen, betrag, grund);
        gruppe.setKeineAusgabe(false);
        update(id, gruppe);
        tRepo.save(t);
    }

    public ArrayList<Transaktion> getfuerIdTransaktion(int id){
        ArrayList<Transaktion> tr = new ArrayList<>();
        for (TransaktionEntity t: tRepo.findAll()) {
            ArrayList<Person> p = new ArrayList<>();
            if(t.transaktionenId()==id){
                for (String s: t.empfaenger()) {
                    p.add(new Person(s));
                }
                tr.add(new Transaktion( (int)(t.betrag() * 100) / 100.0, new Person(t.sender()), p, t.grund(), t.transaktionenId()));
            }
        }
        return tr;
    }

    public ArrayList<Person> getMitglieder(int id){
        Gruppe g = gruppeFinden(id);
        if(g == null){
            return null;
        }
        return g.getMitglieder();
    }

    public void gruppeSchliessen(int id){
        Gruppe gruppe = gruppeFinden(id);
        if(gruppe == null){
            return;
        }
        gruppe.gruppeSchliessen();
        update(id, gruppe);
    }
}
