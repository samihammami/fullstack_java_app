package de.propra.splitter.domain.gruppe;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.propra.splitter.domain.IllegalActionException;
import de.propra.splitter.domain.person.Person;
import de.propra.splitter.domain.transaktion.Transaktion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DomainTest {

    @Test
    @DisplayName("Nutzer kann Gruppe erstellen")
    void test_00() {
        Person person = new Person("Bob");
        Gruppe gruppe = person.gruppeErstellen();
        ArrayList<Person> mitglieder = gruppe.getMitglieder();
        assertThat(mitglieder.get(0).getPersonName()).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Nutzer kann Mitglieder hinzufuegen")
    void test_01() {
        Person bob = new Person("Bob");
        Person herbert = new Person("Herbert");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(herbert);
        ArrayList<Person> mitglieder = gruppe.getMitglieder();
        assertThat(mitglieder.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Geld kann ausgegeben werden")
    void test_02() {
        Person person = new Person("Bob");
        ArrayList<Person> leer = new ArrayList<>();
        Gruppe gruppe = person.gruppeErstellen();
        gruppe.geldAusgeben(new Geld(0.99), person, leer, "");
        ArrayList<Transaktion> transaktionen = gruppe.getTransaktionen();
        assertThat(transaktionen.get(0).geld()).isEqualTo(0.99);
    }

    @Test
    @DisplayName("Betraege koennen addiert werden")
    void test_03() {
        Person bob = new Person("Bob");
        ArrayList<Person> leer = new ArrayList<>();
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.geldAusgeben(new Geld(0.99), bob, leer, "");
        gruppe.geldAusgeben(new Geld(0.99), bob, leer, "");
        HashMap<String, Double> ausgaben = gruppe.gesamtAusgaben();
        assertThat(ausgaben.get("Bob")).isEqualTo(1.98);
    }

    @Test
    @DisplayName("Schulden werden berechnet bei einer Person")
    void test_04() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);
        gruppe.geldAusgeben(new Geld(5.00), bob, new ArrayList<>(List.of(heinrich)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(heinrich).get(bob)).isEqualTo(5.00);
    }

    @Test
    @DisplayName("Schulden werden berechnet bei 2 Personen")
    void test_05() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Person fridolin = new Person("Fridolin");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich,fridolin);
        gruppe.geldAusgeben(new Geld(5.00), bob, new ArrayList<>(Arrays.asList(heinrich, fridolin)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(heinrich).get(bob)).isEqualTo(2.50);
    }

    @Test
    @DisplayName("Schulden werden berechnet bei 2 Personen ohne Geschenk")
    void test_06() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);
        gruppe.geldAusgeben(new Geld(6.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(heinrich).get(bob)).isEqualTo(3.00);
    }

    @Test
    @DisplayName("Man kann sich nichts selber schulden")
    void test_07() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);
        gruppe.geldAusgeben(new Geld(6.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(bob).get(bob)).isNull();
    }

    @Test
    @DisplayName("Schulden werden addiert")
    void test_08() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);
        gruppe.geldAusgeben(new Geld(6.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        gruppe.geldAusgeben(new Geld(6.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(heinrich).get(bob)).isEqualTo(6);
    }

    @Test
    @DisplayName("Geld kann nicht an fremde uebergeben werden")
    void test_09() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        boolean thrown = false;
        Gruppe gruppe = bob.gruppeErstellen();
        try {
            gruppe.geldAusgeben(new Geld(6.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        assertThat(thrown).isTrue();
    }

    @Test
    @DisplayName("Schulden werden ausgeglichen")
    void test_10() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);
        gruppe.geldAusgeben(new Geld(10.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        gruppe.geldAusgeben(new Geld(6.00), heinrich, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(heinrich).get(bob)).isEqualTo(2);
        assertThat(s.get(bob).get(heinrich)).isNull();
    }

    @Test
    @DisplayName("Schulden werden ausgeglichen bei vielen Beiträgen")
    void test_11() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");

        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);

        gruppe.geldAusgeben(new Geld(12.00), bob, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        gruppe.geldAusgeben(new Geld(12.00), heinrich, new ArrayList<>(Arrays.asList(heinrich, bob)), "");
        gruppe.geldAusgeben(new Geld(12.00), heinrich, new ArrayList<>(Arrays.asList(heinrich, bob)), "");

        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();

        assertThat(s.get(heinrich).get(bob)).isNull();
        assertThat(s.get(bob).get(heinrich)).isEqualTo(6.0);
    }

    @Test
    @DisplayName("Schulden werden komplett ausgeglichen bei vielen Beiträgen")
    void test_12() {
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Person fridolin = new Person("Fridolin");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich, fridolin);

        gruppe.geldAusgeben(new Geld(10.00), heinrich, new ArrayList<>(List.of(bob)), "");
        gruppe.geldAusgeben(new Geld(20.00), fridolin, new ArrayList<>(List.of(heinrich)), "");
        gruppe.geldAusgeben(new Geld(10.00), bob, new ArrayList<>(List.of(fridolin)), "");

        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();

        assertThat(s.get(bob).get(heinrich)).isNull();
        assertThat(s.get(fridolin).get(bob)).isNull();
        assertThat(s.get(heinrich).get(fridolin)).isEqualTo(10.0);
    }

    @Test
    @DisplayName("keine neue Transaktion nach Schliessung der Gruppe")
    void test_13() {
        boolean thrown = false;
        Person bob = new Person("Bob");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.gruppeSchliessen();

        try {
            gruppe.geldAusgeben(new Geld(10.00), bob, new ArrayList<>(List.of(bob)), "");
        } catch (IllegalActionException e) {
            thrown = true;
        }
        assertThat(thrown).isTrue();
    }

    @Test
    @DisplayName("keine neue Personen nach Eintragen einer Ausgabe")
    void test_14() {
        boolean thrown = false;
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.geldAusgeben(new Geld(10.00), bob, new ArrayList<>(List.of(bob)), "");

        try {
            gruppe.hinzufuegen(heinrich);
        } catch (IllegalActionException e) {
            thrown = true;
        }
        assertThat(thrown).isTrue();
    }

    @Test
    @DisplayName("Testszenario 7 mit Minimierung")
    void test_15(){
        Person a=new Person("A");
        Person b=new Person("B");
        Person c=new Person("C");
        Person d=new Person("D");
        Person e=new Person("E");
        Person f=new Person("F");
        Person g=new Person("G");
        Gruppe gruppe =a.gruppeErstellen();
        gruppe.hinzufuegen(b,c,d,e,f,g);
        gruppe.geldAusgeben(new Geld(20.00), d, new ArrayList<>(Arrays.asList(d,f)), "");
        gruppe.geldAusgeben(new Geld(10.00), g, new ArrayList<>(List.of(b)), "");
        gruppe.geldAusgeben(new Geld(75.00), e, new ArrayList<>(Arrays.asList(a,c,e)), "");
        gruppe.geldAusgeben(new Geld(50.00), f, new ArrayList<>(Arrays.asList(a,f)), "");
        gruppe.geldAusgeben(new Geld(40.00), e, new ArrayList<>(List.of(d)), "");
        gruppe.geldAusgeben(new Geld(40.00), f, new ArrayList<>(Arrays.asList(b,f)), "");
        gruppe.geldAusgeben(new Geld(5.00), f, new ArrayList<>(List.of(c)), "");
        gruppe.geldAusgeben(new Geld(30.00), g, new ArrayList<>(List.of(a)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        assertThat(s.get(a).get(f)).isEqualTo(40.0);
        assertThat(s.get(a).get(g)).isEqualTo(40.0);
        assertThat(s.get(b).get(e)).isEqualTo(30.0);
        assertThat(s.get(c).get(e)).isEqualTo(30.0);
        assertThat(s.get(d).get(e)).isEqualTo(30.0);
    }

    @Test
    @DisplayName("Testszenario 6")
    void test_16(){
        Person a = new Person("A");
        Person b = new Person("B");
        Person c = new Person("C");
        Person d = new Person("D");
        Person e = new Person("E");
        Person f = new Person("F");
        Gruppe gruppe =a.gruppeErstellen();
        gruppe.hinzufuegen(b,c,d,e,f);
        gruppe.geldAusgeben(new Geld(564.00), a, new ArrayList<>(Arrays.asList(a,b,c,d,e,f)), "");
        gruppe.geldAusgeben(new Geld(38.58), b, new ArrayList<>(Arrays.asList(a,b)), "");
        gruppe.geldAusgeben(new Geld(38.58), b, new ArrayList<>(Arrays.asList(a,b,d)), "");
        gruppe.geldAusgeben(new Geld(82.11), c, new ArrayList<>(Arrays.asList(c,e,f)), "");
        gruppe.geldAusgeben(new Geld(96.00), d, new ArrayList<>(Arrays.asList(d,a,b,c,e,f)), "");
        gruppe.geldAusgeben(new Geld(95.37), f, new ArrayList<>(Arrays.asList(b,e,f)), "");
        HashMap<Person, HashMap<Person, Double>> s = gruppe.schuldenKatalog();
        HashMap<Person, Double> schuldenIndex = gruppe.getSchuldenIndex();
        assertThat(schuldenIndex.get(b)).isEqualTo(96.78);
        assertThat(schuldenIndex.get(c)).isEqualTo(55.26);
        assertThat(schuldenIndex.get(d)).isEqualTo(26.86);
        assertThat(schuldenIndex.get(e)).isEqualTo(169.16);
        assertThat(schuldenIndex.get(f)).isEqualTo(73.79);
    }

    @Test
    @DisplayName("Nutzer kann Gruppe verlassen")
    void test_17() {
        Person person = new Person("Bob");
        Gruppe gruppe = person.gruppeErstellen();
        gruppe.entfernen(person);
        ArrayList<Person> mitglieder = gruppe.getMitglieder();
        assertThat(mitglieder.size()).isZero();
    }

    @Test
    @DisplayName("Nach Eintragen einer Ausgabe kann niemand verlassen")
    void test_18() {
        boolean thrown = false;
        Person bob = new Person("Bob");
        Person heinrich = new Person("Heinrich");
        Gruppe gruppe = bob.gruppeErstellen();
        gruppe.hinzufuegen(heinrich);
        gruppe.geldAusgeben(new Geld(10.00), bob, new ArrayList<>(List.of(bob)), "");

        try {
            gruppe.entfernen(heinrich);
        } catch (IllegalActionException e) {
            thrown = true;
        }
        assertThat(thrown).isTrue();
    }
}


