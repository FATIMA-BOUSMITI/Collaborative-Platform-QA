package com.qa.tests.permissions;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.core.ConfigManager;
import com.qa.pages.PermissionsPage;
import com.qa.tests.admin.AdminFeatureTestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Rôles et permissions")
@Story("Modification et persistance des permissions")
class PermissionUpdateTest extends AdminFeatureTestBase {

  private PermissionsPage permissionsPage;
  private com.qa.pages.RolesPage rolesPage;
  private String fixtureRole;

  @Override
  protected boolean requiresIsolatedQaData() {
    return true;
  }

  @BeforeEach
  void requireIsolatedQaDatabaseAndOpenMatrix() {
    ConfigManager.assumeIsolatedQaData();
    rolesPage = new com.qa.pages.RolesPage(driver, waitDuration, baseUrl);
    rolesPage.open();
    fixtureRole =
        "QA_SELENIUM_"
            + java.util
                .UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .toUpperCase(java.util.Locale.ROOT);
    rolesPage.createTestRole(fixtureRole);
    permissionsPage = new PermissionsPage(driver, waitDuration, baseUrl);
    permissionsPage.open();
  }

  @org.junit.jupiter.api.AfterEach
  void removeOnlyDedicatedFixtureRole() {
    if (rolesPage != null && fixtureRole != null) {
      if (permissionsPage != null) permissionsPage.discardUnsavedChanges();
      rolesPage.open();
      rolesPage.deleteTestRoleIfPresent(fixtureRole);
    }
  }

  @Test
  @Tag("e2e")
  @Tag("qa-data")
  @Description(
      "Modifie une permission d'un rôle non-admin, vérifie la persistance puis restaure l'état"
          + " initial.")
  void permissionChangeIsPersistedAndRestored() {
    String roleId = permissionsPage.selectEditableRole(fixtureRole);
    String permissionId = permissionsPage.firstPermissionId();
    Boolean initiallyChecked = null;

    try {
      initiallyChecked = permissionsPage.isPermissionChecked(roleId, permissionId);
      permissionsPage.togglePermission(roleId, permissionId);
      permissionsPage.save();

      boolean changedValue = !initiallyChecked;
      permissionsPage.open();
      assertThat(permissionsPage.isPermissionChecked(roleId, permissionId))
          .as("The permission change must be persisted")
          .isEqualTo(changedValue);
    } finally {
      if (initiallyChecked != null) {
        permissionsPage.open();
        if (permissionsPage.isPermissionChecked(roleId, permissionId) != initiallyChecked) {
          permissionsPage.togglePermission(roleId, permissionId);
          permissionsPage.save();
          permissionsPage.open();
        }
        assertThat(permissionsPage.isPermissionChecked(roleId, permissionId))
            .as("The original QA role permission must be restored")
            .isEqualTo(initiallyChecked);
      }
    }
  }
}
