// Film sınıfı, her bir filmi nesne olarak tutmak için kullanılır.
public class Film {
    // Film adı bilgisini saklar.
    private String filmAdi;

    // IMDb puanını saklar.
    private double imdbPuani;

    // Filmin izlenme tarihini saklar.
    private String izlenmeTarihi;

    // IMDb oy sayisini saklar.
    private long imdbOySayisi;

    // Filmin tur bilgisini saklar.
    private String tur;

    // Kurucu metot: Yeni bir Film nesnesi oluştururken ilk değerleri atar.
    public Film(String filmAdi, double imdbPuani, String izlenmeTarihi) {
        this.filmAdi = filmAdi;
        this.imdbPuani = imdbPuani;
        this.izlenmeTarihi = izlenmeTarihi;
        this.imdbOySayisi = 0;
        this.tur = "Bilinmiyor";
    }

    // Kurucu metot: Oy sayisi bilgisiyle birlikte Film nesnesi olusturur.
    public Film(String filmAdi, double imdbPuani, String izlenmeTarihi, long imdbOySayisi) {
        this.filmAdi = filmAdi;
        this.imdbPuani = imdbPuani;
        this.izlenmeTarihi = izlenmeTarihi;
        this.imdbOySayisi = imdbOySayisi;
        this.tur = "Bilinmiyor";
    }

    // Kurucu metot: Oy sayisi ve tur bilgisiyle Film nesnesi olusturur.
    public Film(String filmAdi, double imdbPuani, String izlenmeTarihi, long imdbOySayisi, String tur) {
        this.filmAdi = filmAdi;
        this.imdbPuani = imdbPuani;
        this.izlenmeTarihi = izlenmeTarihi;
        this.imdbOySayisi = imdbOySayisi;
        this.tur = (tur == null || tur.trim().isEmpty()) ? "Bilinmiyor" : tur.trim();
    }

    // Film adını döndürür.
    public String getFilmAdi() {
        return filmAdi;
    }

    // Film adını günceller.
    public void setFilmAdi(String filmAdi) {
        this.filmAdi = filmAdi;
    }

    // IMDb puanını döndürür.
    public double getImdbPuani() {
        return imdbPuani;
    }

    // IMDb puanını günceller.
    public void setImdbPuani(double imdbPuani) {
        this.imdbPuani = imdbPuani;
    }

    // İzlenme tarihini döndürür.
    public String getIzlenmeTarihi() {
        return izlenmeTarihi;
    }

    // İzlenme tarihini günceller.
    public void setIzlenmeTarihi(String izlenmeTarihi) {
        this.izlenmeTarihi = izlenmeTarihi;
    }

    // IMDb oy sayisini dondurur.
    public long getImdbOySayisi() {
        return imdbOySayisi;
    }

    // IMDb oy sayisini gunceller.
    public void setImdbOySayisi(long imdbOySayisi) {
        this.imdbOySayisi = imdbOySayisi;
    }

    // Film tur bilgisini dondurur.
    public String getTur() {
        return tur;
    }

    // Film tur bilgisini gunceller.
    public void setTur(String tur) {
        this.tur = (tur == null || tur.trim().isEmpty()) ? "Bilinmiyor" : tur.trim();
    }
}
