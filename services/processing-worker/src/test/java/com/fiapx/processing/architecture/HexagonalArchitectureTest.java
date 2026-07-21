package com.fiapx.processing.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the Hexagonal Architecture boundaries described in docs/LLD/shared-architecture.md and
 * docs/LLD/processing-worker.md. Fails the build (via the normal {@code test} task) on violation.
 */
@AnalyzeClasses(packages = "com.fiapx.processing")
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_jpa =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("jakarta.persistence..", "org.hibernate..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_dtos =
            noClasses()
                    .that()
                    .resideInAPackage("..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application.dto..", "..api.request..", "..api.response..");

    @ArchTest
    static final ArchRule application_should_not_depend_on_infrastructure =
            noClasses()
                    .that()
                    .resideInAPackage("..application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_or_adapters_directly =
            noClasses()
                    .that()
                    .resideInAPackage("..api.controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..infrastructure.repository..", "..infrastructure.adapter.out..")
                    .allowEmptyShould(true);

    // The Spring @Configuration composition root is the one place explicitly
    // allowed to reference concrete adapters/JPA repositories directly - its
    // entire job is wiring adapters to ports.
    @ArchTest
    static final ArchRule only_infrastructure_or_wiring_may_use_jpa_repositories =
            noClasses()
                    .that()
                    .resideOutsideOfPackage("..infrastructure..")
                    .and()
                    .resideOutsideOfPackage("..configuration..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..infrastructure.repository..");

    // Excludes the scaffold's PackageMarker classes - placeholders used only
    // to keep otherwise-empty package directories tracked by git, not real adapters.
    @ArchTest
    static final ArchRule outbound_adapters_should_implement_a_port =
            classes()
                    .that()
                    .resideInAPackage("..infrastructure.adapter.out..")
                    .and()
                    .areNotInterfaces()
                    .and()
                    .areNotRecords()
                    .and(not(JavaClass.Predicates.simpleName("PackageMarker")))
                    .should()
                    .implement(JavaClass.Predicates.resideInAPackage("..application.ports.out.."))
                    .allowEmptyShould(true);

    // "Configuration" is the Spring composition root: it wires concrete
    // adapters to ports, so it is allowed to see every layer, but no layer
    // is allowed to depend back on it.
    @ArchTest
    static final ArchRule dependencies_should_point_inward =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Domain")
                    .definedBy("..domain..")
                    .layer("Application")
                    .definedBy("..application..")
                    .layer("Infrastructure")
                    .definedBy("..infrastructure..")
                    .layer("Api")
                    .definedBy("..api..")
                    .layer("Configuration")
                    .definedBy("..configuration..")
                    .whereLayer("Configuration")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Api")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Infrastructure")
                    .mayOnlyBeAccessedByLayers("Api", "Configuration")
                    .whereLayer("Application")
                    .mayOnlyBeAccessedByLayers("Api", "Infrastructure", "Configuration")
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers(
                            "Application", "Infrastructure", "Api", "Configuration");
}
