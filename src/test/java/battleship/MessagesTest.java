package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes unitários JUNIT5 para a classe Messages")
public class MessagesTest {

    private Locale defaultLocale;

    @BeforeEach
    void setUp() {
        defaultLocale = Locale.getDefault();
    }

    @AfterEach
    void tearDown() throws Exception {
        Locale.setDefault(defaultLocale);
        setBundleToNull();
        Messages.setLanguage(defaultLocale.toLanguageTag());
    }

    private void setBundleToNull() throws NoSuchFieldException, IllegalAccessException {
        Field bundleField = Messages.class.getDeclaredField("bundle");
        bundleField.setAccessible(true);
        bundleField.set(null, null);
    }

    /**
     * Obtém automaticamente uma chave válida do ficheiro messages.properties
     * (procura uma com {0} se withParams for true, ou qualquer chave simples caso contrário).
     */
    private String getExistingKey(boolean withParams) {
        ResourceBundle rb = ResourceBundle.getBundle("messages", Locale.getDefault());
        Enumeration<String> keys = rb.getKeys();
        String fallbackKey = null;

        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (fallbackKey == null) {
                fallbackKey = key;
            }
            String value = rb.getString(key);
            if (withParams && value.contains("{0}")) {
                return key;
            }
            if (!withParams && !value.contains("{0}")) {
                return key;
            }
        }
        return fallbackKey;
    }

    @Test
    @DisplayName("Deve obter uma mensagem simples válida com o idioma por omissão")
    void testGetMessageSimple() {
        String key = getExistingKey(false);
        assertNotNull(key, "O ficheiro messages.properties não deve estar vazio");

        String message = Messages.getMessage(key);

        assertNotNull(message, "A mensagem devolvida não deve ser nula");
        assertFalse(message.isEmpty(), "A mensagem devolvida não deve estar vazia");
    }

    @Test
    @DisplayName("Deve formatar corretamente uma mensagem com parâmetros dinâmicos")
    void testGetMessageWithParams() {
        String key = getExistingKey(true);
        assertNotNull(key, "O ficheiro messages.properties não deve estar vazio");

        String param = "Porta-Aviões";
        String message = Messages.getMessage(key, param);

        assertNotNull(message);
        // Se existir uma chave com {0} no ficheiro, valida que o parâmetro foi inserido
        if (ResourceBundle.getBundle("messages").getString(key).contains("{0}")) {
            assertTrue(message.contains(param), "A mensagem formatada deve conter o parâmetro passado");
        }
    }

    @Test
    @DisplayName("Deve alterar o idioma corretamente através de setLanguage")
    void testSetLanguage() {
        String key = getExistingKey(false);

        Messages.setLanguage("pt");
        String msgPt = Messages.getMessage(key);

        Messages.setLanguage("en");
        String msgEn = Messages.getMessage(key);

        assertNotNull(msgPt);
        assertNotNull(msgEn);
        assertNotEquals(msgPt, msgEn, "As mensagens em idiomas diferentes devem ser distintas");
    }

    @Test
    @DisplayName("Deve lançar MissingResourceException quando a chave não existe")
    void testGetMessageInvalidKey() {
        assertThrows(MissingResourceException.class, () -> {
            Messages.getMessage("chave.inexistente.teste.123");
        }, "Deve lançar exceção para chaves inexistentes");
    }

    @Test
    @DisplayName("Deve reinicializar o bundle caso este se encontre nulo (cobertura de ramos)")
    void testEnsureBundleInitializedWhenNull() throws Exception {
        setBundleToNull();

        String key = getExistingKey(false);
        String message = Messages.getMessage(key);

        assertNotNull(message, "O bundle deve ser automaticamente reinicializado se estiver null");
    }

    @Test
    @DisplayName("Deve instanciar a classe Messages para garantir 100% de cobertura de métodos")
    void testConstructorCoverage() {
        Messages messagesInstance = new Messages();
        assertNotNull(messagesInstance);
    }
}