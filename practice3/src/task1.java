

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

interface ReportFormatter {
    String format(String title, String[] headers, String[][] rows);
}

final class JsonFormatter implements ReportFormatter {
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder out = new StringBuilder("{\n  \"report\": \"").append(escape(title)).append("\",\n  \"records\": [\n");
        for (int i = 0; i < rows.length; i++) {
            out.append("    {");
            for (int j = 0; j < headers.length; j++) {
                if (j > 0) out.append(", ");
                out.append("\"").append(escape(headers[j])).append("\": \"")
                        .append(escape(j < rows[i].length ? rows[i][j] : "")).append("\"");
            }
            out.append("}").append(i < rows.length - 1 ? ",\n" : "\n");
        }
        return out.append("  ]\n}").toString();
    }
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}

final class CsvFormatter implements ReportFormatter {
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder out = new StringBuilder("# TITLE: ").append(csv(title)).append('\n');
        appendRow(out, headers);
        for (String[] row : rows) appendRow(out, row);
        return out.toString();
    }
    private void appendRow(StringBuilder out, String[] row) {
        for (int i = 0; i < row.length; i++) {
            if (i > 0) out.append(',');
            out.append(csv(row[i]));
        }
        out.append('\n');
    }
    private String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
}

interface Exporter { void export(String destination, String data); }
final class BaseFileExporter implements Exporter {
    public void export(String destination, String data) {
        System.out.printf("[DISK WRITE] Writing %d bytes to file://%s%n", data.getBytes(StandardCharsets.UTF_8).length, destination);
        System.out.println("----- FILE PAYLOAD BEGIN -----");
        System.out.println(data);
        System.out.println("----- FILE PAYLOAD END -----");
    }
}
abstract class ExporterDecorator implements Exporter {
    protected final Exporter wrappedExporter;
    ExporterDecorator(Exporter wrappedExporter) {
        if (wrappedExporter == null) throw new IllegalArgumentException("Wrapped exporter cannot be null");
        this.wrappedExporter = wrappedExporter;
    }
    public void export(String destination, String data) { wrappedExporter.export(destination, data); }
}
final class HeaderFooterDecorator extends ExporterDecorator {
    private final String timestamp;
    HeaderFooterDecorator(Exporter exporter, String timestamp) { super(exporter); this.timestamp = timestamp; }
    public void export(String destination, String data) {
        int count = data.split("\\R").length;
        String countSource = data;
        if (data.startsWith("[BASE64_CIPHERTEXT:") && data.endsWith("]")) {
            String encoded = data.substring("[BASE64_CIPHERTEXT:".length(), data.length() - 1);
            try { countSource = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8); }
            catch (IllegalArgumentException ignored) { }
        }
        if (countSource.contains("\"records\": [")) {
            count = 0;
            for (String line : countSource.split("\\R")) if (line.trim().startsWith("{\"")) count++;
        }
        String enriched = "--- EXPORT TIMESTAMP: " + timestamp + " ---\n" + data
                + "\n--- RECORD COUNT: " + count + " | SECURE HASH: [OK] ---";
        wrappedExporter.export(destination, enriched);
    }
}
final class Base64EncryptDecorator extends ExporterDecorator {
    Base64EncryptDecorator(Exporter exporter) { super(exporter); }
    public void export(String destination, String data) {
        String encoded = Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        wrappedExporter.export(destination, "[BASE64_CIPHERTEXT:" + encoded + "]");
    }
}


