package de.propra.splitter.domain.person;

import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.stereotypes.AggregateRoot;

import java.util.ArrayList;
import java.util.Arrays;

@AggregateRoot
public class Person {

    private final String personName;

    public Person(String name) {
        this.personName = name;
    }

    public String getPersonName() {
        return personName;
    }

    public Gruppe gruppeErstellen(){
        return new Gruppe(new ArrayList<>(Arrays.asList(this)));
    }

    public Gruppe gruppeErstellen(String gruppeName, int id){
        return new Gruppe(new ArrayList<>(Arrays.asList(this)),gruppeName, id, false, true);
    }
}
