package battleship;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exports the history of moves (bursts of shots) of a Battleship game to a PDF file.
 */
public class PdfExporter {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
    private static final Font CELL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    private PdfExporter() {
        // utility class
    }

    /**
     * Generates a PDF file with all the moves registered in the game.
     *
     * @param game       the game whose moves are to be printed
     * @param outputPath the path of the PDF file to create (e.g. "jogadas.pdf")
     * @throws IOException if the file cannot be written
     */
    public static void export(IGame game, String outputPath) throws IOException {
        assert game != null;
        assert outputPath != null;

        List<IMove> moves = game.getAlienMoves();

        Document document = new Document(PageSize.A4);
        try (FileOutputStream out = new FileOutputStream(outputPath)) {
            PdfWriter.getInstance(document, out);
            document.open();

            // Título e data
            Paragraph title = new Paragraph("Batalha Naval - Registo de Jogadas", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph date = new Paragraph("Gerado em " + now, CELL_FONT);
            date.setAlignment(Element.ALIGN_CENTER);
            date.setSpacingAfter(15);
            document.add(date);

            // Tabela das rajadas: Jogada | Tiros | Resultado
            PdfPTable table = new PdfPTable(new float[]{1.2f, 3f, 6f});
            table.setWidthPercentage(100);
            table.setHeaderRows(1);
            addHeaderCell(table, "Jogada");
            addHeaderCell(table, "Tiros");
            addHeaderCell(table, "Resultado");

            for (IMove move : moves) {
                table.addCell(new PdfPCell(new Phrase("Nº " + move.getNumber(), CELL_FONT)));
                table.addCell(new PdfPCell(new Phrase(shotsToString(move), CELL_FONT)));
                table.addCell(new PdfPCell(new Phrase(describe(move), CELL_FONT)));
            }
            document.add(table);

            // Resumo final
            Paragraph summaryTitle = new Paragraph("Resumo do jogo", BOLD_FONT);
            summaryTitle.setSpacingBefore(15);
            document.add(summaryTitle);
            document.add(new Paragraph("Total de jogadas: " + moves.size(), CELL_FONT));
            document.add(new Paragraph("Tiros certeiros em navios: " + game.getHits(), CELL_FONT));
            document.add(new Paragraph("Navios afundados: " + game.getSunkShips(), CELL_FONT));
            document.add(new Paragraph("Navios ainda a flutuar: " + game.getRemainingShips(), CELL_FONT));
            document.add(new Paragraph("Tiros repetidos: " + game.getRepeatedShots(), CELL_FONT));
            document.add(new Paragraph("Tiros fora do tabuleiro: " + game.getInvalidShots(), CELL_FONT));
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar o PDF: " + e.getMessage(), e);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    /**
     * Lists the shots of a move, e.g. "A5, C10, F5".
     */
    private static String shotsToString(IMove move) {
        return move.getShots().stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Builds a human readable summary of the result of a move, based on its shot results.
     * It does not call processEnemyFire, to avoid printing to the console again.
     */
    private static String describe(IMove move) {
        int water = 0;
        int repeated = 0;
        int outside = 0;
        Map<String, Integer> hits = new LinkedHashMap<>();
        Map<String, Integer> sunk = new LinkedHashMap<>();

        for (IGame.ShotResult r : move.getShotResults()) {
            if (!r.valid()) {
                outside++;
            } else if (r.repeated()) {
                repeated++;
            } else if (r.ship() == null) {
                water++;
            } else {
                String category = r.ship().getCategory();
                hits.merge(category, 1, Integer::sum);
                if (r.sunk())
                    sunk.merge(category, 1, Integer::sum);
            }
        }

        List<String> parts = new ArrayList<>();
        sunk.forEach((category, n) -> parts.add(n + " " + category + (n > 1 ? "s" : "") + " ao fundo"));
        hits.forEach((category, n) -> {
            if (!sunk.containsKey(category))
                parts.add(n + " tiro" + (n > 1 ? "s" : "") + " num(a) " + category);
        });
        if (water > 0)
            parts.add(water + " tiro" + (water > 1 ? "s" : "") + " na água");
        if (repeated > 0)
            parts.add(repeated + " tiro" + (repeated > 1 ? "s" : "") + " repetido" + (repeated > 1 ? "s" : ""));
        if (outside > 0)
            parts.add(outside + " tiro" + (outside > 1 ? "s" : "") + " exterior" + (outside > 1 ? "es" : ""));

        return parts.isEmpty() ? "-" : String.join(", ", parts);
    }

    private static void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(0x1F, 0x4E, 0x79));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }
}