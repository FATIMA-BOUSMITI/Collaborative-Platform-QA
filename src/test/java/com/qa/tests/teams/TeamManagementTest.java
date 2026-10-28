package com.qa.tests.teams;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.core.ConfigManager;
import com.qa.pages.TeamsPage;
import com.qa.tests.admin.AdminFeatureTestBase;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Équipes")
@Story("Gestion des équipes")
class TeamManagementTest extends AdminFeatureTestBase {

  private TeamsPage teamsPage;
  private final java.util.Set<String> trackedNames = new java.util.LinkedHashSet<>();

  @org.junit.jupiter.api.AfterEach
  void removeTrackedTeams() {
    if (teamsPage != null)
      for (String name : trackedNames) if (teamsPage.contains(name)) teamsPage.delete(name);
  }

  @Override
  protected boolean requiresIsolatedQaData() {
    return true;
  }

  @BeforeEach
  void openTeams() {
    ConfigManager.assumeIsolatedQaData();
    teamsPage = new TeamsPage(driver, waitDuration, baseUrl);
    teamsPage.open();
  }

  @Test
  @Tag("e2e")
  @Description(
      "Vérifie les actions CRUD de l'interface équipe; les données sont persistées par le backend.")
  void teamCanBeCreatedUpdatedReloadedAndDeleted() {
    String teamName = "QA team " + UUID.randomUUID();
    String updatedName = teamName + " updated";

    trackedNames.add(teamName);
    trackedNames.add(updatedName);
    teamsPage.create(teamName);
    assertThat(teamsPage.contains(teamName)).isTrue();

    teamsPage.rename(teamName, updatedName);
    assertThat(teamsPage.contains(updatedName)).isTrue();

    teamsPage.open();
    assertThat(teamsPage.contains(updatedName)).isTrue();
    teamsPage.delete(updatedName);
    teamsPage.open();
    assertThat(teamsPage.contains(updatedName)).isFalse();
  }
}
