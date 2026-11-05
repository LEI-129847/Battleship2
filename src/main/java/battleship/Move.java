package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public class Move implements IMove {

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		// LinkedHashMap mantém a ordem de inserção, para o JSON sair sempre pela mesma ordem
		Map<String, Integer> sunkBoatsCount = new LinkedHashMap<>(); // Rastrear quantos navios de cada tipo afundaram
		Map<String, Integer> hitsPerBoat = new LinkedHashMap<>();    // Acertos que NÃO afundaram o navio

		// Processar cada resultado de tiro
		for (IGame.ShotResult result : this.shotResults) {
			if (!result.valid()) {
				// Tiro inválido - apenas ignorar
				continue;
			}

			if (result.repeated())
				repeatedShots++; // tiro repetido
			else {
				// Tiro válido
				validShots++;
				if (result.ship() == null)
					missedShots++; // Tiro na água
				else {
					String boatName = result.ship().getCategory();
					if (result.sunk())
						sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1); // O tiro que afunda conta só em sunkBoats
					else
						hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
				}
			}
		}

		// Determinar número de tiros fora do tabuleiro
		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		if (verbose) {
			// Construção da mensagem de saída
			StringBuilder output = new StringBuilder();

			if (validShots == 0 && repeatedShots > 0) {
				output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
			} else {
				if (validShots > 0) {
					output.append(validShots).append(" tiro").append(validShots > 1 ? "s" : "").append(" válido").append(validShots > 1 ? "s" : "").append(": ");
				}

				// Atualizar lógica para contar múltiplos barcos afundados do mesmo tipo
				if (!sunkBoatsCount.isEmpty()) {
					for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
						String boatName = entry.getKey();
						int count = entry.getValue();
						output.append(count).append(" ").append(boatName).append(count > 1 ? "s" : "").append(" ao fundo").append(" + ");
					}
				}

				if (!hitsPerBoat.isEmpty()) {
					for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
						String boatName = entry.getKey();
						int hits = entry.getValue();
						output.append(hits).append(" tiro").append(hits > 1 ? "s" : "").append(" num(a) ").append(boatName).append(" + ");
					}
				}

				if (missedShots > 0) {
					output.append(missedShots).append(" tiro").append(missedShots > 1 ? "s" : "").append(" na água");
				} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
					output.setLength(output.length() - 2); // Remover o "+" final
				}

				if (repeatedShots > 0) {
					if (validShots > 0) {
						output.append(", ");
					}
					output.append(repeatedShots).append(" tiro").append(repeatedShots > 1 ? "s" : "").append(" repetido").append(repeatedShots > 1 ? "s" : "");
				}
			}

			// Adicionar contagem de tiros fora do tabuleiro
			if (outsideShots > 0) {
				if (!output.isEmpty()) {
					output.append(", ");
				}
				output.append(outsideShots).append(" tiro").append(outsideShots > 1 ? "s" : "").append(" exterior").append(outsideShots > 1 ? "es" : "");
			}

			// Imprimir na consola se verbose for true
			System.out.println("Jogada nº" + this.number + " -> " + output);
		}

		// Criar a lista de barcos afundados
		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
			Map<String, Object> boat = new LinkedHashMap<>();
			boat.put("count", entry.getValue());
			boat.put("type", entry.getKey());
			sunkBoats.add(boat);
		}

		// Criar a lista de acertos em barcos que não foram afundados
		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			Map<String, Object> boat = new LinkedHashMap<>();
			boat.put("hits", entry.getValue());
			boat.put("type", entry.getKey());
			boatHits.add(boat);
		}

		// Criar o mapa para o JSON, pela mesma ordem do protocolo dado ao LLM
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("validShots", validShots);
		response.put("sunkBoats", sunkBoats);
		response.put("repeatedShots", repeatedShots);
		response.put("outsideShots", outsideShots);
		response.put("hitsOnBoats", boatHits);
		response.put("missedShots", missedShots);

		// Serializar o JSON utilizando Jackson
		String jsonString;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			jsonString = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}

		System.out.println(jsonString);
		System.out.println();

		// Retornar o JSON
		return jsonString;
	}
}