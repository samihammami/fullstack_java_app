package de.propra.splitter.services.restschnittstelle;

import java.util.List;

public record RestAusgaben(String grund, String glaeubiger, int cent, List<String> schuldner) {
}
