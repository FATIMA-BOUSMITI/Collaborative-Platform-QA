package com.qa.tests.users;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Gestion des utilisateurs")
@Story("Liste des utilisateurs")
class UserListTest extends UserManagementTestBase {

    @Test
    @Tag("regression")
    @Description("La page affiche les colonnes réellement présentes dans le tableau des utilisateurs.")
    void userTableDisplaysItsExpectedColumns() {
        assertThat(usersPage.getColumnHeaders())
                .containsExactly("Utilisateur", "Email", "Département", "Rôle", "Statut", "Actions");
    }
}
