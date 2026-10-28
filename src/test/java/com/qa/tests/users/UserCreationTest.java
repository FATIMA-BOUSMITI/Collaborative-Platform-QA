package com.qa.tests.users;

import com.qa.pages.UsersPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Gestion des utilisateurs")
@Story("Création des utilisateurs")
class UserCreationTest extends UserManagementTestBase {

    @Test
    @Tag("smoke")
    @Tag("e2e")
    @Description("Un nouvel utilisateur est visible après rechargement, ce qui vérifie sa persistance backend.")
    void shouldCreateUserSuccessfully() {
        String email = createTestUser();

        usersPage.refresh();

        assertThat(usersPage.waitForUser(email)).isTrue();
        assertThat(usersPage.getUserName(email)).isEqualTo("QA Selenium Automation");
        assertThat(usersPage.getUserDepartment(email))
                .isNotBlank()
                .isNotEqualTo("Sans département");
        assertThat(usersPage.getUserRole(email))
                .isNotBlank()
                .isNotEqualTo("Aucun rôle");
    }

    @Test
    @Tag("regression")
    @Tag("e2e")
    @Description("Une adresse déjà enregistrée est rejetée et ne crée pas de seconde ligne.")
    void shouldRejectDuplicateEmail() {
        String email = createTestUser();

        usersPage.openCreateModal();
        usersPage.fillCreateUserForm("Duplicate", "QA", email);
        usersPage.submitCreateForm();

        assertThat(usersPage.waitForCreateResult())
                .isEqualTo(UsersPage.CreateResult.REJECTED);
        assertThat(usersPage.getCreateError()).isNotBlank();

        usersPage.closeCreateModalIfOpen();
        usersPage.refresh();

        assertThat(usersPage.waitForUser(email)).isTrue();
        assertThat(usersPage.getVisibleUserEmails().stream()
                .filter(email::equals)
                .count()).isEqualTo(1);
    }
}
