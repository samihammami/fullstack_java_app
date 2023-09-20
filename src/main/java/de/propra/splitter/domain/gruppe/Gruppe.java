package de.propra.splitter.domain.gruppe;

import de.propra.splitter.domain.IllegalActionException;
import de.propra.splitter.domain.person.Person;
import de.propra.splitter.domain.transaktion.Transaktion;
import de.propra.splitter.stereotypes.AggregateRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@AggregateRoot
public class Gruppe {

    private ArrayList<Person> mitglieder;
    private ArrayList<Ueberweisung> ueberweisungen;
    private ArrayList<Transaktion> transaktionen;
    private HashMap<Person, Double> schuldenIndex;

    private int id;
    private String gruppenName = "";

    private boolean gruppeGeschlossen = false;

    private boolean keineAusgabe = true;

    public Gruppe(ArrayList<Person> mitglieder) {
        this.mitglieder = mitglieder;
        ueberweisungen = new ArrayList<>();
        transaktionen = new ArrayList<>();
        schuldenIndex = new HashMap<>();
    }

    public Gruppe(ArrayList<Person> mitglieder, String gruppenName, int id, boolean gruppeGeschlossen, boolean keineAusgabe) {
        this.mitglieder = mitglieder;
        this.id = id;
        ueberweisungen = new ArrayList<>();
        transaktionen = new ArrayList<>();
        schuldenIndex = new HashMap<>();
        this.gruppenName = gruppenName;
        this.gruppeGeschlossen = gruppeGeschlossen;
        this.keineAusgabe = keineAusgabe;
    }

    public String getGruppenName() {
        return gruppenName;
    }

    public ArrayList<Transaktion> getTransaktionen() {
        return transaktionen;
    }

    public String getName() {
        String namen = "";
        for (int i = 0; i < mitglieder.size(); i++) {
            namen += mitglieder.get(i).getPersonName() + "\n";
        }
        return namen;
    }

