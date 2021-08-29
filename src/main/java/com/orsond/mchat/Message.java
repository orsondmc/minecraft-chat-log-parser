package com.orsond.mchat;

import java.util.Date;
import java.util.Locale;

public class Message {
    public final Date date;
    public final String from;
    public final String to;
    public final String text;

    public Message(Date date, String from, String to, String text) {
        this.date = date;
        this.from = from;
        this.to = to;
        this.text = text.trim();
    }

    public static Message parse(Date date, String line) {
        Message message;
        if (line.toLowerCase().contains(" [chat] <")) {
            message = Message.generalChat(date, line);
        } else if (line.toLowerCase().contains(" whispers:")) {
            message = whisperFrom(date, line);
        } else if (line.toLowerCase().contains("[chat] to ")) {
            message = whisperTo(date, line);
        } else {
            message = null;
        }
        return message;
    }

    private static Message whisperTo(Date date, String raw) {
        int i = raw.toLowerCase().indexOf("[chat] to ");
        int endIndex = raw.indexOf(":", i);
        if (i == -1) {
            return null;
        }
        if (endIndex == -1) {
            return null;
        }
        String to = raw.substring(i + 10, endIndex);
        return new Message(dateFrom(date, raw), null, to, raw.substring(raw.indexOf(":", i + 1) + 1));
    }

    private static Message whisperFrom(Date date, String raw) {
        int start = raw.toLowerCase().indexOf("[chat] ");
        int end = raw.toLowerCase().indexOf(" whispers:", start);
        String from = raw.substring(start + 7, end);
        return new Message(dateFrom(date, raw), from, null, raw.substring(raw.indexOf("whispers: ") + 10));
    }

    public static Message generalChat(Date date, String raw) {
        int i = raw.indexOf('<');
        if (i == -1) {
            return null;
        }
        int endIndex = raw.indexOf('>', i);
        if (endIndex == -1) {
            return null;
        }
        String from = raw.substring(i + 1, endIndex);
        return new Message(dateFrom(date, raw), from, "[everyone]", raw.substring(raw.indexOf("> ") + 2));
    }

    private static Date dateFrom(Date date, String raw) {
        int fromDate = raw.indexOf('[');
        String timeAsString = raw.substring(fromDate + 1, raw.indexOf(']', fromDate));
        String[] atoms = timeAsString.split(":");
        Date newDate = new Date(date.getTime());

        // 10Mar2021 21:52:09.562
        if (atoms[0].contains(" ")) {
            atoms[0] = atoms[0].split(" ")[1];
        }

        try {
            newDate.setHours(((Double)Double.parseDouble(atoms[0])).intValue());
            newDate.setMinutes(((Double)Double.parseDouble(atoms[1])).intValue());
            newDate.setSeconds(((Double)Double.parseDouble(atoms[2])).intValue());
        } catch (NumberFormatException ignored) {}
        return newDate;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("date=").append(date);
        sb.append(", from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
