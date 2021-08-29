package com.orsond.mchat;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;

public class MessageSaver {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private final LoadingCache<String, PrintWriter> dmWriterCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(5))
            .maximumSize(50)
            .removalListener(removalNotification -> {
                PrintWriter writer = (PrintWriter) removalNotification.getValue();
                writer.flush();
                writer.close();
            })
            .build(new CacheLoader<>() {
        @Override
        public PrintWriter load(String s) throws Exception {
            File file = new File(dms, s + ".txt");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    return new PrintWriter(new StringWriter());
                }
            }
            return new PrintWriter(new FileOutputStream(file, true));
        }
    });

    private final LoadingCache<String, PrintWriter> gcWriterCache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofSeconds(5))
            .maximumSize(50)
            .removalListener(removalNotification -> {
                PrintWriter writer = (PrintWriter) removalNotification.getValue();
                writer.flush();
                writer.close();
            })
            .build(new CacheLoader<>() {
                @Override
                public PrintWriter load(String s) throws Exception {
                    File file = new File(gc, s + ".txt");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            return new PrintWriter(new StringWriter());
                        }
                    }
                    return new PrintWriter(new FileOutputStream(file, true));
                }
            });

    private final File dms;
    private final File gc;

    public MessageSaver(File path) {
        this.dms = new File(path, "out/dms");
        if (this.dms.exists()) {
            this.dms.delete();
        }
        this.dms.mkdirs();

        this.gc = new File(path, "out/gc");
        if (this.gc.exists()) {
            this.gc.delete();
        }
        this.gc.mkdirs();
    }

    public void save(Message message) {
        if ("[everyone]".equals(message.to) && message.from != null) {
            PrintWriter writer = gcWriterCache.getUnchecked(message.from);
            writer.println(String.format("[%s] %s", DATE_FORMAT.format(message.date), message.text));
        } else if (message.from == null || message.to == null) {
            String conversation = MoreObjects.firstNonNull(message.from, message.to);
            if (message.from == null) {
                PrintWriter writer = dmWriterCache.getUnchecked(conversation);
                writer.println(String.format("[%s] you said: %s", DATE_FORMAT.format(message.date), message.text));
            } else {
                PrintWriter writer = dmWriterCache.getUnchecked(conversation);
                writer.println(String.format("[%s] %s: %s", DATE_FORMAT.format(message.date), message.from, message.text));
            }
        }
    }

    public void flush() {
        dmWriterCache.invalidateAll();
        gcWriterCache.invalidateAll();
    }
}
