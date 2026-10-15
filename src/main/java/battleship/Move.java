package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Uma jogada (rajada de tiros) e respetivos resultados.
 */
public class Move implements IMove {

	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{number=" + number + ", shots=" + shots.size() + ", results=" + shotResults.size() + '}';
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
	 * Analisa os resultados da rajada e imprime/devolve o JSON de resposta para o LLM.
	 *
	 * @param verbose não utilizado (mantido por compatibilidade com IMove)
	 * @return o JSON com o resultado da rajada
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunk = new LinkedHashMap<>(); // navios afundados, por tipo
		Map<String, Integer> hits = new LinkedHashMap<>(); // acertos sem afundar, por tipo

		for (IGame.ShotResult result : shotResults) {
			if (!result.valid())
				continue; // fora do tabuleiro

			if (result.repeated()) {
				repeatedShots++;
			} else {
				validShots++;
				if (result.ship() == null) {
					missedShots++;
				} else {
					String type = result.ship().getCategory();
					// o tiro que afunda conta só em sunkBoats
					((result.sunk()) ? sunk : hits).merge(type, 1, Integer::sum);
				}
			}
		}

		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		Map<String, Object> response = new LinkedHashMap<>();
		response.put("validShots", validShots);
		response.put("sunkBoats", toList(sunk, "count"));
		response.put("repeatedShots", repeatedShots);
		response.put("outsideShots", outsideShots);
		response.put("hitsOnBoats", toList(hits, "hits"));
		response.put("missedShots", missedShots);

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		String json;
		try {
			json = mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}

		System.out.println(json);
		System.out.println();
		return json;
	}

	/** Converte um mapa tipo->quantidade numa lista de {countKey: n, "type": tipo}. */
	private static List<Map<String, Object>> toList(Map<String, Integer> map, String countKey) {
		List<Map<String, Object>> list = new ArrayList<>();
		map.forEach((type, n) -> {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put(countKey, n);
			entry.put("type", type);
			list.add(entry);
		});
		return list;
	}
}