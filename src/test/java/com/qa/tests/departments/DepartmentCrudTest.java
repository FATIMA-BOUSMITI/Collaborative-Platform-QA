package com.qa.tests.departments;

import com.qa.core.ConfigManager;
import com.qa.pages.DepartmentsPage;
import com.qa.tests.admin.AdminFeatureTestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Départements")
@Story("Cycle de vie via l'API")
class DepartmentCrudTest extends AdminFeatureTestBase {

    private DepartmentsPage departmentsPage;
    private final Set<String> testDepartmentNames = new LinkedHashSet<>();

    @Override
    protected boolean requiresIsolatedQaData() {
        return true;
    }

    @BeforeEach
    void openDepartments() {
        ConfigManager.assumeIsolatedQaData();
        departmentsPage = new DepartmentsPage(driver, waitDuration, baseUrl);
        departmentsPage.open();
    }

    @AfterEach
    void removeOnlyDepartmentsCreatedByThisTest() {
        if (departmentsPage == null) {
            return;
        }
        for (String name : testDepartmentNames) {
            departmentsPage.deleteIfPresent(name);
        }
    }

    @Test
    @Tag("e2e")
    @Tag("qa-data")
    @Description("Crée, modifie, relit puis supprime un département unique dans la base QA isolée.")
    void departmentCanBeCreatedUpdatedReloadedAndDeleted() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 3)
                .toUpperCase();
        String initialName = "Q" + suffix + " QA department";
        String updatedName = initialName + " updated";
        testDepartmentNames.add(initialName);
        testDepartmentNames.add(updatedName);

        departmentsPage.create(initialName);
        assertThat(departmentsPage.contains(initialName)).isTrue();

        departmentsPage.rename(initialName, updatedName);
        assertThat(departmentsPage.contains(updatedName)).isTrue();

        departmentsPage.open();
        assertThat(departmentsPage.contains(updatedName))
                .as("The department update must be persisted by the backend")
                .isTrue();

        departmentsPage.deleteIfPresent(updatedName);
        assertThat(departmentsPage.contains(updatedName)).isFalse();
        departmentsPage.open();
        assertThat(departmentsPage.contains(updatedName))
                .as("The department delete must be persisted by the backend")
                .isFalse();
        testDepartmentNames.clear();
    }
}
