package com.qa.tests.kanban;

import static org.assertj.core.api.Assertions.assertThat;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.KanbanPage;
import com.qa.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Kanban")
@Story("Création, déplacement et suppression de tâches")
class KanbanTaskWorkflowTest extends BaseTest {

  private KanbanPage kanbanPage;
  private String baseUrl;
  private String trackedTask;

  @BeforeEach
  void signInAsManagerAndOpenBoard() {
    ConfigManager.assumeIsolatedQaData();
    ConfigManager.requireIsolatedQaData();
    baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");
    ConfigManager.Credentials credentials = ConfigManager.validCredentials();
    LoginPage loginPage = new LoginPage(driver, waitDuration, baseUrl);
    loginPage.open();
    loginPage.signIn(credentials.email(), credentials.password());
    loginPage.waitForLandingPath();

    kanbanPage = new KanbanPage(driver, waitDuration, baseUrl);
    kanbanPage.open();
  }

  @org.junit.jupiter.api.AfterEach
  void archiveOnlyTrackedTaskIfPresent() {
    if (kanbanPage != null
        && trackedTask != null
        && kanbanPage.getTaskTitles().contains(trackedTask)) kanbanPage.archiveTask(trackedTask);
  }

  @Test
  @Tag("e2e")
  @Description(
      "Crée une tâche, la déplace par glisser-déposer, confirme son archivage et vérifie son"
          + " retrait du DOM.")
  void taskCanBeCreatedMovedAndArchived() {
    String taskTitle = "QA Kanban " + UUID.randomUUID();

    trackedTask = taskTitle;
    kanbanPage.createTask(taskTitle);
    assertThat(kanbanPage.getTaskStatus(taskTitle)).isEqualTo("TODO");

    kanbanPage.moveTaskToStatus(taskTitle, "IN_PROGRESS");
    assertThat(kanbanPage.getTaskStatus(taskTitle)).isEqualTo("IN_PROGRESS");

    kanbanPage.open();
    assertThat(kanbanPage.getTaskStatus(taskTitle)).isEqualTo("IN_PROGRESS");
    kanbanPage.archiveTask(taskTitle);
    kanbanPage.open();
    assertThat(kanbanPage.getTaskTitles()).doesNotContain(taskTitle);
  }
}
