package com.qa.tests.users;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Gestion des utilisateurs")
@Story("Suppression des utilisateurs")
class UserDeletionTest extends UserManagementTestBase {

    @Test
    @Tag("regression")
    @Tag("e2e")
    @Description("La suppression d'un utilisateur QA est effective et persiste après actualisation.")
    void deletedUserRemainsAbsentAfterRefresh() {
        String email = createTestUser();

        usersPage.deleteUser(email);
        usersPage.refresh();

        assertThat(usersPage.isUserVisible(email)).isFalse();
    }
}
