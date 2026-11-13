package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários para a classe PdfExporter")
public class PdfExporterTest {

    @TempDir
    Path tempDir;

    private String outputPath;

    @BeforeEach
    void setUp() {
        outputPath = tempDir.resolve("jogadas_teste.pdf").toString();
    }

    @Test
    @DisplayName("Exportação bem-sucedida com dados simulados manualmente")
    void testExportSuccessManualStubs() throws IOException {
        IGame game = new IGame() {
            @Override public String randomEnemyFire() { return ""; }
            @Override public String readEnemyFire(Scanner in) { return ""; }
            @Override public void fireShots(List<IPosition> shots) {}
            @Override public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) { return null; }
            @Override public IFleet getMyFleet() { return null; }
            @Override public IFleet getAlienFleet() { return null; }
            @Override public List<IMove> getMyMoves() { return Collections.emptyList(); }
            @Override public void printMyBoard(boolean show_shots, boolean show_legend) {}
            @Override public void printAlienBoard(boolean show_shots, boolean show_legend) {}
            @Override public void over() {}

            @Override
            public List<IMove> getAlienMoves() {
                return List.of(new IMove() {
                    @Override public int getNumber() { return 1; }
                    @Override public List<IPosition> getShots() {
                        return List.of(new PositionStub(5, 5));
                    }
                    @Override public List<ShotResult> getShotResults() {
                        return List.of(
                                new IGame.ShotResult(false, false, null, false), // Fora (!valid)
                                new IGame.ShotResult(true, true, null, false),  // Repetido
                                new IGame.ShotResult(true, false, null, false), // Água
                                new IGame.ShotResult(true, false, new ShipStub("Barca"), false), // Hit sem afundar
                                new IGame.ShotResult(true, false, new ShipStub("Fragata"), true)   // Afunda navio
                        );
                    }
                    @Override public String processEnemyFire(boolean verbose) { return ""; }
                });
            }
            @Override public int getHits() { return 3; }
            @Override public int getSunkShips() { return 1; }
            @Override public int getRemainingShips() { return 2; }
            @Override public int getRepeatedShots() { return 1; }
            @Override public int getInvalidShots() { return 1; }
        };

        assertDoesNotThrow(() -> PdfExporter.export(game, outputPath));

        File pdfFile = new File(outputPath);
        assertTrue(pdfFile.exists(), "O ficheiro PDF deve existir após a exportação.");
        assertTrue(pdfFile.length() > 0, "O ficheiro PDF não deve estar vazio.");
    }

    @Test
    @DisplayName("Exportação com jogadas vazias")
    void testExportEmptyMoveResults() throws IOException {
        IGame game = new IGame() {
            @Override public String randomEnemyFire() { return ""; }
            @Override public String readEnemyFire(Scanner in) { return ""; }
            @Override public void fireShots(List<IPosition> shots) {}
            @Override public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) { return null; }
            @Override public IFleet getMyFleet() { return null; }
            @Override public IFleet getAlienFleet() { return null; }
            @Override public List<IMove> getMyMoves() { return Collections.emptyList(); }
            @Override public void printMyBoard(boolean show_shots, boolean show_legend) {}
            @Override public void printAlienBoard(boolean show_shots, boolean show_legend) {}
            @Override public void over() {}

            @Override
            public List<IMove> getAlienMoves() {
                return List.of(new IMove() {
                    @Override public int getNumber() { return 2; }
                    @Override public List<IPosition> getShots() { return Collections.emptyList(); }
                    @Override public List<ShotResult> getShotResults() { return Collections.emptyList(); }
                    @Override public String processEnemyFire(boolean verbose) { return ""; }
                });
            }
            @Override public int getHits() { return 0; }
            @Override public int getSunkShips() { return 0; }
            @Override public int getRemainingShips() { return 3; }
            @Override public int getRepeatedShots() { return 0; }
            @Override public int getInvalidShots() { return 0; }
        };

        assertDoesNotThrow(() -> PdfExporter.export(game, outputPath));

        File pdfFile = new File(outputPath);
        assertTrue(pdfFile.exists(), "O ficheiro PDF deve ser criado mesmo com jogadas vazias.");
    }

    @Test
    @DisplayName("Lança IOException para caminho inválido")
    void testExportThrowsIOException() {
        IGame game = new IGame() {
            @Override public String randomEnemyFire() { return ""; }
            @Override public String readEnemyFire(Scanner in) { return ""; }
            @Override public void fireShots(List<IPosition> shots) {}
            @Override public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) { return null; }
            @Override public IFleet getMyFleet() { return null; }
            @Override public IFleet getAlienFleet() { return null; }
            @Override public List<IMove> getAlienMoves() { return Collections.emptyList(); }
            @Override public List<IMove> getMyMoves() { return Collections.emptyList(); }
            @Override public int getRepeatedShots() { return 0; }
            @Override public int getInvalidShots() { return 0; }
            @Override public int getHits() { return 0; }
            @Override public int getSunkShips() { return 0; }
            @Override public int getRemainingShips() { return 0; }
            @Override public void printMyBoard(boolean show_shots, boolean show_legend) {}
            @Override public void printAlienBoard(boolean show_shots, boolean show_legend) {}
            @Override public void over() {}
        };
        assertThrows(IOException.class, () -> PdfExporter.export(game, tempDir.toString()));
    }

    // Stub auxiliar para representar uma posição nos testes
    private static class PositionStub implements IPosition {
        private final int row;
        private final int col;

        public PositionStub(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override public int getRow() { return row; }
        @Override public int getColumn() { return col; }
        @Override public char getClassicRow() { return (char) ('A' + row); }
        @Override public int getClassicColumn() { return col + 1; }
        @Override public boolean equals(Object other) { return super.equals(other); }
        @Override public boolean isInside() { return true; }
        @Override public boolean isAdjacentTo(IPosition other) { return false; }
        @Override public void occupy() {}
        @Override public void shoot() {}
        @Override public boolean isOccupied() { return false; }
        @Override public boolean isHit() { return false; }
        @Override public List<IPosition> adjacentPositions() { return Collections.emptyList(); }
        @Override public String toString() { return "A5"; }
    }

    // Stub auxiliar para representar um navio simples nos testes (com todos os métodos de IShip)
    private static class ShipStub implements IShip {
        private final String category;

        public ShipStub(String category) {
            this.category = category;
        }

        @Override public String getCategory() { return category; }
        @Override public Integer getSize() { return 1; }
        @Override public List<IPosition> getPositions() { return Collections.emptyList(); }
        @Override public List<IPosition> getAdjacentPositions() { return Collections.emptyList(); }
        @Override public IPosition getPosition() { return null; }
        @Override public Compass getBearing() { return Compass.NORTH; }
        @Override public boolean stillFloating() { return true; }
        @Override public int getTopMostPos() { return 0; }
        @Override public int getBottomMostPos() { return 0; }
        @Override public int getLeftMostPos() { return 0; }
        @Override public int getRightMostPos() { return 0; }
        @Override public boolean occupies(IPosition pos) { return false; }
        @Override public boolean tooCloseTo(IShip other) { return false; }
        @Override public boolean tooCloseTo(IPosition pos) { return false; }
        @Override public boolean isInsideBoard() { return true; }
        @Override public void shoot(IPosition pos) {}
        @Override public void sink() {}
    }
}