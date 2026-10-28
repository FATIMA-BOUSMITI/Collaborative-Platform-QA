package com.qa.tests.users;

import static org.assertj.core.api.Assertions.assertThat;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Gestion des utilisateurs")
@Story("Validation du formulaire utilisateur")
class UserRequiredFieldsTest extends UserManagementTestBase {

  @Test
  @Tag("regression")
  @Description("Le formulaire exige un prénom.")
  void firstNameIsRequired() {
    assertRequiredField("", "Automation", newTestEmail(), "Le prénom est obligatoire.");
  }

  @Test
  @Tag("regression")
  @Description("Le formulaire exige un nom.")
  void lastNameIsRequired() {
    assertRequiredField("QA", "", newTestEmail(), "Le nom est obligatoire.");
  }

  @Test
  @Tag("regression")
  @Description("Le formulaire exige une adresse email.")
  void emailIsRequired() {
    assertRequiredField("QA", "Automation", "", "L'adresse email est obligatoire.");
  }

  @Test
  @Tag("regression")
  @Description("Le formulaire exige un département.")
  void departmentIsRequired() {
    usersPage.openCreateModal();
    usersPage.fillCreateUserForm("QA", "Automation", newTestEmail());
    usersPage.leaveDepartmentUnselected();
    usersPage.submitCreateForm();

    assertThat(usersPage.isRequiredFieldInvalid("Département")).isTrue();
  }

  @Test
  @Tag("regression")
  @Description("Le formulaire exige un rôle.")
  void roleIsRequired() {
    usersPage.openCreateModal();
    usersPage.fillCreateUserForm("QA", "Automation", newTestEmail());
    usersPage.leaveRoleUnselected();
    usersPage.submitCreateForm();

    assertThat(usersPage.isRequiredFieldInvalid("Rôle")).isTrue();
  }

  private void assertRequiredField(
      String firstName, String lastName, String email, String expectedMessage) {
    usersPage.openCreateModal();
    usersPage.fillCreateUserForm(firstName, lastName, email);
    usersPage.submitCreateForm();

    String label = firstName.isEmpty() ? "Prénom" : lastName.isEmpty() ? "Nom" : "Email";
    assertThat(usersPage.isRequiredFieldInvalid(label)).as(expectedMessage).isTrue();
    assertThat(usersPage.isCreateModalOpen()).isTrue();
  }
}
