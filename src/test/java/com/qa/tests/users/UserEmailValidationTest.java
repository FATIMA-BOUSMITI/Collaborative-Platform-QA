package com.qa.tests.users;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.pages.UsersPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Gestion des utilisateurs")
@Story("Validation de l'email")
class UserEmailValidationTest extends UserManagementTestBase {

  @Test
  @Tag("regression")
  @Tag("e2e")
  @Description("Une adresse email mal formée doit être rejetée par le parcours réel.")
  void malformedEmailIsRejected() {
    String malformedEmail = "not-an-email-" + java.util.UUID.randomUUID();

    usersPage.openCreateModal();
    usersPage.fillCreateUserForm("QA", "Invalid Email", malformedEmail);
    usersPage.submitCreateForm();

    assertThat(usersPage.waitForCreateResult())
        .as("The form must remain open when native form validation rejects the malformed email")
        .isEqualTo(UsersPage.CreateResult.REJECTED);
    assertThat(usersPage.getCreateError()).isNotBlank();
    assertThat(usersPage.isCreateModalOpen()).isTrue();
    usersPage.closeCreateModalIfOpen();
    assertThat(usersPage.isUserVisible(malformedEmail)).isFalse();
  }
}