    public Person getDurchNamen(String n) {
        for (Person p : mitglieder) {
            if (p.getPersonName().equals(n)) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Person> getMitglieder() {
        return mitglieder;
    }

    public Person getMitgliederDurchName(String name) {
        for (Person p : mitglieder) {
            if (p.getPersonName().equals(name)) return p;
        }
        return null;
    }

    public ArrayList<String> getMitgliederNamen() {
        ArrayList<String> namen = new ArrayList<>();
        for (Person p : mitglieder) {
            namen.add(p.getPersonName());
        }
        return namen;
    }

    public void hinzufuegen(Person... a) {
        keineAusgabe = (transaktionen.size() == 0);
        if (keineAusgabe) {
            for (Person b : a) {
                if (!getMitgliederNamen().contains(b.getPersonName())) {
                    mitglieder.add(b);
                    schuldenIndex.put(b, 0.0);
                }
            }
        } else
            throw new IllegalActionException("Ausgabe wurde schon eingetragen");
    }

    public void entfernen(Person... a) {
        if (keineAusgabe) {
            for (Person b : a) {
                mitglieder.remove(b);
            }
        } else
            throw new IllegalActionException("Ausgabe wurde schon eingetragen");
    }

    public void geldAusgeben(Geld betrag, Person sender, ArrayList<Person> empfaenger, String grund) {
        if (!gruppeGeschlossen) {
            if (!schuldenIndex.containsKey(sender)) {
                schuldenIndex.put(sender, 0.0);
            }
            if (empfaenger.contains(sender)) {
                schuldenIndex.put(sender, schuldenIndex.get(sender) - (betrag.getBetrag() - (betrag.getBetrag() / empfaenger.size())));
            } else {
                schuldenIndex.put(sender, schuldenIndex.get(sender) - betrag.getBetrag());
            }
            for (Person p : empfaenger) {
                if (!getMitgliederNamen().contains(p.getPersonName())) {
                    throw new IllegalArgumentException("Empfaenger ist kein Mitglied");
                }
                schuldenIndex.putIfAbsent(p, 0.0);
                if (!sender.equals(p)) {
                    schuldenIndex.put(p, schuldenIndex.get(p) + (betrag.getBetrag() / empfaenger.size()));
                }
            }
            transaktionen.add(new Transaktion(betrag.getBetrag(), sender, empfaenger, grund, id));
            keineAusgabe = false;
        } else {
            throw new IllegalActionException("Gruppe ist schon geschlossen");
        }
    }

    public void geldAusgeben(double geld, Person sender, ArrayList<Person> empfaenger, String grund) {
        Geld betrag = new Geld(geld);
        if (!schuldenIndex.containsKey(sender)){
            schuldenIndex.put(sender, 0.0);
        }
        if (empfaenger.contains(sender)) {
            schuldenIndex.put(sender, schuldenIndex.get(sender) - (betrag.getBetrag() - (betrag.getBetrag() / empfaenger.size())));
        }
        else{
            schuldenIndex.put(sender, schuldenIndex.get(sender) - betrag.getBetrag());
        }
        for (Person p : empfaenger) {
            if (!getMitgliederNamen().contains(p.getPersonName())) {
                throw new IllegalArgumentException("Empfaenger ist kein Mitglied");
            }
            schuldenIndex.putIfAbsent(p, 0.0);
            if (!sender.equals(p))
                schuldenIndex.put(p, schuldenIndex.get(p) + (betrag.getBetrag() / empfaenger.size()));
        }
        transaktionen.add(new Transaktion(betrag.getBetrag(), sender, empfaenger, grund, id));
        keineAusgabe = false;
    }

    public HashMap<String, Double> gesamtAusgaben() {
        HashMap<String, Double> ausgaben = new HashMap<>();
        Double summe;
        for (Person person : mitglieder) {
            summe = 0.0;
            for (Transaktion transaktion : transaktionen) {
                if (transaktion.sender() == person) summe += transaktion.geld();
            }
            ausgaben.put(person.getPersonName(), summe);
        }
        return ausgaben;
    }

    private HashMap<Person, Double> schuldenBerechnen(Person person) {
        HashMap<Person, Double> schulden = new HashMap<>();
        for (int i = 0; i < transaktionen.size(); i++) {
            List<Person> empfaenger = transaktionen.get(i).empfaenger();
            Person sender = transaktionen.get(i).sender();
            schuldenAddieren(person, schulden, i, empfaenger, sender);
        }
        if (schulden.size() == 0) {
            return null;
        }
        return schulden;
    }

    public HashMap<Person, HashMap<Person, Double>> schuldenKatalog() {
        HashMap<Person, HashMap<Person, Double>> schulden = new HashMap<>();
        for (Person p : mitglieder) {
            if (schuldenBerechnen(p) != null) {
                schulden.put(p, schuldenBerechnen(p));
            }
        }
        if (schulden.size() == 0) {
            return null;
        }
        schulden = schuldenAusgleichen();
        for (Person p : schuldenIndex.keySet()) {
            schuldenIndex.put(p, ((int) (schuldenIndex.get(p) * 100)) / 100.0);
        }
        return schulden;
    }

    private void schuldenAddieren(Person person, HashMap<Person, Double> schulden, int i, List<Person> empfaenger, Person sender) {
        if (!person.getPersonName().equals(sender.getPersonName())) {
            if (empfaenger.contains(person)) {
                double bisherSchulden = 0;
                if (schulden.containsKey(sender)) {
                    bisherSchulden = schulden.get(sender);
                }
                schulden.put(sender, bisherSchulden + geldTeilen(transaktionen.get(i).geld(), empfaenger.size()));
            }
        }
    }

    private Double geldTeilen(Double transaktion, int size) {
        return (double) (Math.round((transaktion / size) * 100)) / 100.0;
    }

    public void gruppeSchliessen() {
        gruppeGeschlossen = true;
    }

    private HashMap<Person, HashMap<Person, Double>> schuldenAusgleichen() {
        HashMap<Person, HashMap<Person, Double>> s2 = new HashMap<>();
        for (Person p : mitglieder) {
            s2.put(p, new HashMap<>());
        }
        List<Person> l = getOrderedIndexes();
        for (Person p : mitglieder) {
            List<Person> klein = new LinkedList<>();
            for (Person b : l) {
                if (schuldenIndex.get(b) == null) {
                    continue;
                }
                if (schuldenIndex.get(b) < 0) {
                    klein.add(b);
                }
            }
            List<Person> gross = new LinkedList<>();
            for (Person b : l) {
                if (schuldenIndex.get(b) == null) {
                    continue;
                }
                if (schuldenIndex.get(b) > 0) {
                    gross.add(b);
                }
            }

            List<Person> finalerDurchlauf = null;
            if (schuldenIndex.get(p) != null && schuldenIndex.get(p) > 0) {
                finalerDurchlauf = sucheDurchlaufe(p, klein, finalerDurchlauf, false);
            }
            if (schuldenIndex.get(p) != null && schuldenIndex.get(p) < 0) {
                finalerDurchlauf = sucheDurchlaufe(p, gross, finalerDurchlauf, true);
            }
            if (finalerDurchlauf == null || finalerDurchlauf.size() == 0) {
                continue;
            }
            for (Person ignoriert : finalerDurchlauf) {
                if (schuldenIndex.get(p) != null && schuldenIndex.get(p) > 0) {
                    for (Person c : finalerDurchlauf) {
                        s2.get(p).put(c, -schuldenIndex.get(c));
                    }
                } else if (schuldenIndex.get(p) != null && schuldenIndex.get(p) < 0) {
                    for (Person c : finalerDurchlauf) {
                        s2.get(c).put(p, schuldenIndex.get(c));
                    }
                }
            }
        }
        return s2;
    }

    private List<Person> sucheDurchlaufe(Person p, List<Person> big, List<Person> finalerDurchlauf, boolean b) {
        int size = big.size();
        double h = schuldenIndex.get(p);
        List<Person> lokalerDurchlauf = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            for (Person person : big) {
                if ((h + schuldenIndex.get(person) > 0 || p.equals(person)) && b) {
                    continue;
                }
                if ((h + schuldenIndex.get(person) < 0 || p.equals(person)) && !b) {
                    continue;
                }
                h += schuldenIndex.get(person);
                lokalerDurchlauf.add(person);
            }
            if (durchlaufDimensionen(finalerDurchlauf, lokalerDurchlauf)) finalerDurchlauf = lokalerDurchlauf;
            if (h == 0) break;
            lokalerDurchlauf = new LinkedList<>();
            big.remove(big.get(0));
            h = schuldenIndex.get(p);
        }
        return finalerDurchlauf;
    }

    private static boolean durchlaufDimensionen(List<Person> finDurch, List<Person> lokDurch) {
        return finDurch == null || lokDurch.size() < finDurch.size();
    }

    private List<Person> getOrderedIndexes() {
        List<Person> l = new LinkedList<>();
        Person smallest = mitglieder.get(0);
        for (Person ignoriert : mitglieder) {
            for (Person b : mitglieder) {
                if (schuldenIndex.get(b) == null || schuldenIndex.get(smallest) == null) {
                    continue;
                }
                if (schuldenIndex.get(b) < schuldenIndex.get(smallest) && !l.contains(b)) smallest = b;
            }
            l.add(smallest);
            for (Person b : mitglieder) {
                if (!l.contains(b)) {
                    smallest = b;
                    break;
                }
            }
        }
        return l;
    }

    public ArrayList<Ueberweisung> ueberweisungenAngeben() {
        HashMap<Person, HashMap<Person, Double>> schulden = schuldenKatalog();
        ueberweisungen = new ArrayList<>();
        if (schulden == null) {
            return ueberweisungen;
        }
        for (Person p : schulden.keySet()) {
            for (Person b : schulden.get(p).keySet()) {
                ueberweisungen.add(new Ueberweisung(schulden.get(p).get(b), p, b));
            }
        }
        return ueberweisungen;
    }

    public double getUeberweisungGeld(int id) {
        return ueberweisungen.get(id).betrag();
    }

    public String getUeberweisungSender(int id) {
        return ueberweisungen.get(id).sender().getPersonName();
    }

    public String getUeberweisungEmpfaenger(int id) {
        return ueberweisungen.get(id).empfaenger().getPersonName();
    }

    public int getId() {
        return id;
    }

    public boolean getGruppeGeschlossen() {
        return gruppeGeschlossen;
    }

    public HashMap<Person, Double> getSchuldenIndex() {
        return schuldenIndex;
    }

    public void setTransaktionen(ArrayList<Transaktion> t) {
        Person mitglied = null;
        for (Transaktion tr : t) {
            ArrayList<Person> liste = new ArrayList<>();
            for (Person p : mitglieder) {
                if (p.getPersonName().equals(tr.sender().getPersonName())) {
                    mitglied = p;
                    break;
                }
            }
            for (Person p : tr.empfaenger()) {
                for (Person m : mitglieder) {
                    if (m.getPersonName().equals(p.getPersonName())) {
                        liste.add(m);
                    }
                }
            }
            geldAusgeben(tr.geld(), mitglied, liste, tr.grund());
        }
    }

    public void setMitglieder(ArrayList<Person> p) {
        for (Person m : p) {
            hinzufuegen(m);
        }
    }

    public boolean getKeineAusgabe() {
        return keineAusgabe;
    }

    public void setKeineAusgabe(boolean keineAusgabe){
        this.keineAusgabe = keineAusgabe;
    }
}