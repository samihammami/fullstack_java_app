package de.propra.splitter.datenbanken;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.person.Person;
import de.propra.splitter.domain.transaktion.Transaktion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;

@DataJdbcTest
@ActiveProfiles("test")
public class TechnischeDatenbankTests {

    @Autowired
    GruppenRepository gruppenRepository;

    @Autowired
    TransaktionRepository transaktionRepository;

    @Test
    @DisplayName("Gruppe wird zurueckgegeben")
    @Sql({"classpath:sql/Tabellen.sql", "classpath:sql/GruppenInsert.sql"})
    void test_01() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        Gruppe gruppe = gruppenImpl.gruppeFinden(0);
        assertThat(gruppe.getGruppenName()).isEqualTo("test");
    }

    @Test
    @DisplayName("Alle Gruppe werden zurueckgegeben")
    @Sql({"classpath:sql/GruppenInsert.sql"})
    void test_02() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        List<Gruppe> gruppe = gruppenImpl.alleGruppen();
        assertThat(gruppe.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mitglieder werden ausgelesen")
    @Sql({"classpath:sql/GruppenInsert.sql"})
    void test_03() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        ArrayList<Person> mitglieder = gruppenImpl.getMitglieder(0);
        assertThat(mitglieder.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Transaktionen werden ausgelesen")
    @Sql({"classpath:sql/GruppenInsert.sql", "classpath:sql/TransaktionInsert.sql"})
    void test_04() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        ArrayList<Transaktion> transaktions = gruppenImpl.getfuerIdTransaktion(0);
        assertThat(transaktions.size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Betraege werden richtig gespeichert")
    @Sql({"classpath:sql/GruppenInsert.sql", "classpath:sql/TransaktionInsert.sql"})
    void test_05() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        ArrayList<Transaktion> transaktions = gruppenImpl.getfuerIdTransaktion(0);
        assertThat(transaktions.get(0).geld()).isEqualTo(20.0);
        assertThat(transaktions.get(1).geld()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Gruppe kann geschlossen werden")
    @Sql("classpath:sql/GruppenInsert.sql")
    void test_06() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        gruppenImpl.gruppeSchliessen(0);
        assertThat(gruppenImpl.gruppeFinden(0).getGruppeGeschlossen()).isTrue();
    }

    @Test
    @DisplayName("Mitglied wird hinzugefuegt")
    @Sql({"classpath:sql/GruppenInsert.sql"})
    void test_07() {
        GruppenImpl gruppenImpl = new GruppenImpl(gruppenRepository, transaktionRepository);
        gruppenImpl.mitgliedHinzufuegen(new Person("Herbert"), 0);
        ArrayList<Person> mitglieder = gruppenImpl.getMitglieder(0);
        assertThat(mitglieder.size()).isEqualTo(3);
    }
}
