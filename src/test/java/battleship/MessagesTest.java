package battleship;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

class MessagesTest {

    @Test
    @DisplayName("Testa se consegue obter uma mensagem simples após definir a linguagem para PT")
    void testSetLanguageAndGetMessagePT() {
        Messages.setLanguage("pt");
        String resultado = Messages.getMessage("msg.goodbye");

        assertNotNull(resultado, "A mensagem não devia ser nula");
        assertEquals("Bons ventos!", resultado, "A mensagem deve corresponder ao texto em português");
    }

    @Test
    @DisplayName("Testa se consegue obter uma mensagem após definir a linguagem para EN")
    void testSetLanguageAndGetMessageEN() {
        Messages.setLanguage("en");
        String resultado = Messages.getMessage("msg.goodbye");

        assertNotNull(resultado, "A mensagem não devia ser nula");
        assertEquals("Fair winds!", resultado, "A mensagem deve corresponder ao texto em inglês");
    }

    @Test
    @DisplayName("Testa a formatação de mensagens com parâmetros dinâmicos")
    void testGetMessageWithParameters() {
        Messages.setLanguage("pt");
        String resultado = Messages.getMessage("pdf.success", "jogo.pdf");

        assertNotNull(resultado, "A mensagem com parâmetros não devia ser nula");
        assertTrue(resultado.startsWith("PDF criado com sucesso:"), "A mensagem devia conter o texto base");
    }

    @Test
    @DisplayName("Testa se a linguagem padrão funciona sem chamar o setLanguage explicitamente")
    void testDefaultLanguage() {
        String resultado = Messages.getMessage("menu.header");

        assertNotNull(resultado, "O bundle deve carregar a linguagem por omissão e encontrar a chave");
    }

    @Test
    @DisplayName("Testa a inicialização de segurança quando o bundle é nulo (para 100% Branch Coverage)")
    void testEnsureBundleInitialized() throws Exception {
        // Usamos Reflection para forçar a variável privada 'bundle' a ficar a null
        Field bundleField = Messages.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(null, null); // Coloca a null

        // Ao pedir uma mensagem, o método ensureBundleInitialized() vai apanhar o bundle a null e reinicializá-lo
        String resultado = Messages.getMessage("msg.goodbye");

        assertNotNull(resultado, "A mensagem não devia ser nula após a reinicialização forçada");
    }

    @Test
    @DisplayName("Testa o comportamento quando é pedida uma chave inexistente")
    void testMissingKey() {
        Messages.setLanguage("pt");

        assertThrows(java.util.MissingResourceException.class, () -> {
            Messages.getMessage("chave.que.nao.existe");
        }, "Deveria lançar uma exceção ao tentar obter uma chave inexistente");
    }
}