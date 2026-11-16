package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários JUNIT5 para a interface IMove")
class IMoveTest {

    @Test
    @DisplayName("Deve ler corretamente uma jogada com múltiplos tiros (ramo com iterações no ciclo)")
    void testReadMoveWithMultipleShots() {
        // Simula input: 3 tiros nas posições (1, 2), (3, 4) e (5, 6)
        String simulatedInput = "3 1 2 3 4 5 6";
        Scanner scanner = new Scanner(simulatedInput);
        int moveNumber = 1;

        Move move = IMove.readMove(moveNumber, scanner);

        assertNotNull(move, "O objeto Move devolvido não deve ser nulo");
        assertEquals(moveNumber, move.getNumber(), "O número da jogada deve corresponder ao passado");

        List<IPosition> shots = move.getShots();
        assertNotNull(shots);
        assertEquals(3, shots.size(), "Devem ter sido lidos exatamente 3 tiros");

        assertEquals(1, shots.get(0).getRow());
        assertEquals(2, shots.get(0).getColumn());

        assertEquals(3, shots.get(1).getRow());
        assertEquals(4, shots.get(1).getColumn());

        assertEquals(5, shots.get(2).getRow());
        assertEquals(6, shots.get(2).getColumn());

        assertNotNull(move.getShotResults(), "A lista de resultados dos tiros deve ser inicializada");
        assertTrue(move.getShotResults().isEmpty(), "A lista de resultados deve começar vazia");
    }

    @Test
    @DisplayName("Deve ler corretamente uma jogada com zero tiros (ramo sem entrar no ciclo)")
    void testReadMoveWithZeroShots() {
        // Simula input: 0 tiros (garante cobertura do ramo false da condição i < numShots)
        String simulatedInput = "0";
        Scanner scanner = new Scanner(simulatedInput);
        int moveNumber = 5;

        Move move = IMove.readMove(moveNumber, scanner);

        assertNotNull(move);
        assertEquals(moveNumber, move.getNumber());
        assertTrue(move.getShots().isEmpty(), "A lista de tiros deve estar vazia quando numShots é 0");
        assertTrue(move.getShotResults().isEmpty());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o Scanner tem dados insuficientes")
    void testReadMoveIncompleteInput() {
        // Indica 2 tiros, mas só fornece coordenadas para 1 tiro
        String simulatedInput = "2 1 2";
        Scanner scanner = new Scanner(simulatedInput);

        assertThrows(NoSuchElementException.class, () -> {
            IMove.readMove(1, scanner);
        }, "Deve lançar NoSuchElementException se faltarem coordenadas no Scanner");
    }

    @Test
    @DisplayName("Deve lançar exceção quando o Scanner recebe texto em vez de inteiros")
    void testReadMoveInvalidInputFormat() {
        String simulatedInput = "abc";
        Scanner scanner = new Scanner(simulatedInput);

        assertThrows(InputMismatchException.class, () -> {
            IMove.readMove(1, scanner);
        }, "Deve lançar InputMismatchException se o input não for numérico");
    }
}