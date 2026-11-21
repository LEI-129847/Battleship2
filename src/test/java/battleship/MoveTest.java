package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;
    private IShip galleon;
    private IShip barge;

    @BeforeEach
    void setUp() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
        galleon = new Galleon(Compass.NORTH, new Position(1, 1));
        barge = new Barge(Compass.NORTH, new Position(6, 6));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ---------- auxiliares ----------

    private IGame.ShotResult invalid()          { return new IGame.ShotResult(false, false, null, false); }
    private IGame.ShotResult repeated()         { return new IGame.ShotResult(true, true, null, false); }
    private IGame.ShotResult water()            { return new IGame.ShotResult(true, false, null, false); }
    private IGame.ShotResult hit(IShip s)       { return new IGame.ShotResult(true, false, s, false); }
    private IGame.ShotResult sunk(IShip s)      { return new IGame.ShotResult(true, false, s, true); }

    private Move moveOf(IGame.ShotResult... results) {
        return new Move(1, new ArrayList<>(), new ArrayList<>(List.of(results)));
    }

    /** Executa em modo verbose e devolve a primeira linha impressa ("Jogada nº1 -> ..."). */
    private String verboseLine(Move move) {
        move.processEnemyFire(true);
        return captured.toString(StandardCharsets.UTF_8).lines().findFirst().orElse("");
    }

    // ---------- construtor, getters e toString ----------

    @Test
    @DisplayName("Construtor e getters devolvem os valores recebidos")
    void testGetters() {
        List<IPosition> shots = List.of(new Position(1, 1), new Position(2, 2));
        List<IGame.ShotResult> results = List.of(water(), water());
        Move m = new Move(7, shots, results);

        assertEquals(7, m.getNumber());
        assertSame(shots, m.getShots());
        assertSame(results, m.getShotResults());
    }

    @Test
    @DisplayName("toString mostra número, nº de tiros e nº de resultados")
    void testToString() {
        List<IPosition> shots = List.of(new Position(1, 1), new Position(2, 2));
        Move m = new Move(3, shots, List.of(water(), water(), water()));
        assertEquals("Move{number=3, shots=2, results=3}", m.toString());
    }

    // ---------- JSON ----------

    @Test
    @DisplayName("Sem verbose não imprime a linha 'Jogada' mas imprime o JSON")
    void testNonVerboseNoSummary() {
        moveOf(water(), water(), water()).processEnemyFire(false);
        String out = captured.toString(StandardCharsets.UTF_8);
        assertFalse(out.contains("Jogada"));
        assertTrue(out.contains("validShots"));
    }

    @Test
    @DisplayName("Com verbose imprime o número da jogada")
    void testVerbosePrefix() {
        assertTrue(verboseLine(moveOf(water(), water(), water())).startsWith("Jogada nº1 -> "));
    }

    @Test
    @DisplayName("JSON de 3 tiros na água tem os contadores corretos")
    void testJsonAllWater() throws Exception {
        JsonNode json = MAPPER.readTree(moveOf(water(), water(), water()).processEnemyFire(false));

        assertEquals(3, json.get("validShots").asInt());
        assertEquals(3, json.get("missedShots").asInt());
        assertEquals(0, json.get("repeatedShots").asInt());
        assertEquals(0, json.get("outsideShots").asInt());
        assertEquals(0, json.get("sunkBoats").size());
        assertEquals(0, json.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("JSON mantém a ordem dos campos do protocolo")
    void testJsonFieldOrder() throws Exception {
        JsonNode json = MAPPER.readTree(moveOf(water(), water(), water()).processEnemyFire(false));
        Iterator<String> names = json.fieldNames();
        for (String expected : new String[]{"validShots", "sunkBoats", "repeatedShots",
                "outsideShots", "hitsOnBoats", "missedShots"}) {
            assertEquals(expected, names.next());
        }
        assertFalse(names.hasNext());
    }

    @Test
    @DisplayName("JSON separa navios afundados de acertos sem afundar")
    void testJsonSunkAndHits() throws Exception {
        JsonNode json = MAPPER.readTree(
                moveOf(sunk(galleon), hit(barge), water()).processEnemyFire(false));

        assertEquals(3, json.get("validShots").asInt());
        assertEquals(1, json.get("missedShots").asInt());

        JsonNode sunkBoats = json.get("sunkBoats");
        assertEquals(1, sunkBoats.size());
        assertEquals(1, sunkBoats.get(0).get("count").asInt());
        assertEquals(galleon.getCategory(), sunkBoats.get(0).get("type").asText());

        JsonNode hits = json.get("hitsOnBoats");
        assertEquals(1, hits.size());
        assertEquals(1, hits.get(0).get("hits").asInt());
        assertEquals(barge.getCategory(), hits.get(0).get("type").asText());
    }

    @Test
    @DisplayName("Dois navios do mesmo tipo afundados somam no mesmo contador")
    void testJsonSunkSameTypeAccumulates() throws Exception {
        JsonNode json = MAPPER.readTree(
                moveOf(sunk(galleon), sunk(galleon), water()).processEnemyFire(false));
        assertEquals(1, json.get("sunkBoats").size());
        assertEquals(2, json.get("sunkBoats").get(0).get("count").asInt());
    }

    @Test
    @DisplayName("Dois acertos no mesmo tipo de navio somam no mesmo contador")
    void testJsonHitsSameTypeAccumulates() throws Exception {
        JsonNode json = MAPPER.readTree(
                moveOf(hit(galleon), hit(galleon), water()).processEnemyFire(false));
        assertEquals(1, json.get("hitsOnBoats").size());
        assertEquals(2, json.get("hitsOnBoats").get(0).get("hits").asInt());
    }

    @Test
    @DisplayName("Tiros inválidos são ignorados e contam como exteriores")
    void testInvalidShotsIgnored() throws Exception {
        JsonNode json = MAPPER.readTree(
                moveOf(invalid(), invalid(), new IGame.ShotResult(false, false, galleon, true))
                        .processEnemyFire(false));

        assertEquals(0, json.get("validShots").asInt());
        assertEquals(Game.NUMBER_SHOTS, json.get("outsideShots").asInt());
        assertEquals(0, json.get("sunkBoats").size());
    }

    @Test
    @DisplayName("Lista de resultados vazia não rebenta e todos os tiros são exteriores")
    void testEmptyResults() throws Exception {
        JsonNode json = MAPPER.readTree(moveOf().processEnemyFire(false));
        assertEquals(0, json.get("validShots").asInt());
        assertEquals(Game.NUMBER_SHOTS, json.get("outsideShots").asInt());
    }

    // ---------- mensagem verbose: só repetidos / só exteriores ----------

    @Test
    @DisplayName("Verbose: 3 tiros inválidos -> só exteriores (plural)")
    void testVerboseOnlyOutside() {
        assertEquals("Jogada nº1 -> 3 tiros exteriores",
                verboseLine(moveOf(invalid(), invalid(), invalid())));
    }

    @Test
    @DisplayName("Verbose: 1 repetido + 2 exteriores (singular/plural)")
    void testVerboseOneRepeatedOnly() {
        assertEquals("Jogada nº1 -> 1 tiro repetido, 2 tiros exteriores",
                verboseLine(moveOf(repeated(), invalid(), invalid())));
    }

    @Test
    @DisplayName("Verbose: 2 repetidos + 1 exterior (plural/singular)")
    void testVerboseTwoRepeatedOnly() {
        assertEquals("Jogada nº1 -> 2 tiros repetidos, 1 tiro exterior",
                verboseLine(moveOf(repeated(), repeated(), invalid())));
    }

    @Test
    @DisplayName("Verbose: 3 repetidos, sem exteriores")
    void testVerboseAllRepeated() {
        assertEquals("Jogada nº1 -> 3 tiros repetidos",
                verboseLine(moveOf(repeated(), repeated(), repeated())));
    }

    // ---------- mensagem verbose: só água ----------

    @Test
    @DisplayName("Verbose: 3 tiros na água")
    void testVerboseAllWater() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 3 tiros na água",
                verboseLine(moveOf(water(), water(), water())));
    }

    @Test
    @DisplayName("Verbose: 1 tiro na água + 2 exteriores (singular)")
    void testVerboseOneWater() {
        assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 2 tiros exteriores",
                verboseLine(moveOf(water(), invalid(), invalid())));
    }

    // ---------- mensagem verbose: navios afundados ----------

    @Test
    @DisplayName("Verbose: 1 navio ao fundo (singular) + água")
    void testVerboseOneSunk() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 1 " + galleon.getCategory()
                        + " ao fundo + 2 tiros na água",
                verboseLine(moveOf(sunk(galleon), water(), water())));
    }

    @Test
    @DisplayName("Verbose: 2 navios do mesmo tipo ao fundo (plural) + água")
    void testVerboseTwoSunkSameType() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 2 " + galleon.getCategory()
                        + "s ao fundo + 1 tiro na água",
                verboseLine(moveOf(sunk(galleon), sunk(galleon), water())));
    }

    @Test
    @DisplayName("Verbose: navio ao fundo sem água remove o '+' final")
    void testVerboseSunkWithoutWater() {
        String line = verboseLine(moveOf(sunk(galleon), invalid(), invalid()));
        assertTrue(line.contains("1 tiro válido: 1 " + galleon.getCategory() + " ao fundo"));
        assertTrue(line.contains("2 tiros exteriores"));
        assertFalse(line.contains("+"));
    }

    // ---------- mensagem verbose: acertos sem afundar ----------

    @Test
    @DisplayName("Verbose: 1 acerto (singular) + água")
    void testVerboseOneHit() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 1 tiro num(a) " + galleon.getCategory()
                        + " + 2 tiros na água",
                verboseLine(moveOf(hit(galleon), water(), water())));
    }

    @Test
    @DisplayName("Verbose: 2 acertos (plural) + água")
    void testVerboseTwoHits() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 2 tiros num(a) " + galleon.getCategory()
                        + " + 1 tiro na água",
                verboseLine(moveOf(hit(galleon), hit(galleon), water())));
    }

    @Test
    @DisplayName("Verbose: só acertos, sem água, remove o '+' final")
    void testVerboseOnlyHitsWithoutWater() {
        String line = verboseLine(moveOf(hit(galleon), hit(galleon), hit(galleon)));
        assertTrue(line.contains("3 tiros válidos: 3 tiros num(a) " + galleon.getCategory()));
        assertFalse(line.contains("+"));
    }

    @Test
    @DisplayName("Verbose: afundado, acerto e água em simultâneo")
    void testVerboseSunkHitAndWater() {
        assertEquals("Jogada nº1 -> 3 tiros válidos: 1 " + galleon.getCategory()
                        + " ao fundo + 1 tiro num(a) " + barge.getCategory()
                        + " + 1 tiro na água",
                verboseLine(moveOf(sunk(galleon), hit(barge), water())));
    }

    // ---------- mensagem verbose: válidos + repetidos ----------

    @Test
    @DisplayName("Verbose: válido + 1 repetido + 1 exterior")
    void testVerboseValidRepeatedOutside() {
        assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 1 tiro repetido, 1 tiro exterior",
                verboseLine(moveOf(water(), repeated(), invalid())));
    }

    @Test
    @DisplayName("Verbose: válido + 2 repetidos (plural)")
    void testVerboseValidAndTwoRepeated() {
        assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 2 tiros repetidos",
                verboseLine(moveOf(water(), repeated(), repeated())));
    }
}