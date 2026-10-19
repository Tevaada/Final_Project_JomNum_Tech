package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {

    private CsvUtil() {
    }

    public static ArrayList<ArrayList<String>> read(Path file)
            throws IOException {

        ArrayList<ArrayList<String>> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            ArrayList<String> record = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            boolean afterClosingQuote = false;
            boolean recordStarted = false;
            int lineNumber = 1;
            int value;

            while ((value = reader.read()) != -1) {
                char character = (char) value;

                if (inQuotes) {
                    if (character == '"') {
                        reader.mark(1);
                        int next = reader.read();

                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            afterClosingQuote = true;

                            if (next != -1) {
                                reader.reset();
                            }
                        }
                    } else {
                        field.append(character);

                        if (character == '\n') {
                            lineNumber++;
                        }
                    }

                    continue;
                }

                if (afterClosingQuote) {
                    if (character == ',') {
                        record.add(field.toString());
                        field.setLength(0);
                        afterClosingQuote = false;
                        recordStarted = true;
                        continue;
                    }

                    if (character == '\r' || character == '\n') {
                        if (character == '\r') {
                            consumeOptionalLineFeed(reader);
                        }

                        record.add(field.toString());
                        records.add(record);
                        record = new ArrayList<>();
                        field.setLength(0);
                        afterClosingQuote = false;
                        recordStarted = false;
                        lineNumber++;
                        continue;
                    }

                    throw new IOException(
                        "Invalid character after a closing quote at line "
                        + lineNumber + "."
                    );
                }

                if (character == '"') {
                    if (field.length() != 0) {
                        throw new IOException(
                            "Unexpected quote at line " + lineNumber + "."
                        );
                    }

                    inQuotes = true;
                    recordStarted = true;
                } else if (character == ',') {
                    record.add(field.toString());
                    field.setLength(0);
                    recordStarted = true;
                } else if (character == '\r' || character == '\n') {
                    if (character == '\r') {
                        consumeOptionalLineFeed(reader);
                    }

                    record.add(field.toString());
                    records.add(record);
                    record = new ArrayList<>();
                    field.setLength(0);
                    recordStarted = false;
                    lineNumber++;
                } else {
                    field.append(character);
                    recordStarted = true;
                }
            }

            if (inQuotes) {
                throw new IOException(
                    "Unclosed quoted field at line " + lineNumber + "."
                );
            }

            if (recordStarted || afterClosingQuote || !record.isEmpty()) {
                record.add(field.toString());
                records.add(record);
            }
        }

        return records;
    }

    public static void write(
            Path file, List<? extends List<String>> records)
            throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            for (List<String> record : records) {
                for (int index = 0; index < record.size(); index++) {
                    if (index > 0) {
                        writer.write(',');
                    }

                    writer.write(escape(record.get(index)));
                }

                writer.newLine();
            }
        }
    }

    private static String escape(String value) {
        if (value == null) {
            throw new IllegalArgumentException(
                "CSV values cannot be null."
            );
        }

        if (value.indexOf(',') < 0
                && value.indexOf('"') < 0
                && value.indexOf('\r') < 0
                && value.indexOf('\n') < 0) {
            return value;
        }

        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static void consumeOptionalLineFeed(BufferedReader reader)
            throws IOException {

        reader.mark(1);
        int next = reader.read();

        if (next != '\n' && next != -1) {
            reader.reset();
        }
    }
}
