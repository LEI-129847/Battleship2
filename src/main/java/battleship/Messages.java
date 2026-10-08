package battleship;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {
    private static ResourceBundle bundle;

    // Define a linguagem
    public static void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    // Obtém a mensagem simples
    public static String getMessage(String key) {
        return bundle.getString(key);
    }

    // Obtém a mensagem com parâmetros dinâmicos
    public static String getMessage(String key, Object... params) {
        String p = bundle.getString(key);
        return MessageFormat.format(p, params);
    }
}

