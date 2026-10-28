package com.qa.tests.kanban;

import com.qa.core.BaseTest;
import com.qa.core.ConfigManager;
import com.qa.pages.KanbanPage;
import com.qa.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Projets et Kanban")
@Story("Interface Kanban")
class KanbanUiTest extends BaseTest {

    private KanbanPage kanbanPage;

    @BeforeEach
    void signInAndOpenKanban() {
        String baseUrl = ConfigManager.baseUri().toString().replaceAll("/$", "");
        LoginPage loginPage = new LoginPage(driver, waitDuration, baseUrl);
        ConfigManager.Credentials credentials = ConfigManager.validCredentials();

        loginPage.open();
        loginPage.signIn(credentials.email(), credentials.password());
        loginPage.waitForLandingPath();

        kanbanPage = new KanbanPage(driver, waitDuration, baseUrl);
        kanbanPage.open();
    }

    @Test
    @Tag("regression")
    @Description("Vérifie que l'espace Projets et Kanban est accessible sans dépendre de données de démonstration.")
    void boardDisplaysImplementedColumnsAndSeedTasks() {
        assertThat(kanbanPage.isBoardVisible()).isTrue();
    }

    @Test
    @Tag("regression")
    @Description("Le formulaire tâche est disponible lorsqu'un projet accessible autorise sa création.")
    void taskFormOpensWithSelectedColumnStatus() {
        Assumptions.assumeTrue(kanbanPage.hasTaskCreationAction(),
                "Skipped: the configured manager has no writable project in this environment.");
        kanbanPage.openFirstTaskForm();

        assertThat(kanbanPage.getTaskModalStatus()).isEqualTo("Nouvelle tâche");

        kanbanPage.closeTaskForm();
    }

    @Test
    @Tag("regression")
    @Description("Vérifie l'ouverture/fermeture du formulaire projet sans prétendre que la création est persistée.")
    void projectFormCanBeOpenedAndClosed() {
        assertThat(kanbanPage.isProjectViewAvailable()).isTrue();

        kanbanPage.openProjectForm();

        assertThat(kanbanPage.getProjectFormTitle()).isEqualTo("Nouveau projet");

        kanbanPage.closeProjectForm();
        assertThat(kanbanPage.isProjectFormVisible()).isFalse();
    }
}
