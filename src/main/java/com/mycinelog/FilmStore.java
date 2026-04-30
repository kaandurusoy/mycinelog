package com.mycinelog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// filmler.txt dosyasina okuma/yazma yapan yardimci sinif.
public class FilmStore {
    private final Path path;

    public FilmStore(Path path) {
        this.path = path;
    }

    public List<FilmRecord> yukle() {
        List<FilmRecord> liste = new ArrayList<>();
        if (!Files.exists(path)) {
            return liste;
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String satir;
            while ((satir = reader.readLine()) != null) {
                String[] p = satir.split("\\|");
                if (p.length < 3) continue;

                String ad = p[0];
                double puan = Double.parseDouble(p[1]);
                long oy = 0L;
                String tur = "Bilinmiyor";
                String tarih;

                if (p.length >= 5) {
                    oy = Long.parseLong(p[2]);
                    tur = p[3];
                    tarih = p[4];
                } else if (p.length == 4) {
                    oy = Long.parseLong(p[2]);
                    tarih = p[3];
                } else {
                    tarih = p[2];
                }
                liste.add(new FilmRecord(ad, puan, oy, tur, tarih));
            }
        } catch (Exception ignored) {
            // Hatali satirlar oldugunda uygulama tamamen durmasin.
        }
        return liste;
    }

    public void kaydet(List<FilmRecord> filmler) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (FilmRecord f : filmler) {
                writer.write(f.getAd() + "|" + f.getPuan() + "|" + f.getOy() + "|" + f.getTur() + "|" + f.getIzlenmeTarihi());
                writer.newLine();
            }
        }
    }
}
