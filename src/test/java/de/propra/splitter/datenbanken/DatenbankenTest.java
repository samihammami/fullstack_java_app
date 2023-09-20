package de.propra.splitter.datenbanken;

import static org.mockito.Mockito.*;

import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.person.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatenbankenTest {

    GruppenRepository gruppenRepository = mock(GruppenRepository.class);
    TransaktionRepository transaktionRepository = mock(TransaktionRepository.class);

    @Test
    @DisplayName("Eine Gruppe kann gespeichert werden")
    void test_01(){
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        Gruppe gruppe = new Gruppe(new ArrayList<>(Arrays.asList(new Person("Bob"), new Person("Hans"))), "test", 0, false, true);
        gruppenImpl.speichern(gruppe);
        verify(gruppenRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Mitglieder werden gespeichert")
    void test_02(){
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        Gruppe gruppe = new Gruppe(new ArrayList<>(Arrays.asList(new Person("Bob"), new Person("Hans"))), "test", 0, false, true);
        gruppenImpl.speichern(gruppe);
        verify(gruppenRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Eine Transaktion kann gespeichert werden")
    void test_03() {
        when(gruppenRepository.findAll()).thenReturn(Collections.singletonList(new GruppeEntity(null, 0, "test", false, new String[]{"Bob", "Hans"}, true)));
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        gruppenImpl.transaktionHinzufuegen(0, 20.0, new Person("Bob"), new ArrayList<>(List.of(new Person("Hans"))), "");
        verify(transaktionRepository, atLeastOnce()).save(any());
    }
}
