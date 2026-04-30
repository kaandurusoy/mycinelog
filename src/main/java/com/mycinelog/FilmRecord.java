package com.mycinelog;

// JavaFX tarafinda bir filmi temsil eden veri modeli.
public class FilmRecord {
    private String ad;
    private double puan;
    private long oy;
    private String tur;
    private String izlenmeTarihi;

    public FilmRecord(String ad, double puan, long oy, String tur, String izlenmeTarihi) {
        this.ad = ad;
        this.puan = puan;
        this.oy = oy;
        this.tur = tur;
        this.izlenmeTarihi = izlenmeTarihi;
    }

    public String getAd() { return ad; }
    public double getPuan() { return puan; }
    public long getOy() { return oy; }
    public String getTur() { return tur; }
    public String getIzlenmeTarihi() { return izlenmeTarihi; }
}
