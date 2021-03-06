package org.apertereports.util;

import java.util.*;
import org.apertereports.dao.ReportTemplateDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
public final class VaadinUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReportTemplateDAO.class);
    private static final Map<Thread, Locale> LOCALE_THREAD_MAP = new HashMap<Thread, Locale>();

    private VaadinUtil() {
    }
    
    public static synchronized void setThreadLocale(Locale l) {
        LOCALE_THREAD_MAP.put(Thread.currentThread(), l);
    }

    public static synchronized void unsetThreadLocale() {
        LOCALE_THREAD_MAP.remove(Thread.currentThread());
    }

    public static String getValue(String key, Object... params) {
        if (key == null) {
            logger.warn("key is null");
            key = "";
        }
        String v = getValue(key);
        StringBuilder sb = new StringBuilder();
        Formatter fm = new Formatter(sb, nvl(LOCALE_THREAD_MAP.get(Thread.currentThread()), Locale.getDefault()));
        fm.format(v, params);
        return sb.toString();
    }

    /**
     * Returns value for given key from ui-message.properties file or another
     * localized ui-message properties file. If the key given is null, then
     * an empty string is returned
     * @param key Key
     * @return Value for given key
     */
    public static String getValue(String key) {
        if (key == null) {
            return "";
        }
        ResourceBundle bundle = ResourceBundle.getBundle("ui-messages",
                nvl(LOCALE_THREAD_MAP.get(Thread.currentThread()), Locale.getDefault()));
        if (bundle.containsKey(key)) {
            return nvl(bundle.getString(key), key);
        }
        return key;
    }

    public static <T> T nvl(T... values) {
        for (T t : values) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }
}
