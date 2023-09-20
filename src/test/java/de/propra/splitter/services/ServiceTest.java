package de.propra.splitter.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.transaktion.Transaktion;
import de.propra.splitter.domain.person.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServiceTest {

    GruppenRepo repo = mock(GruppenRepo.class);

    SplitterService splitterService = new SplitterService(repo);

    @Test
    @DisplayName("Eine Gruppe kann hinzugefuegt werden")
    void test_01() {
        splitterService.gruppeHinzufuegen("Neue Gruppe", "Bob");
        verify(repo).speichern(any(Gruppe.class));
    }

    @Test
    @DisplayName("Mehrere Gruppen koennen hinzugefuegt werden")
    void test_02() {
        splitterService.gruppeHinzufuegen("Neue Gruppe", "Bob");
        splitterService.gruppeHinzufuegen("Neue Gruppe", "Bob");
        verify(repo, times(2)).speichern(any(Gruppe.class));
    }

    @Test
    @DisplayName("Eine Transaktion kann getaetigt werden")
    void test_03() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(List.of(new Gruppe(new ArrayList<>(List.of(new Person("Bob")))))));
        when(repo.getfuerIdTransaktion(0)).thenReturn(new ArrayList<>(List.of(new Transaktion(10, new Person("Bob"), new ArrayList<>(List.of(new Person("Hans"))), "ja", 0))));
        splitterService.gruppeHinzufuegen("Neue Gruppe", "Bob");
        splitterService.transaktionHinzufuegen(new Person("Bob"), "Bob", 20.0, 0, "");
        verify(repo, atLeastOnce()).transaktionHinzufuegen(anyInt(), anyDouble(), any(), any(), anyString());
    }

    @Test
    @DisplayName("Ein Mitglied kann hinzugefuegt werden")
    void test_04() {
        when(repo.gruppeFinden(0)).thenReturn((new Gruppe(new ArrayList<>(Arrays.asList(new Person("Bob"), new Person("Hans"))))));
        when(repo.getfuerIdTransaktion(anyInt())).thenReturn(null);
        splitterService.gruppeHinzufuegen("Neue Gruppe", "Bob");
        splitterService.mitgliedHinzufuegen(new Person("Hans"), 0);
        verify(repo, atLeastOnce()).mitgliedHinzufuegen(any(), anyInt());
    }

    @Test
    @DisplayName("Schulden werden angeben")
    void test_05() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(List.of(new Gruppe(new ArrayList<>(Arrays.asList(new Person("Bob"), new Person("Hans")))))));
        when(repo.getfuerIdTransaktion(0)).thenReturn(new ArrayList<>(List.of(new Transaktion(20.0, new Person("Bob"), new ArrayList<>(List.of(new Person("Hans"))), "", 0))));
        splitterService.gruppeHinzufuegen("Neue Gruppe", "Bob");
        splitterService.mitgliedHinzufuegen(new Person("Hans"), 0);
        splitterService.transaktionHinzufuegen(splitterService.findByID(0).getMitglieder().get(0), "Hans", 20.0, 0, "");
        assertThat(splitterService.getUeberweisungsdetail(0).get(0).geld()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Eine neue Gruppe hat einen einzigen Mitglied")
    void test_6() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(List.of(new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, false, true))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        List<Person> personList = splitterService.findByID(0).getMitglieder();
        assertThat(personList).hasSize(1);
    }

    @Test
    @DisplayName("Eine Gruppe kann geschlossen werden")
    void test_7() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(List.of(new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, false, true))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        Gruppe gruppe = splitterService.findByID(0);
        gruppe.gruppeSchliessen();
        assertThat(gruppe.getGruppeGeschlossen()).isTrue();
    }

    @Test
    @DisplayName("Transaktionen werden richtig gespeichert")
    void test_8() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(List.of(new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, false, true))));
        when(repo.getfuerIdTransaktion(0)).thenReturn(new ArrayList<>(List.of(new Transaktion(10, new Person("Bob"), new ArrayList<>(List.of(new Person("Hans"))), "ja", 0))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        Gruppe gruppe = splitterService.findByID(0);
        gruppe.hinzufuegen(new Person("Hans"));
        Person person = gruppe.getDurchNamen("Bob");
        Person person2 = gruppe.getDurchNamen("Hans");
        gruppe.geldAusgeben(10.0, person, new ArrayList<>(List.of(person2)), "ja");
        assertThat(splitterService.getTransaktionen(0).get(0).geld()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Ueberweisungen werden richtig gespeichert")
    void test_9() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(List.of(new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, false, true))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        Gruppe gruppe = splitterService.findByID(0);
        gruppe.hinzufuegen(new Person("Hans"));
        Person person = gruppe.getDurchNamen("Bob");
        Person person2 = gruppe.getDurchNamen("Hans");
        gruppe.geldAusgeben(10.0, person, new ArrayList<>(List.of(person2)), "ja");
        gruppe.gruppeSchliessen();
        assertThat(splitterService.getUeberweisungsdetail(0).get(0).geld()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Es werden nur die eigenen Gruppen angezeigt")
    void test_10() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(Arrays.asList(
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, false, true),
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test2", 1, false, true),
                new Gruppe(new ArrayList<>(List.of(new Person("Hans"))), "Test3", 2, false, true))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        splitterService.gruppeHinzufuegen("Test2", "Bob");
        splitterService.gruppeHinzufuegen("Test3", "Hans");
        assertThat(splitterService.alleGruppen("Bob").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Es werden die geschlossenen Gruppen richtig angegeben")
    void test_11() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(Arrays.asList(
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, true, true),
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test2", 1, true, true),
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test3", 2, false, true))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        splitterService.gruppeHinzufuegen("Test2", "Bob");
        splitterService.gruppeHinzufuegen("Test3", "Bob");
        splitterService.gruppeSchliessen(0);
        splitterService.gruppeSchliessen(1);
        assertThat(splitterService.alleGeschlossenenGruppen("Bob").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Es werden die offene Gruppen richtig angegeben")
    void test_12() {
        when(repo.alleGruppen()).thenReturn(new ArrayList<>(Arrays.asList(
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test", 0, true, true),
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test2", 1, false, true),
                new Gruppe(new ArrayList<>(List.of(new Person("Bob"))), "Test3", 2, false, true))));
        splitterService.gruppeHinzufuegen("Test", "Bob");
        splitterService.gruppeHinzufuegen("Test2", "Bob");
        splitterService.gruppeHinzufuegen("Test3", "Bob");
        splitterService.gruppeSchliessen(0);
        assertThat(splitterService.alleOffenenGruppen("Bob").size()).isEqualTo(2);
    }


}
