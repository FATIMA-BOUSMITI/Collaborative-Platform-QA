package com.qa.tests.users;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Gestion des utilisateurs")
@Story("Recherche des utilisateurs")
class UserSearchTest extends UserManagementTestBase {

    @Test
    @Tag("regression")
    @Tag("e2e")
    @Description("La recherche par email retrouve le compte QA créé via l'interface.")
    void searchFindsCreatedUserByEmail() {
        String email = createTestUser();

        usersPage.searchFor(email);

        assertThat(usersPage.getVisibleUserEmails()).containsExactly(email);
    }
}
