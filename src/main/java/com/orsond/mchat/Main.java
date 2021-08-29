package com.orsond.mchat;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class Main {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void main(String[] args) throws IOException {
        if (args[0] == null) {
            System.err.println("first argument must be a minecraft/log directory");
        }
        File path = new File(args[0]);
        MessageSaver messageSaver = new MessageSaver(path);
        for (File file : logFiles(path)) {
            BufferedReader bufferedReader = open(file);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.contains("[CHAT]")) {
                    continue;
                }
                Message message = Message.parse(fileDate(file), line);
                if (message == null) continue;
                messageSaver.save(message);
            }
        }
        messageSaver.flush();
    }

    private static List<File> logFiles(File path) {
        return Arrays.stream(Objects.requireNonNull(path.list()))
                .map(s -> new File(path, s))
                .filter(file -> {
                    if (file.isDirectory()) {
                        return false;
                    }
                    try {
                        fileDate(file);
                        return true;
                    } catch (RuntimeException e) {
                        System.err.println(file + " was not a log");
                        return false;
                    }
                })
                .sorted(Comparator.comparing(Main::fileDate))
                .collect(Collectors.toList());
    }

    private static BufferedReader open(File file) {
        // TODO: split on [01:13:46] as a token ???
        InputStream is;
        try {
            is = new FileInputStream(file);
            if (file.getName().endsWith(".gz")) {
                is = new GZIPInputStream(is);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new BufferedReader(new InputStreamReader(is));
    }

    public static Date fileDate(File file) {
        String date = file.getName().substring(0, file.getName().indexOf('.') + 1);
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
