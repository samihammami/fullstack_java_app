package de.propra.splitter;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.conditions.ArchConditions.accessClassesThat;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.conditions.ArchConditions;
import de.propra.splitter.stereotypes.AggregateRoot;

@AnalyzeClasses(packagesOf = SplitterApplication.class, importOptions = ImportOption.DoNotIncludeTests.class)
public class Archunit {

    @ArchTest
    static final ArchRule onion = onionArchitecture()
            .domainModels("..domain..")
            .domainServices("..services..")
            .applicationServices("..services..")
            .adapter("web", "..web")
            .adapter("datenbanken", "..datenbanken");

    @ArchTest
    ArchRule controllerSollenServicesNutzen = classes()
            .that()
            .resideInAnyPackage("..web..")
            .should(ArchConditions.not(accessClassesThat(resideInAPackage("..datenbanken.."))));

    @ArchTest
    ArchRule nurGruppenSindPublic = classes()
            .that()
            .resideInAPackage("..domain.gruppe")
            .and()
            .arePublic()
            .should()
            .beAnnotatedWith(AggregateRoot.class);

    @ArchTest
    ArchRule nurPersonenSindPublic = classes()
            .that()
            .resideInAPackage("..domain.person")
            .and()
            .arePublic()
            .should()
            .beAnnotatedWith(AggregateRoot.class);

    @ArchTest
    ArchRule nurTransaktionenSindPublic = classes()
            .that()
            .resideInAPackage("..domain.transaktion")
            .and()
            .arePublic()
            .should()
            .beAnnotatedWith(AggregateRoot.class);
}
