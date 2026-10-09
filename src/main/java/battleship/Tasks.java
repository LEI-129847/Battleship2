package battleship;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The type Tasks.
 */
public class Tasks {
	/**
	 * The constant LOGGER.
	 */
	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * The constant GOODBYE_MESSAGE.
	 */
	private static final String GOODBYE_MESSAGE = "Bons ventos!";

	/**
	 * Strings to be used by the user
	 */
	private static final String AJUDA = "ajuda";
	private static final String GERAFROTA = "gerafrota";
	private static final String LEFROTA = "lefrota";
	private static final String DESISTIR = "desisto";
	private static final String RAJADA = "rajada";
	private static final String TIROS = "tiros";
	private static final String MAPA = "mapa";
	private static final String STATUS = "estado";
	private static final String SIMULA = "simula";
	private static final String PDF = "pdf";
	private static final String SCOREBOARD = "scoreboard";

	/**
	 * Name of the PDF file with the moves of the game
	 */
	private static final String PDF_FILE = "jogadas.pdf";
	private static final String SCOREBOARD = "scoreboard";

	/**
	 * This task also tests the fighting element of a round of three shots
	 */
	public static void menu() {

		Scanner in = new Scanner(System.in);
		System.out.print("Escolha o idioma / Choose language (pt/en): ");
		String lang  = in.next();
		Messages.setLanguage(lang);

		IFleet myFleet = null;
		IGame game = null;
		menuHelp();

		System.out.print("> ");
		String command = in.next();
		while (!command.equals(DESISTIR)) {

			switch (command) {
				case GERAFROTA:
					myFleet = Fleet.createRandom();
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case LEFROTA:
					myFleet = buildFleet(in);
					game = new Game(myFleet);
					game.printMyBoard(false, true);
					break;
				case STATUS:
					if (myFleet != null)
						myFleet.printStatus();
					break;
				case MAPA:
					if (myFleet != null)
						game.printMyBoard(false, true);
					break;
				case RAJADA:
					if (game != null) {
						game.readEnemyFire(in);
						myFleet.printStatus();
						game.printMyBoard(true, false);

						if (game.getRemainingShips() == 0) {
							game.over();
							saveGameScore(game);
							System.exit(0);
						}
					}
					break;
				case SIMULA:
					if (game != null) {
						while (game.getRemainingShips() > 0){
							game.randomEnemyFire();
							myFleet.printStatus();
							game.printMyBoard(true, false);
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
						}

						if (game.getRemainingShips() == 0) {
							game.over();
							saveGameScore(game);
							System.exit(0);
						}
					}
					break;
				case TIROS:
					if (game != null)
						game.printMyBoard(true, true);
					break;
				case PDF:
					if (game != null)
						exportMovesToPdf(game);
					else
						System.out.println(Messages.getMessage("err.no_game"));
						System.out.println("Ainda não há jogo! Use primeiro " + GERAFROTA + " ou " + LEFROTA + ".");
					break;

				case SCOREBOARD:
					Scoreboard scoreboard = new Scoreboard();
					scoreboard.showScores();
					break;

				case AJUDA:
					menuHelp();
					break;

                case AJUDA:
                    menuHelp();
                    break;
				default:
					System.out.println("Que comando é esse??? Repete ...");
			}
			System.out.print("> ");
			command = in.next();
		}
		System.out.println(Messages.getMessage("msg.goodbye"));
		if (game != null)
			game.over();
		System.out.println(GOODBYE_MESSAGE);
	}

	/**
	 * This function provides help information about the menu commands.
	 */
	public static void menuHelp() {
		System.out.println(Messages.getMessage("menu.header"));
		System.out.println(Messages.getMessage("menu.instruction"));
		System.out.println("- " + GERAFROTA + ": " + Messages.getMessage("cmd.gerafrota"));
		System.out.println("- " + LEFROTA + ": " + Messages.getMessage("cmd.lefrota"));
		System.out.println("- " + STATUS + ": " + Messages.getMessage("cmd.estado"));
		System.out.println("- " + MAPA + ": " + Messages.getMessage("cmd.mapa"));
		System.out.println("- " + RAJADA + ": " + Messages.getMessage("cmd.rajada"));
		System.out.println("- " + SIMULA + ": " + Messages.getMessage("cmd.simula"));
		System.out.println("- " + TIROS + ": " + Messages.getMessage("cmd.tiros"));
		System.out.println("- " + PDF + ": " + Messages.getMessage("cmd.pdf"));
		System.out.println("- " + SCOREBOARD + ": " + Messages.getMessage("cmd.scoreboard"));
		System.out.println("- " + DESISTIR + ": " + Messages.getMessage("cmd.desisto"));
		System.out.println(Messages.getMessage("menu.footer"));
	}

