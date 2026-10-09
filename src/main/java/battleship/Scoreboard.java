package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.sql.ResultSet;

public class Scoreboard {

    private static final String DATABASE = "jdbc:sqlite:scoreboard.db";

    private Connection connection;

    public Scoreboard() {
        try {
            connection = DriverManager.getConnection(DATABASE);
            createTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scores ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "date TEXT, "
                + "shots INTEGER, "
                + "hits INTEGER, "
                + "sinks INTEGER"
                + ")";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public void saveScore(int shots, int hits, int sinks) {
        String sql = "INSERT INTO scores (date, shots, hits, sinks) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LocalDateTime.now().toString());
            statement.setInt(2, shots);
            statement.setInt(3, hits);
            statement.setInt(4, sinks);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao guardar o resultado do jogo.", e);
        }
    }

    public void showScores() {
        String sql = "SELECT * FROM scores ORDER BY shots ASC";

        try (Statement statement = connection.createStatement();
             ResultSet results = statement.executeQuery(sql)) {

            System.out.println();
            System.out.println("========== SCOREBOARD ==========");

            int position = 1;

            while (results.next()) {
                System.out.println(
                        position + ". " +
                                "Data: " + results.getString("date") +
                                " | Tiros: " + results.getInt("shots") +
                                " | Acertos: " + results.getInt("hits") +
                                " | Navios afundados: " + results.getInt("sinks")
                );

                position++;
            }

            if (position == 1) {
                System.out.println("Ainda não existem resultados.");
            }

            System.out.println("================================");
            System.out.println();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar o Scoreboard.", e);
        }
    }

}