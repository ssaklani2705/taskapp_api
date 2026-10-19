package com.webelement.taskapp.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {
    private static final long TTL_MS = 2 * 60 * 1000L;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private static class Entry {
        final String answer; final long expiresAt;
        Entry(String a, long e) { answer = a; expiresAt = e; }
    }

    public Map<String, String> create() throws IOException {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(e -> e.getValue().expiresAt < now);   // cleanup
        String text = String.valueOf(100000 + random.nextInt(900000));
        String id = UUID.randomUUID().toString();
        store.put(id, new Entry(text, now + TTL_MS));
        Map<String, String> res = new HashMap<>();
        res.put("captchaId", id);
        res.put("image", toBase64Png(text));
        return res;
    }

    /** One-time use: removed whether right or wrong. */
    public boolean verifyAndConsume(String id, String answer) {
        if (id == null || answer == null) return false;
        Entry e = store.remove(id);
        return e != null && e.expiresAt >= System.currentTimeMillis()
                && e.answer.equalsIgnoreCase(answer.trim());
    }

    private String toBase64Png(String text) throws IOException {
        int w = 140, h = 44;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE); g.fillRect(0, 0, w, h);
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.drawLine(random.nextInt(w), random.nextInt(h), random.nextInt(w), random.nextInt(h));
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        for (int i = 0; i < text.length(); i++) {
            g.setColor(new Color(random.nextInt(120), random.nextInt(120), random.nextInt(120)));
            g.drawString(String.valueOf(text.charAt(i)), 10 + i * 20, 28 + random.nextInt(8));
        }
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }

}
