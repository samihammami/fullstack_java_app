package de.propra.splitter.services;

import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.transaktion.Transaktion;
import de.propra.splitter.domain.person.Person;

import java.util.ArrayList;
import java.util.List;

public interface GruppenRepo {

    List<Gruppe> alleGruppen();

    void speichern(Gruppe gruppe);

    void mitgliedHinzufuegen(Person p, int id);

    Gruppe gruppeFinden(int id);

    void transaktionHinzufuegen(int id, double betrag, Person sender, ArrayList<Person> empfaenger, String grund);

    ArrayList<Transaktion> getfuerIdTransaktion(int id);

    ArrayList<Person> getMitglieder(int id);

    void gruppeSchliessen(int id);
}
