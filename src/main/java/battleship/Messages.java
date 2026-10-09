package battleship;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
    private static final String BASE_NAME = "messages";

    // Inicialização estática por omissão com o Locale padrão do sistema
    private static ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, Locale.getDefault());

    // Define a linguagem
    public static void setLanguage(String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);
        bundle = ResourceBundle.getBundle(BASE_NAME, locale);
    }

    // Obtém a mensagem simples
    public static String getMessage(String key) {
        ensureBundleInitialized();
        return bundle.getString(key);
    }

    // Obtém a mensagem com parâmetros dinâmicos
    public static String getMessage(String key, Object... params) {
        String p = getMessage(key);
        return MessageFormat.format(p, params);
    }

    // Verificação de segurança caso o bundle de algum modo permaneça nulo
    private static void ensureBundleInitialized() {
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BASE_NAME, Locale.getDefault());
        }
    }
}