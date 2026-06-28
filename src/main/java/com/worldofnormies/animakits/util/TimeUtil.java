package com.worldofnormies.animakits.util;

public class TimeUtil {

    public static long parseDuration(String raw) {
        if (raw == null || raw.isBlank()) return -2;
        String trimmed = raw.trim().toLowerCase();
        if (trimmed.equals("-1")) return -1;

        try { return Long.parseLong(trimmed); } catch (NumberFormatException ignored) {}

        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:(\\d+)d)?(?:(\\d+)h)?(?:(\\d+)m)?(?:(\\d+)s)?").matcher(trimmed);
        if (!m.matches()) return -2;

        long days = m.group(1) != null ? Long.parseLong(m.group(1)) : 0;
        long hours = m.group(2) != null ? Long.parseLong(m.group(2)) : 0;
        long minutes = m.group(3) != null ? Long.parseLong(m.group(3)) : 0;
        long seconds = m.group(4) != null ? Long.parseLong(m.group(4)) : 0;

        long total = days * 86400L + hours * 3600L + minutes * 60L + seconds;
        return (total > 0 || trimmed.equals("0s") || trimmed.equals("0")) ? total : -2;
    }

    public static String formatDuration(long seconds) {
        if (seconds == 0) return "0s";
        if (seconds == -1) return "Permanent";
        if (seconds < 0) return "None";

        long d = seconds / 86400, h = (seconds % 86400) / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
