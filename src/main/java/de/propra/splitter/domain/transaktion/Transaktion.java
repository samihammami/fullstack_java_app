package de.propra.splitter.domain.transaktion;

import de.propra.splitter.domain.person.Person;
import de.propra.splitter.stereotypes.AggregateRoot;

import java.util.ArrayList;
import java.util.List;

@AggregateRoot
public record Transaktion(double geld, Person sender, ArrayList<Person> empfaenger, String grund, int id) {

    public String alleEmpfaenger(){
        String s="";
        for (Person p: empfaenger) {
            s += p.getPersonName() + " ";
        }
        return s;
    }

    public List<String> alleNamen(){
        List<String> namen = new ArrayList<>();
        for (Person p: empfaenger) {
            namen.add(p.getPersonName());
        }
        return namen;
    }
}
