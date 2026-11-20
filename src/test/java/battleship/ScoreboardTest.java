
package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Scoreboard.
 *
 * Author: LEI-129827
 * Date: 2026-11-20 19:52
 *
 * Cyclomatic Complexity:
 * - Scoreboard(): 2
 * - createTable(): 1
 * - saveScore(): 2
 * - showScores(): 4
 */
class ScoreboardTest {

    private Scoreboard scoreboard;

    private static final String DATABASE = "jdbc:sqlite:scoreboard.db";

    @BeforeEach
    void setUp() throws SQLException {
        cleanDatabase();
        scoreboard = new Scoreboard();
    }

    @AfterEach
    void tearDown() throws SQLException {
        scoreboard = null;
        cleanDatabase();
    }

    // --------------------------------------------------
    // Constructor tests
    // --------------------------------------------------

    @Test
    @DisplayName("Scoreboard1 - creates the scoreboard successfully")
    void scoreboard1() throws SQLException {
        assertNotNull(scoreboard,
                "Error: the Scoreboard instance should not be null");

        assertTrue(tableExists(),
                "Error: the scores table should exist after construction");
    }

    @Test
    @DisplayName("Scoreboard2 - creates the table if it does not exist")
    void scoreboard2() throws SQLException {
        dropTable();

        Scoreboard newScoreboard = new Scoreboard();

        assertNotNull(newScoreboard,
                "Error: a new Scoreboard instance should be created");

        assertTrue(tableExists(),
                "Error: the scores table should be created by the constructor");
    }

    // --------------------------------------------------
    // createTable tests
    // --------------------------------------------------

    @Test
    @DisplayName("createTable - creates the scores table")
    void createTable() throws SQLException {
        assertTrue(tableExists(),
                "Error: createTable should create the scores table");
    }

    // --------------------------------------------------
    // saveScore tests
    // --------------------------------------------------

    @Test
    @DisplayName("saveScore1 - saves a score successfully")
    void saveScore1() throws SQLException {
        scoreboard.saveScore(20, 10, 3);

        try (Connection connection = DriverManager.getConnection(DATABASE);
             Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(
                     "SELECT shots, hits, sinks FROM scores")) {

            assertTrue(results.next(),
                    "Error: a score should have been inserted into the database");

            assertAll("Saved score values",
                    () -> assertEquals(20, results.getInt("shots"),
                            "Error: expected 20 shots"),
                    () -> assertEquals(10, results.getInt("hits"),
                            "Error: expected 10 hits"),
                    () -> assertEquals(3, results.getInt("sinks"),
                            "Error: expected 3 sunk ships")
            );
        }
    }

    @Test
    @DisplayName("saveScore2 - throws an exception when the table does not exist")
    void saveScore2() throws SQLException {
        dropTable();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scoreboard.saveScore(20, 10, 3),
                "Error: saveScore should throw RuntimeException when the table is missing"
        );

        assertEquals("Erro ao guardar o resultado do jogo.",
                exception.getMessage(),
                "Error: the exception should contain the expected message");
    }

    // --------------------------------------------------
    // showScores tests
    // --------------------------------------------------

    @Test
    @DisplayName("showScores1 - displays a message when the scoreboard is empty")
    void showScores1() {
        String output = captureOutput(() -> scoreboard.showScores());

        assertTrue(output.contains("Ainda não existem resultados."),
                "Error: the empty scoreboard message should be displayed");

        assertTrue(output.contains("SCOREBOARD"),
                "Error: the scoreboard title should be displayed");
    }

    @Test
    @DisplayName("showScores2 - displays a single score")
    void showScores2() {
        scoreboard.saveScore(20, 10, 3);

        String output = captureOutput(() -> scoreboard.showScores());

        assertAll("Single score output",
                () -> assertTrue(output.contains("1. "),
                        "Error: the first score position should be displayed"),
                () -> assertTrue(output.contains("Tiros: 20"),
                        "Error: the number of shots should be displayed"),
                () -> assertTrue(output.contains("Acertos: 10"),
                        "Error: the number of hits should be displayed"),
                () -> assertTrue(output.contains("Navios afundados: 3"),
                        "Error: the number of sunk ships should be displayed"),
                () -> assertFalse(output.contains("Ainda não existem resultados."),
                        "Error: the empty scoreboard message should not be displayed")
        );
    }

    @Test
    @DisplayName("showScores3 - displays multiple scores in ascending shot order")
    void showScores3() {
        scoreboard.saveScore(30, 15, 2);
        scoreboard.saveScore(10, 8, 4);
        scoreboard.saveScore(20, 12, 3);

        String output = captureOutput(() -> scoreboard.showScores());

        int position10 = output.indexOf("Tiros: 10");
        int position20 = output.indexOf("Tiros: 20");
        int position30 = output.indexOf("Tiros: 30");

        assertAll("Score ordering",
                () -> assertTrue(position10 >= 0,
                        "Error: the score with 10 shots should be displayed"),
                () -> assertTrue(position20 >= 0,
                        "Error: the score with 20 shots should be displayed"),
                () -> assertTrue(position30 >= 0,
                        "Error: the score with 30 shots should be displayed"),
                () -> assertTrue(position10 < position20,
                        "Error: the score with 10 shots should appear before 20 shots"),
                () -> assertTrue(position20 < position30,
                        "Error: the score with 20 shots should appear before 30 shots"),
                () -> assertTrue(output.contains("1. "),
                        "Error: the first position should be displayed"),
                () -> assertTrue(output.contains("2. "),
                        "Error: the second position should be displayed"),
                () -> assertTrue(output.contains("3. "),
                        "Error: the third position should be displayed")
        );
    }

    @Test
    @DisplayName("showScores4 - throws an exception when the scores table is missing")
    void showScores4() throws SQLException {
        dropTable();

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> scoreboard.showScores(),
                "Error: showScores should throw RuntimeException when the table is missing"
        );

        assertEquals("Erro ao consultar o Scoreboard.",
                exception.getMessage(),
                "Error: the exception should contain the expected message");
    }

    // --------------------------------------------------
    // Helper methods
    // --------------------------------------------------

    private void cleanDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE);
             Statement statement = connection.createStatement()) {

            statement.execute(
                    "CREATE TABLE IF NOT EXISTS scores (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "date TEXT, " +
                            "shots INTEGER, " +
                            "hits INTEGER, " +
                            "sinks INTEGER)"
            );

            statement.executeUpdate("DELETE FROM scores");
        }
    }

    private boolean tableExists() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE);
             ResultSet results = connection.getMetaData()
                     .getTables(null, null, "scores", null)) {

            return results.next();
        }
    }

    private void dropTable() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE);
             Statement statement = connection.createStatement()) {

            statement.execute("DROP TABLE IF EXISTS scores");
        }
    }

    private String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(output));
            action.run();
        } finally {
            System.setOut(originalOut);
        }

        return output.toString();
    }
}
