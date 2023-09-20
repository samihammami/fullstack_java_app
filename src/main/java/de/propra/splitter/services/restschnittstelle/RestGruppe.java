package de.propra.splitter.services.restschnittstelle;

import java.util.List;

public record RestGruppe(Integer gruppe, String name, String[] personen, boolean geschlossen, List<RestAusgaben> ausgaben) {
}
