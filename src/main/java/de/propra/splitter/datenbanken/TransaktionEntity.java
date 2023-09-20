package de.propra.splitter.datenbanken;

import org.springframework.data.annotation.Id;

public record TransaktionEntity(@Id Integer id, Integer transaktionenId, String sender, String[] empfaenger, Double betrag, String grund) {
}
