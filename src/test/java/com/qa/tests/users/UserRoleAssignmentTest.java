package com.qa.tests.users;

import com.qa.pages.RolesPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Rôles et permissions")
@Story("Affectation et modification du rôle utilisateur")
class UserRoleAssignmentTest extends UserManagementTestBase {

    private RolesPage rolesPage;

    @AfterEach
    void returnToUsersForTrackedUserCleanup() {
        if (usersPage != null) {
            usersPage.open();
        }
    }

    @Test
    @Tag("e2e")
    @Tag("qa-data")
    @Description("Modifie le rôle d'un utilisateur créé par le test; le compte est supprimé par le nettoyage QA.")
    void testUserRoleCanBeChanged() {
        String email = createTestUser();
        String baseUrl = com.qa.core.ConfigManager.baseUri()
                .toString()
                .replaceAll("/$", "");
        rolesPage = new RolesPage(driver, waitDuration, baseUrl);
        rolesPage.open();
        rolesPage.searchFor(email);

        String initialRole = rolesPage.getRoleForEmail(email);
        rolesPage.openRoleEditorFor(email);
        List<String> availableRoles = rolesPage.getNonAdminRoleOptions();
        String differentRole = availableRoles.stream()
                .filter(role -> !role.equalsIgnoreCase(initialRole))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "At least two non-administrator roles are needed for this QA scenario."));

        rolesPage.selectRoleAndSave(differentRole);
        rolesPage.open();
        rolesPage.searchFor(email);
        assertThat(rolesPage.getRoleForEmail(email))
                .as("Role assignment must be persisted by the backend")
                .isEqualTo(differentRole);
    }
}
