package com.universidad.votaciones;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.universidad.votaciones", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArquitecturaTest {
    // 1. El dominio es Java puro
    @ArchTest
    static final ArchRule dominioPuro = noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..infraestructura..");
    // 2. La aplicación (puertos y casos de uso) tampoco conoce la tecnología
    @ArchTest
    static final ArchRule aplicacionSinTecnologia = noClasses()
            .that().resideInAPackage("..aplicacion..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "..infraestructura..");
    // 3. Los adaptadores no se llaman entre sí
    @ArchTest
    static final ArchRule adaptadoresNoSeLlaman = noClasses()
            .that().resideInAPackage("..infraestructura.entrada..")
            .should().dependOnClassesThat().resideInAPackage("..infraestructura.salida..");

    // 4. La entrada solo entra por los puertos de entrada
    @ArchTest
    static final ArchRule entradaNoUsaPuertosDeSalida = noClasses()
            .that().resideInAPackage("..infraestructura.entrada..")
            .should().dependOnClassesThat().resideInAPackage("..puertos.salida..");
}