	private static void exportMovesToPdf(IGame game) {
		assert game != null;
		System.out.println("======================= AJUDA DO MENU =========================");
		System.out.println("Digite um dos comandos abaixo para interagir com o jogo:");
		System.out.println("- " + GERAFROTA + ": Gera uma frota aleatória de navios.");
		System.out.println("- " + LEFROTA + ": Permite criar e carregar uma frota personalizada.");
		System.out.println("- " + STATUS + ": Mostra o status atual da frota.)");
		System.out.println("- " + MAPA + ": Exibe o mapa da frota.");
		System.out.println("- " + RAJADA + ": Realiza uma rajada de disparos.");
		System.out.println("- " + SIMULA + ": Simula um jogo completo.");
		System.out.println("- " + TIROS + ": Lista os tiros válidos realizados (* = tiro em navio, o = tiro na água)");
		System.out.println("- " + PDF + ": Exporta o registo das jogadas para o ficheiro " + PDF_FILE + ".");
		System.out.println("- " + SCOREBOARD + ": Mostra os resultados dos jogos anteriores.");
		System.out.println("- " + DESISTIR + ": Encerra o jogo.");
		System.out.println("===============================================================");
	}

	/**
	 * Exports the moves of the given game to a PDF file.
	 *
	 * @param game The game whose moves are to be exported
	 */
	private static void exportMovesToPdf(IGame game) {
		assert game != null;

		try {
			PdfExporter.export(game, PDF_FILE);
			System.out.println("PDF criado com sucesso: " + PDF_FILE);
		} catch (java.io.IOException e) {
			LOGGER.error("Erro ao criar o PDF das jogadas", e);
			System.out.println("Não foi possível criar o PDF: " + e.getMessage());
		}
	}
	/**
	 * This operation allows the build up of a fleet, given user data
	 *
	 * @param in The scanner to read from
	 * @return The fleet that has been built
	 */
	public static Fleet buildFleet(Scanner in) {

		try {
			PdfExporter.export(game, PDF_FILE);
			System.out.println(Messages.getMessage("pdf.success") + PDF_FILE);
		} catch (java.io.IOException e) {
			LOGGER.error("Erro ao criar o PDF das jogadas", e);
			System.out.println(Messages.getMessage("pdf.error") + e.getMessage());
		}
	}

	public static Fleet buildFleet(Scanner in) {
		assert in != null;

		Fleet fleet = new Fleet();
		int i = 0;
		while (i < Fleet.FLEET_SIZE) {
			IShip s = readShip(in);
			if (s != null) {
				boolean success = fleet.addShip(s);
				if (success)
					i++;
				else
					LOGGER.info("Falha na criacao de {} {} {}", s.getCategory(), s.getBearing(), s.getPosition());
			} else {
				LOGGER.info("Navio desconhecido!");
			}
		}
		LOGGER.info("{} navios adicionados com sucesso!", i);
		return fleet;
	}

	public static Ship readShip(Scanner in) {
		assert in != null;

		String shipKind = in.next();
		Position pos = readPosition(in);
		char c = in.next().charAt(0);
		Compass bearing = Compass.charToCompass(c);
		return Ship.buildShip(shipKind, bearing, pos);
	}

	public static Position readPosition(Scanner in) {
		assert in != null;

		int row = in.nextInt();
		int column = in.nextInt();
		return new Position(row, column);
	}

	public static IPosition readClassicPosition(@NotNull Scanner in) {
		if (!in.hasNext()) {
			throw new IllegalArgumentException("Nenhuma posição válida encontrada!");
		}

		String part1 = in.next();
		String part2 = null;

		if (in.hasNextInt()) {
			part2 = in.next();
		}

		String input = (part2 != null) ? part1 + part2 : part1;
		input = input.toUpperCase();

		if (input.matches("[A-Z]\\d+")) {
			char column = input.charAt(0);
			int row = Integer.parseInt(input.substring(1));
			return new Position(column, row);
		} else if (part2 != null && part1.matches("[A-Z]") && part2.matches("\\d+")) {
			char column = part1.charAt(0);
			int row = Integer.parseInt(part2);
			return new Position(column, row);
		} else {
			throw new IllegalArgumentException("Formato inválido. Use 'A3', 'A 3' ou similar.");
		}
	}

	private static void saveGameScore(IGame game) {
		Scoreboard scoreboard = new Scoreboard();
		int shots = game.getAlienMoves().size() * Game.NUMBER_SHOTS;
		int hits = game.getHits();
		int sinks = game.getSunkShips();
		scoreboard.saveScore(shots, hits, sinks);
		System.out.println(Messages.getMessage("msg.score_saved"));
	}
		System.out.println("Resultado guardado no Scoreboard!"); }

}