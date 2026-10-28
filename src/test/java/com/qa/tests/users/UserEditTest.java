package com.qa.tests.users;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Gestion des utilisateurs")
@Story("Modification des utilisateurs")
class UserEditTest extends UserManagementTestBase {

    @Test
    @Tag("regression")
    @Tag("e2e")
    @Description("La modification du prénom d'un utilisateur QA est conservée après actualisation.")
    void editedUserIsPersistedAfterRefresh() {
        String email = createTestUser();

        usersPage.openEditUser(email);
        usersPage.changeFirstName("Updated QA");
        usersPage.saveUserChanges();
        usersPage.refresh();

        assertThat(usersPage.getUserName(email)).isEqualTo("Updated QA Automation");
    }
}
