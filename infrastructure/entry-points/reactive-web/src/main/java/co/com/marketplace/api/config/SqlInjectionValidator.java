package co.com.marketplace.api.config;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class SqlInjectionValidator {

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(" +
                    "(--\\s*[^\\r\\n]*\\b(or|and|union|select|drop|delete|insert|update|exec|alter)\\b)" +
                    "|(/\\*.*\\*/)" +
                    "|(;\\s*(drop|delete|update|insert|alter|truncate|exec|execute|create|rename|grant|revoke)\\b)" +
                    "|(\\bunion\\b\\s+(all\\s+)?select\\b)" +
                    "|(\\b(drop|truncate|alter)\\s+(table|database|schema|index)\\b)" +
                    "|(\\binsert\\s+into\\b)" +
                    "|(\\bdelete\\s+from\\b)" +
                    "|(\\b(or|and)\\b\\s+['\"]?\\w+['\"]?\\s*=\\s*['\"]?\\w+['\"]?\\s*(--|#|/\\*))" +
                    "|(\\b(or|and)\\b\\s+\\d+\\s*=\\s*\\d+(\\s|$|--|#))" +
                    "|(['\"]\\s*(or|and)\\s+['\"]?\\w+['\"]?\\s*=\\s*['\"]?\\w+)" +
                    "|(\\bxp_cmdshell\\b|\\bxp_\\w+|\\bsp_executesql\\b)" +
                    "|(\\binformation_schema\\.)" +
                    "|(\\b(sleep|benchmark|pg_sleep)\\s*\\()" +
                    "|(\\bwaitfor\\s+delay\\b)" +
                    "|(\\bload_file\\s*\\(|\\binto\\s+outfile\\b|\\binto\\s+dumpfile\\b)" +
                    "|(\\bcast\\s*\\(.+\\bas\\b.+\\))" +
                    "|(\\bconvert\\s*\\(.+,.+\\))" +
                    "|(0x[0-9a-f]{16,})" +
                    ")"
    );

    private SqlInjectionValidator() {
    }

    public static boolean containsSqlInjection(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(value).find();
    }

    public static String findOffendingField(Object node, String path) {
        if (node == null) {
            return null;
        }
        if (node instanceof String s) {
            return containsSqlInjection(s) ? (path.isEmpty() ? "body" : path) : null;
        }
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                String childPath = path.isEmpty() ? key : path + "." + key;
                String result = findOffendingField(entry.getValue(), childPath);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
        if (node instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                String result = findOffendingField(list.get(i), path + "[" + i + "]");
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
