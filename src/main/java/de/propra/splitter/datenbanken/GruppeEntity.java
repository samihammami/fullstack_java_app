package de.propra.splitter.datenbanken;

import org.springframework.data.annotation.Id;

import java.util.List;


public record GruppeEntity(@Id Integer id, Integer gruppenId, String gruppenName, boolean geschlossen, String[] mitglieder, boolean ausgabe) {
}
