
package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class TasksTest {

	private Tasks tasks;

	@BeforeEach
	void setUp() {
		tasks = new Tasks();
	}

	@AfterEach
	void tearDown() {
		tasks = null;
	}

	// ---------------------------------------------------------
	// readPosition
	// ---------------------------------------------------------

	@Test
	@DisplayName("readPosition reads row and column")
	void readPosition() {
		Scanner in = new Scanner("3 5");

		Position position = Tasks.readPosition(in);

		assertAll(
				() -> assertEquals(3, position.getRow()),
				() -> assertEquals(5, position.getColumn())
		);
	}

	// ---------------------------------------------------------
	// readClassicPosition
	// ---------------------------------------------------------

	@Test
	@DisplayName("readClassicPosition reads combined coordinate")
	void readClassicPosition1() {
		Scanner in = new Scanner("A3");

		IPosition position = Tasks.readClassicPosition(in);

		assertAll(
				() -> assertEquals(2, position.getColumn()),
				() -> assertEquals(0, position.getRow())
		);
	}

	@Test
	@DisplayName("readClassicPosition reads separated coordinate")
	void readClassicPosition2() {
		Scanner in = new Scanner("B 4");

		IPosition position = Tasks.readClassicPosition(in);

		assertAll(
				() -> assertEquals(3, position.getColumn()),
				() -> assertEquals(1, position.getRow())
		);
	}

	@Test
	@DisplayName("readClassicPosition accepts lowercase letters")
	void readClassicPosition3() {
		Scanner in = new Scanner("c5");

		IPosition position = Tasks.readClassicPosition(in);

		assertAll(
				() -> assertEquals(4, position.getColumn()),
				() -> assertEquals(2, position.getRow())
		);
	}

	@Test
	@DisplayName("readClassicPosition rejects empty input")
	void readClassicPosition4() {
		Scanner in = new Scanner("");

		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(in));
	}

	@Test
	@DisplayName("readClassicPosition rejects invalid coordinate format")
	void readClassicPosition5() {
		Scanner in = new Scanner("123");

		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(in));
	}

	@Test
	@DisplayName("readClassicPosition rejects letter without row")
	void readClassicPosition6() {
		Scanner in = new Scanner("A");

		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(in));
	}

	@Test
	@DisplayName("readClassicPosition rejects non-numeric row")
	void readClassicPosition7() {
		Scanner in = new Scanner("A xyz");

		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(in));
	}

	// ---------------------------------------------------------
	// readShip
	// ---------------------------------------------------------

	@Test
	@DisplayName("readShip creates a barca")
	void readShip1() {
		Scanner in = new Scanner("barca 2 3 n");

		Ship ship = Tasks.readShip(in);

		assertAll(
				() -> assertNotNull(ship),
				() -> assertEquals("Barca", ship.getCategory()),
				() -> assertEquals(2, ship.getPosition().getRow()),
				() -> assertEquals(3, ship.getPosition().getColumn()),
				() -> assertEquals(Compass.NORTH, ship.getBearing())
		);
	}

	@Test
	@DisplayName("readShip returns null for unknown ship type")
	void readShip2() {
		Scanner in = new Scanner("submarino 2 3 n");

		Ship ship = Tasks.readShip(in);

		assertNull(ship);
	}

	// ---------------------------------------------------------
	// buildFleet
	// ---------------------------------------------------------

	@Test
	@DisplayName("buildFleet creates a complete fleet")
	void buildFleet1() {
		String input =
				"barca 0 0 n " +
						"barca 0 2 n " +
						"barca 0 4 n " +
						"barca 0 6 n " +
						"barca 0 8 n " +
						"barca 2 0 n " +
						"barca 2 2 n " +
						"barca 2 4 n " +
						"barca 2 6 n " +
						"barca 2 8 n " +
						"barca 4 0 n";

		Fleet fleet = Tasks.buildFleet(new Scanner(input));

		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	@DisplayName("buildFleet ignores unknown ship types")
	void buildFleet2() {
		String input =
				"submarino 0 0 n " +
						"barca 0 0 n " +
						"barca 0 2 n " +
						"barca 0 4 n " +
						"barca 0 6 n " +
						"barca 0 8 n " +
						"barca 2 0 n " +
						"barca 2 2 n " +
						"barca 2 4 n " +
						"barca 2 6 n " +
						"barca 2 8 n " +
						"barca 4 0 n";

		Fleet fleet = Tasks.buildFleet(new Scanner(input));

		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	@DisplayName("buildFleet ignores ships outside the board")
	void buildFleet3() {
		String input =
				"barca 20 20 n " +
						"barca 0 0 n " +
						"barca 0 2 n " +
						"barca 0 4 n " +
						"barca 0 6 n " +
						"barca 0 8 n " +
						"barca 2 0 n " +
						"barca 2 2 n " +
						"barca 2 4 n " +
						"barca 2 6 n " +
						"barca 2 8 n " +
						"barca 4 0 n";

		Fleet fleet = Tasks.buildFleet(new Scanner(input));

		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	@DisplayName("buildFleet ignores colliding ships")
	void buildFleet4() {
		String input =
				"barca 0 0 n " +
						"barca 0 0 n " +
						"barca 0 2 n " +
						"barca 0 4 n " +
						"barca 0 6 n " +
						"barca 0 8 n " +
						"barca 2 0 n " +
						"barca 2 2 n " +
						"barca 2 4 n " +
						"barca 2 6 n " +
						"barca 2 8 n " +
						"barca 4 0 n";

		Fleet fleet = Tasks.buildFleet(new Scanner(input));

		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	// ---------------------------------------------------------
	// menuHelp
	// ---------------------------------------------------------

	@Test
	@DisplayName("menuHelp prints all command names")
	void menuHelp() {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			System.setOut(new PrintStream(output));
			Tasks.menuHelp();
		} finally {
			System.setOut(originalOut);
		}

		String result = output.toString();

		assertAll(
				() -> assertTrue(result.contains("gerafrota")),
				() -> assertTrue(result.contains("lefrota")),
				() -> assertTrue(result.contains("estado")),
				() -> assertTrue(result.contains("mapa")),
				() -> assertTrue(result.contains("rajada")),
				() -> assertTrue(result.contains("simula")),
				() -> assertTrue(result.contains("tiros")),
				() -> assertTrue(result.contains("Scoreboard")),
				() -> assertTrue(result.contains("pdf")),
				() -> assertTrue(result.contains("desisto"))
		);
	}

	// ---------------------------------------------------------
	// formatDuration
	// ---------------------------------------------------------

	private String invokeFormatDuration(long millis) throws Exception {
		Method method = Tasks.class.getDeclaredMethod(
				"formatDuration", long.class);
		method.setAccessible(true);

		return (String) method.invoke(null, millis);
	}

	@Test
	@DisplayName("formatDuration formats zero milliseconds")
	void formatDuration() throws Exception {
		assertEquals("0 min 0.0 s", invokeFormatDuration(0));
	}

	@Test
	@DisplayName("formatDuration formats seconds")
	void formatDuration1() throws Exception {
		assertEquals("1 min 1.0 s", invokeFormatDuration(61000));
	}

	@Test
	@DisplayName("formatDuration formats minutes and tenths")
	void formatDuration2() throws Exception {
		assertEquals("2 min 5.9 s", invokeFormatDuration(125990));
	}

	// ---------------------------------------------------------
	// exportMovesToPdf
	// ---------------------------------------------------------

	@Test
	@DisplayName("exportMovesToPdf creates a PDF successfully")
	void exportMovesToPdf() throws Exception {
		IGame game = new Game(new Fleet());

		Method method = Tasks.class.getDeclaredMethod(
				"exportMovesToPdf", IGame.class);
		method.setAccessible(true);

		Path pdf = Path.of("Jogadas.pdf");

		// Não apagar nem substituir um ficheiro preexistente.
		assertFalse(Files.exists(pdf),
				"Já existe um Jogadas.pdf; não executar o teste sobre esse ficheiro");

		try {
			method.invoke(null, game);

			assertTrue(Files.exists(pdf));
			assertTrue(Files.size(pdf) > 0);
		} finally {
			Files.deleteIfExists(pdf);
		}
	}

	// ---------------------------------------------------------
	// saveGameScore
	// ---------------------------------------------------------

	private void invokeSaveGameScore(IGame game) throws Exception {
		Method method = Tasks.class.getDeclaredMethod(
				"saveGameScore", IGame.class);
		method.setAccessible(true);
		method.invoke(null, game);
	}

	@Test
	@DisplayName("saveGameScore saves game statistics")
	void saveGameScore() throws Exception {
		IGame game = new Game(new Fleet());

		assertDoesNotThrow(() -> invokeSaveGameScore(game));
	}

	// ---------------------------------------------------------
	// menu - commands without an initialized game
	// ---------------------------------------------------------

	@Test
	@DisplayName("menu processes commands without a game")
	void menu() {
		InputStream originalIn = System.in;
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		String input =
				"pt " +
						"estado " +
						"mapa " +
						"rajada " +
						"simula " +
						"tiros " +
						"pdf " +
						"ajuda " +
						"comandoInvalido " +
						"desisto";

		try {
			System.setIn(new ByteArrayInputStream(
					input.getBytes(StandardCharsets.UTF_8)));
			System.setOut(new PrintStream(output));

			assertDoesNotThrow(Tasks::menu);

			String result = output.toString();

			assertTrue(result.contains("Bons Ventos!"));
			assertTrue(result.contains("Que comando é esse???"));
			assertTrue(result.contains("Ainda não há jogo!"));
		} finally {
			System.setIn(originalIn);
			System.setOut(originalOut);
		}
	}

	// ---------------------------------------------------------
	// menu - commands with an initialized game
	// ---------------------------------------------------------

	@Test
	@DisplayName("menu processes commands after gerafrota")
	void menu1() throws IOException {
		InputStream originalIn = System.in;
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		String input =
				"pt " +
						"gerafrota " +
						"estado " +
						"mapa " +
						"tiros " +
						"pdf " +
						"ajuda " +
						"desisto";

		Path pdf = Path.of("Jogadas.pdf");

		assertFalse(Files.exists(pdf),
				"Já existe um Jogadas.pdf; não executar o teste sobre esse ficheiro");

		try {
			System.setIn(new ByteArrayInputStream(
					input.getBytes(StandardCharsets.UTF_8)));
			System.setOut(new PrintStream(output));

			assertDoesNotThrow(Tasks::menu);

			String result = output.toString();

			assertTrue(result.contains("Bons Ventos!"));
			assertTrue(result.contains("PDF foi criado com sucesso"));
		} finally {
			System.setIn(originalIn);
			System.setOut(originalOut);
			Files.deleteIfExists(pdf);
		}
	}

	// ---------------------------------------------------------
	// menu - scoreboard
	// ---------------------------------------------------------

	@Test
	@DisplayName("menu opens scoreboard")
	void menuScoreboard() {
		InputStream originalIn = System.in;
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		String input = "pt Scoreboard desisto";

		try {
			System.setIn(new ByteArrayInputStream(
					input.getBytes(StandardCharsets.UTF_8)));
			System.setOut(new PrintStream(output));

			assertDoesNotThrow(Tasks::menu);

			assertTrue(output.toString().contains("Bons Ventos!"));
		} finally {
			System.setIn(originalIn);
			System.setOut(originalOut);
		}
	}

	// ---------------------------------------------------------
	// menu - generated fleet and exit
	// ---------------------------------------------------------

	@Test
	@DisplayName("menu generates a random fleet and exits")
	void menuGerafrota() {
		InputStream originalIn = System.in;
		PrintStream originalOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		String input = "pt gerafrota desisto";

		try {
			System.setIn(new ByteArrayInputStream(
					input.getBytes(StandardCharsets.UTF_8)));
			System.setOut(new PrintStream(output));

			assertDoesNotThrow(Tasks::menu);

			assertTrue(output.toString().contains("Bons Ventos!"));
		} finally {
			System.setIn(originalIn);
			System.setOut(originalOut);
		}
	}
}
