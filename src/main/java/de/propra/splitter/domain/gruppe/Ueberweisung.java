package de.propra.splitter.domain.gruppe;

import de.propra.splitter.domain.person.Person;

record Ueberweisung(double betrag, Person sender, Person empfaenger) {

}
