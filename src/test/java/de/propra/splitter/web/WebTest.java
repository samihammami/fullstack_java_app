package de.propra.splitter.web;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import de.propra.splitter.WithMockOAuth2User;
import de.propra.splitter.config.SecurityConfig;
import de.propra.splitter.domain.gruppe.Gruppe;
import de.propra.splitter.domain.person.Person;
import de.propra.splitter.services.SplitterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;

@WebMvcTest
@Import({SecurityConfig.class})
public class WebTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    SplitterService service;

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Hauptseite erreichbar")
    void test_01() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Gruppe kann erstellt werden")
    void test_02() throws Exception {
        mvc.perform(post("/").param("name", "Neue Gruppe").with(csrf()));
        verify(service,atLeast(1)).gruppeHinzufuegen("Neue Gruppe", "Bob");
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Ein Mitglied kann hinzugefuegt werden")
    void test_03() throws Exception {
        when(service.findByID(0)).thenReturn(new Gruppe(new ArrayList<>()));
        mvc.perform(post("/details/hinzufuegen").param("name", "Robert").param("id", "0").with(csrf()));
        verify(service,atLeast(1)).mitgliedHinzufuegen(any(Person.class), any(Integer.class));
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Mehrere Mitglieder koennen in unterschieliche Gruppen hinzugefuegt werden")
    void test_04() throws Exception {
        when(service.findByID(0)).thenReturn(new Gruppe(new ArrayList<>()));
        when(service.findByID(1)).thenReturn(new Gruppe(new ArrayList<>()));
        mvc.perform(post("/details/hinzufuegen").param("name", "Robert").param("id", "0").with(csrf()));
        mvc.perform(post("/details/hinzufuegen").param("name", "Heinrich").param("id", "1").with(csrf()));
        verify(service,atLeast(2)).mitgliedHinzufuegen(any(Person.class), any(Integer.class));
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Eine Transaktion wird hinzugefuegt")
    void test_05() throws Exception{
        when(service.findByID(0)).thenReturn(new Gruppe(new ArrayList<>(Arrays.asList(new Person("Bob")))));
        mvc.perform(post("/details/transaktion")
                .param("empfaenger", "Bob")
                .param("betrag", "20")
                .param("id", "0")
                .param("grund", "")
                .with(csrf()));
        verify(service).transaktionHinzufuegen(any(Person.class), anyString(), any(Double.class), anyInt(), anyString());
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Detailseite erreichbar")
    void test_06() throws Exception {
        when(service.findByID(0)).thenReturn(new Gruppe(new ArrayList<>(Arrays.asList(new Person("Bob")))));
        mvc.perform(get("/details").param("id", "0")).andExpect(status().isOk());
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Mehrere Mitglieder koennen hinzugefuegt werden")
    void test_07() throws Exception {
        when(service.findByID(0)).thenReturn(new Gruppe(new ArrayList<>()));
        mvc.perform(post("/details/hinzufuegen").param("name", "Robert").param("id", "0").with(csrf()));
        mvc.perform(post("/details/hinzufuegen").param("name", "Heinrich").param("id", "0").with(csrf()));
        verify(service,atLeast(2)).mitgliedHinzufuegen(any(Person.class), any(Integer.class));
    }

    @Test
    @WithMockOAuth2User(login="Bob")
    @DisplayName("Eine Gruppe kann geschlossen werden")
    void test_08() throws Exception{
        when(service.findByID(0)).thenReturn(new Gruppe(new ArrayList<>()));
        mvc.perform(post("/details/schliessen").param("id", "0").with(csrf()));
        verify(service).gruppeSchliessen(anyInt());
    }


}