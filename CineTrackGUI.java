import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

// Swing tabanli film takip uygulamasinin ana GUI sinifi.
public class CineTrackGUI extends JFrame {

    // Verilerin kalici olarak saklanacagi dosya adi.
    private static final String DOSYA_ADI = "filmler.txt";
    
    // OMDb API anahtaridir (gerekirse kendi anahtarinla degistirebilirsin).
    private static final String OMDB_API_KEY = "thewdb";
    
    // Koyu tema icin ana arka plan rengi.
    private static final Color ARKA_PLAN_RENGI = new Color(24, 26, 31);
    
    // Koyu temada kart/panel yuzeyi icin kullanilan renk.
    private static final Color PANEL_RENGI = new Color(32, 35, 42);
    
    // Tabloda bir sonraki satir icin alternatif arka plan rengi.
    private static final Color PANEL_RENGI_ALTERNATIF = new Color(38, 42, 51);
    
    // Koyu zeminde okunakli acik yazi rengi.
    private static final Color YAZI_RENGI = new Color(230, 232, 236);
    
    // Metin kutulari icin bir ton daha acik arka plan rengi.
    private static final Color ALAN_RENGI = new Color(46, 50, 60);
    
    // Modern gorunum icin butonlarda kullanilan vurgu rengi.
    private static final Color BUTON_RENGI = new Color(76, 110, 245);
    
    // Buton uzerine gelindiginde gorunen daha acik mavi ton.
    private static final Color BUTON_HOVER_RENGI = new Color(98, 128, 252);

    // Film verilerini bellek icinde tutan liste.
    private final ArrayList<Film> filmler = new ArrayList<>();

    // Film adini girmek icin kullanilan metin kutusu.
    private JTextField adField;

    // IMDb puanini girmek icin kullanilan metin kutusu.
    private JTextField puanField;
    
    // IMDb oy sayisini girmek icin kullanilan metin kutusu.
    private JTextField oyField;
    
    // Film turunu girmek/gostermek icin kullanilan metin kutusu.
    private JTextField turField;

    // Izlenme tarihini girmek icin kullanilan metin kutusu.
    private JTextField tarihField;
    
    // Film adina gore arama yapmak icin kullanilan metin kutusu.
    private JTextField aramaField;
    
    // IMDb linki veya film adi girmek icin kullanilan metin kutusu.
    private JTextField imdbKaynakField;

    // Filmleri satirlar halinde gosteren tablo.
    private JTable filmTablosu;

    // Tablo verisini yoneten model.
    private DefaultTableModel tabloModeli;
    
    // Tabloda satir filtreleme/siralama yapmak icin kullanilan sorter.
    private TableRowSorter<DefaultTableModel> tabloSorter;
    
    // IMDb'den veri cekmek icin kullanilan HTTP istemcisi.
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // Pencereyi ve tum bilesenleri olusturan kurucu metot.
    public CineTrackGUI() {
        // Pencerenin basligini belirler.
        setTitle("MyCineLog");

        // Pencere kapatilinca uygulamanin tamamen kapanmasini saglar.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Pencerenin ilk acilis boyutunu ayarlar.
        setSize(980, 600);

        // Pencereyi ekranin ortasinda acar.
        setLocationRelativeTo(null);

        // Ustte giris alani, ortada tablo olacak sekilde ana duzeni kurar.
        setLayout(new BorderLayout());
        
        // Tum pencerenin arka planini koyu tema rengine ayarlar.
        getContentPane().setBackground(ARKA_PLAN_RENGI);

        // Ust kisimdaki metin kutulari ve butonlari olusturur.
        girisPaneliniOlustur();

        // Orta kisimdaki film tablosunu olusturur.
        tabloyuOlustur();

        // Uygulama acilirken dosyadaki verileri yukler.
        dosyadanFilmleriYukle();

        // Yuklenen verileri ekranda tabloya yansitir.
        tabloyuYenile();
    }

    // Film girisi icin label, textfield ve butonlari ust panelde toplar.
    private void girisPaneliniOlustur() {
        // Ust alani birden fazla satira bolmek icin ana panel.
        JPanel girisPaneli = new JPanel(new GridLayout(3, 1, 0, 6));
        girisPaneli.setBackground(PANEL_RENGI);
        girisPaneli.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(58, 63, 74)),
            javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // 1. satir: film bilgisi giris alanlari.
        JPanel formSatiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        formSatiri.setBackground(PANEL_RENGI);

        // 2. satir: islem butonlari.
        JPanel butonSatiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        butonSatiri.setBackground(PANEL_RENGI);

        // 3. satir: arama alani ve arama butonu.
        JPanel aramaSatiri = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        aramaSatiri.setBackground(PANEL_RENGI);

        // "Film Adi" alaninin ne oldugunu gosteren etiket.
        JLabel adLabel = new JLabel("Film Adi:");
        adLabel.setForeground(YAZI_RENGI);
        adLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Kullanicinin film adini yazacagi metin kutusu.
        adField = new JTextField(15);
        metinAlaniniStille(adField);

        // "IMDb Puan" alaninin ne oldugunu gosteren etiket.
        JLabel puanLabel = new JLabel("IMDb Puan:");
        puanLabel.setForeground(YAZI_RENGI);
        puanLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Kullanicinin IMDb puanini yazacagi metin kutusu.
        puanField = new JTextField(8);
        metinAlaniniStille(puanField);

        // "Izlenme Tarihi" alaninin ne oldugunu gosteren etiket.
        JLabel tarihLabel = new JLabel("Izlenme Tarihi:");
        tarihLabel.setForeground(YAZI_RENGI);
        tarihLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Kullanicinin izlenme tarihini yazacagi metin kutusu.
        tarihField = new JTextField(10);
        metinAlaniniStille(tarihField);
        
        // "IMDb Oy" alaninin ne oldugunu gosteren etiket.
        JLabel oyLabel = new JLabel("IMDb Oy:");
        oyLabel.setForeground(YAZI_RENGI);
        oyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Kullanicinin IMDb oy sayisini yazacagi metin kutusu.
        oyField = new JTextField(10);
        metinAlaniniStille(oyField);
        
        // "Tur" alaninin ne oldugunu gosteren etiket.
        JLabel turLabel = new JLabel("Tur:");
        turLabel.setForeground(YAZI_RENGI);
        turLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Kullanicinin film turunu yazacagi metin kutusu.
        turField = new JTextField(14);
        metinAlaniniStille(turField);

        // Girilen bilgileri listeye ekleyen buton.
        JButton ekleButonu = new JButton("Ekle");

        // Tablodaki secili filmi silen buton.
        JButton silButonu = new JButton("Secileni Sil");

        // Tablodaki secili filmi giris kutularindaki degerlerle guncelleyen buton.
        JButton guncelleButonu = new JButton("Secileni Guncelle");

        // Filmleri IMDb puanina gore buyukten kucuge siralayan buton.
        JButton siralaButonu = new JButton("Puana Gore Sirala");

        // Bellekteki verileri dosyaya yazan buton.
        JButton kaydetButonu = new JButton("Dosyaya Kaydet");
        
        // Film adina gore listede arama yapan buton.
        JButton araButonu = new JButton("Ara");
        
        // IMDb linki/film adindan otomatik bilgi ceken buton.
        JButton imdbdenDoldurButonu = new JButton("IMDb'den Doldur");

        // Tum butonlara ortak modern gorunum stilini uygular.
        butonuStille(ekleButonu);
        butonuStille(guncelleButonu);
        butonuStille(silButonu);
        butonuStille(siralaButonu);
        butonuStille(kaydetButonu);
        butonuStille(araButonu);
        butonuStille(imdbdenDoldurButonu);

        // Ekle butonuna basildiginda film ekleme islemini baslatir.
        ekleButonu.addActionListener(e -> filmEkle());

        // Sil butonuna basildiginda secili filmi siler.
        silButonu.addActionListener(e -> seciliFilmiSil());

        // Guncelle butonuna basildiginda secili filmi gunceller.
        guncelleButonu.addActionListener(e -> seciliFilmiGuncelle());

        // Sirala butonuna basildiginda listeyi puana gore siralar.
        siralaButonu.addActionListener(e -> puanaGoreSirala());

        // Kaydet butonuna basildiginda dosyaya kaydetme yapar.
        kaydetButonu.addActionListener(e -> dosyayaKaydet(true));
        
        // Film arama alaninin ne oldugunu gosteren etiket.
        JLabel aramaLabel = new JLabel("Film Ara:");
        aramaLabel.setForeground(YAZI_RENGI);
        aramaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        // Kullanici bu alana film adini yazarak listede arama yapar.
        aramaField = new JTextField(12);
        metinAlaniniStille(aramaField);

        // IMDb kaynagi alaninin ne oldugunu gosteren etiket.
        JLabel imdbKaynakLabel = new JLabel("IMDb Link/Film:");
        imdbKaynakLabel.setForeground(YAZI_RENGI);
        imdbKaynakLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Kullanici bu alana IMDb linki veya film adi yazabilir.
        imdbKaynakField = new JTextField(18);
        metinAlaniniStille(imdbKaynakField);

        // Ara butonuna basildiginda arama metnine gore film bulur.
        araButonu.addActionListener(e -> filmAra());
        
        // IMDb'den Doldur butonu film adi ve puani otomatik getirir.
        imdbdenDoldurButonu.addActionListener(e -> imdbdenDoldur());

        // Arama kutusuna yazildikca tabloyu anlik olarak filtreler.
        aramaField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                canliAramaFiltresiUygula();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                canliAramaFiltresiUygula();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                canliAramaFiltresiUygula();
            }
        });

        // Film giris bilesenlerini ilk satira ekler.
        formSatiri.add(adLabel);
        formSatiri.add(adField);
        formSatiri.add(puanLabel);
        formSatiri.add(puanField);
        formSatiri.add(oyLabel);
        formSatiri.add(oyField);
        formSatiri.add(turLabel);
        formSatiri.add(turField);
        formSatiri.add(tarihLabel);
        formSatiri.add(tarihField);

        // Islem butonlarini ikinci satira ekler.
        butonSatiri.add(ekleButonu);
        butonSatiri.add(guncelleButonu);
        butonSatiri.add(silButonu);
        butonSatiri.add(siralaButonu);
        butonSatiri.add(kaydetButonu);

        // Arama bilesenlerini ucuncu satira ekler.
        aramaSatiri.add(aramaLabel);
        aramaSatiri.add(aramaField);
        aramaSatiri.add(araButonu);
        aramaSatiri.add(imdbKaynakLabel);
        aramaSatiri.add(imdbKaynakField);
        aramaSatiri.add(imdbdenDoldurButonu);

        // Uc satiri ana giris paneline ekler.
        girisPaneli.add(formSatiri);
        girisPaneli.add(butonSatiri);
        girisPaneli.add(aramaSatiri);

        // Ust paneli ana pencerenin kuzey kismina ekler.
        add(girisPaneli, BorderLayout.NORTH);
    }

    // Filmleri listelemek icin JTable ve tablo modelini olusturur.
    private void tabloyuOlustur() {
        // Sadece goruntuleme amacli kolon adlarini belirler.
        String[] kolonlar = {"ID", "Film Adi", "IMDb Puan", "IMDb Oy", "Tur", "Izlenme Tarihi"};

        // Tablo modelini olusturur; ID dahil tum veriler bu modele yazilir.
        tabloModeli = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Kullanicinin dogrudan hucre editlemesini kapatir.
                return false;
            }
        };

        // Modeli kullanacak tablo nesnesini olusturur.
        filmTablosu = new JTable(tabloModeli) {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                // Satirlarin zebra gorunumde olmasi icin cift/tek satirlara farkli ton uygular.
                java.awt.Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? PANEL_RENGI : PANEL_RENGI_ALTERNATIF);
                    c.setForeground(YAZI_RENGI);
                }
                return c;
            }
        };
        
        // Tabloya filtreleme ve siralama kabiliyeti kazandirir.
        tabloSorter = new TableRowSorter<>(tabloModeli);
        filmTablosu.setRowSorter(tabloSorter);
        
        // Tabloda modern koyu tema gorunumu uygular.
        filmTablosu.setBackground(PANEL_RENGI);
        filmTablosu.setForeground(YAZI_RENGI);
        filmTablosu.setSelectionBackground(BUTON_RENGI);
        filmTablosu.setSelectionForeground(Color.WHITE);
        filmTablosu.setGridColor(new Color(58, 63, 74));
        filmTablosu.setRowHeight(32);
        filmTablosu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filmTablosu.getTableHeader().setBackground(ALAN_RENGI);
        filmTablosu.getTableHeader().setForeground(YAZI_RENGI);
        filmTablosu.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        filmTablosu.getTableHeader().setReorderingAllowed(false);

        // Tabloyu kaydirma cubugu ile birlikte gosterir.
        JScrollPane kaydirmaAlani = new JScrollPane(filmTablosu);
        kaydirmaAlani.getViewport().setBackground(PANEL_RENGI);
        kaydirmaAlani.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(58, 63, 74)),
            javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));

        // Tabloyu pencerenin orta kismina yerlestirir.
        add(kaydirmaAlani, BorderLayout.CENTER);
    }

    // Giris kutularindan veri alip yeni film ekler.
    private void filmEkle() {
        // Tum alanlari ortak metotla kontrol edip guvenli sekilde veri alir.
        FilmGirisi giris = filmGirisiniDogrula();
        if (giris == null) {
            return;
        }
        
        // Ayni adla kayitli bir film varsa tekrar eklenmesini engeller.
        if (filmAdiZatenVarMi(giris.ad, -1)) {
            JOptionPane.showMessageDialog(this, "Bu film zaten listede var. Lutfen farkli bir film girin.");
            return;
        }

        // Yeni film nesnesini listeye ekler.
        filmler.add(new Film(giris.ad, giris.puan, giris.tarih, giris.oySayisi, giris.tur));

        // Liste degisince tabloyu gunceller.
        tabloyuYenile();

        // Liste degisince dosyaya otomatik kayit yapar.
        dosyayaKaydet(false);

        // Giris kutularini temizler.
        alanlariTemizle();
    }

    // Tabloda secilen filmi listeden siler.
    private void seciliFilmiSil() {
        // Tablodaki secili satirin index degerini alir.
        int seciliSatir = filmTablosu.getSelectedRow();

        // Hic satir secilmediyse uyari verir.
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lutfen silmek icin bir film secin.");
            return;
        }

        // Tablo satiri ile liste indexi ayni oldugu icin dogrudan siler.
        filmler.remove(seciliSatir);

        // Silme sonrasi tabloyu yeniler.
        tabloyuYenile();

        // Silme sonrasi dosyayi gunceller.
        dosyayaKaydet(false);
    }

    // Tabloda secilen filmi, giris kutularindaki degerlerle gunceller.
    private void seciliFilmiGuncelle() {
        // Guncellenecek satiri bulur.
        int seciliSatir = filmTablosu.getSelectedRow();

        // Secim yoksa kullaniciyi bilgilendirir.
        if (seciliSatir == -1) {
            JOptionPane.showMessageDialog(this, "Lutfen guncellemek icin bir film secin.");
            return;
        }

        // Tum alanlari ortak metotla kontrol edip guvenli sekilde veri alir.
        FilmGirisi giris = filmGirisiniDogrula();
        if (giris == null) {
            return;
        }
        
        // Baska bir kayitta ayni film adi varsa guncellemeyi engeller.
        if (filmAdiZatenVarMi(giris.ad, seciliSatir)) {
            JOptionPane.showMessageDialog(this, "Bu film adi baska bir kayitta zaten var.");
            return;
        }

        // Secilen film nesnesini getirir.
        Film film = filmler.get(seciliSatir);

        // Filmin alanlarini setter metotlariyla gunceller.
        film.setFilmAdi(giris.ad);
        film.setImdbPuani(giris.puan);
        film.setImdbOySayisi(giris.oySayisi);
        film.setTur(giris.tur);
        film.setIzlenmeTarihi(giris.tarih);

        // Guncel verileri tabloya yansitir.
        tabloyuYenile();

        // Guncel verileri dosyada saklar.
        dosyayaKaydet(false);

        // Giris kutularini temizler.
        alanlariTemizle();
    }

    // Filmleri IMDb puanina gore buyukten kucuge siralar.
    private void puanaGoreSirala() {
        // Listeyi Comparator ile azalan sirada duzenler.
        filmler.sort(Comparator.comparingDouble(Film::getImdbPuani).reversed());

        // Siralama sonucunu tabloda gosterir.
        tabloyuYenile();

        // Yeni siralamayi dosyada korur.
        dosyayaKaydet(false);
    }

    // Bellekteki film listesini tabloya yeniden yazar.
    private void tabloyuYenile() {
        // Eski tablo satirlarini tamamen temizler.
        tabloModeli.setRowCount(0);

        // Listedeki her filmi tabloya satir olarak ekler.
        for (int i = 0; i < filmler.size(); i++) {
            Film film = filmler.get(i);
            tabloModeli.addRow(new Object[]{
                i + 1,
                film.getFilmAdi(),
                film.getImdbPuani(),
                oySayisiniFormatla(film.getImdbOySayisi()),
                film.getTur(),
                film.getIzlenmeTarihi()
            });
        }

        // Veri yenilense bile arama kutusundaki metne gore aktif filtreyi korur.
        canliAramaFiltresiUygula();
    }

    // Metin kutularini bir sonraki islem icin bosaltir.
    private void alanlariTemizle() {
        adField.setText("");
        puanField.setText("");
        oyField.setText("");
        turField.setText("");
        tarihField.setText("");
    }

    // Bellekteki film listesini filmler.txt dosyasina kaydeder.
    private void dosyayaKaydet(boolean mesajGoster) {
        // Kaydetme sirasinda dosyayi acip guvenli sekilde kapatir.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOSYA_ADI))) {
            // Her film tek satira "ad|puan|oy|tur|tarih" formatinda yazilir.
            for (Film film : filmler) {
                writer.write(
                    film.getFilmAdi() + "|" +
                    film.getImdbPuani() + "|" +
                    film.getImdbOySayisi() + "|" +
                    film.getTur() + "|" +
                    film.getIzlenmeTarihi()
                );
                writer.newLine();
            }

            // Bu metot manuel butondan cagrildiysa kullaniciya bilgi verir.
            if (mesajGoster) {
                JOptionPane.showMessageDialog(this, "Veriler dosyaya kaydedildi.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Kaydetme hatasi: " + e.getMessage());
        }
    }

    // Uygulama acilirken filmler.txt dosyasindan verileri yukler.
    private void dosyadanFilmleriYukle() {
        // Dosyayi satir satir okumak icin reader nesnesi olusturur.
        try (BufferedReader reader = new BufferedReader(new FileReader(DOSYA_ADI))) {
            String satir;

            // Her satiri ayrac karakterine gore parcalayip Film nesnesine cevirir.
            while ((satir = reader.readLine()) != null) {
                String[] parcalar = satir.split("\\|");

                // Format beklenenden farkliysa satiri atlar.
                if (parcalar.length < 3) {
                    continue;
                }

                // Tek satir bozuk olsa bile uygulama cokmesin diye satir bazli kontrol yapar.
                try {
                    // Film alanlarini ayristirir.
                    String ad = parcalar[0];
                    double puan = Double.parseDouble(parcalar[1]);
                    long oySayisi = 0;
                    String tur = "Bilinmiyor";
                    String tarih;

                    // Eski ve yeni dosya formatlarini birlikte destekler.
                    if (parcalar.length >= 5) {
                        oySayisi = Long.parseLong(parcalar[2]);
                        tur = parcalar[3];
                        tarih = parcalar[4];
                    } else if (parcalar.length == 4) {
                        oySayisi = Long.parseLong(parcalar[2]);
                        tarih = parcalar[3];
                    } else {
                        tarih = parcalar[2];
                    }

                    // Olusan film nesnesini listeye ekler.
                    filmler.add(new Film(ad, puan, tarih, oySayisi, tur));
                } catch (NumberFormatException e) {
                    // Hatali satir varsa sadece o satiri atlar.
                }
            }
        } catch (IOException e) {
            // Dosya yoksa ilk acilis olabilir; sessizce devam eder.
        }
    }

    // Film adina gore listede arama yapip bulunan kaydi secer.
    private void filmAra() {
        // Arama metnini alir ve bosluklari temizler.
        String aranan = aramaField.getText().trim();

        // Bos arama yapilirsa kullaniciyi bilgilendirir.
        if (aranan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lutfen aramak icin bir film adi girin.");
            return;
        }

        // Film adinda gecen ilk eslesmeyi bulmaya calisir.
        for (int i = 0; i < filmler.size(); i++) {
            Film film = filmler.get(i);
            if (film.getFilmAdi().toLowerCase().contains(aranan.toLowerCase())) {
                // Bulunan satiri tabloda secili hale getirir.
                filmTablosu.setRowSelectionInterval(i, i);
                filmTablosu.scrollRectToVisible(filmTablosu.getCellRect(i, 0, true));

                // Bulunan filmi duzenleme kolayligi icin giris alanlarina yazar.
                adField.setText(film.getFilmAdi());
                puanField.setText(String.valueOf(film.getImdbPuani()));
                oyField.setText(oySayisiniFormatla(film.getImdbOySayisi()));
                turField.setText(film.getTur());
                tarihField.setText(film.getIzlenmeTarihi());
                return;
            }
        }

        // Eslesme yoksa bilgi mesaji gosterir.
        JOptionPane.showMessageDialog(this, "Aradiginiz film bulunamadi.");
    }

    // IMDb linki veya film adi ile otomatik olarak ad ve puan doldurur.
    private void imdbdenDoldur() {
        // Kullanici girdisini alir.
        String kaynak = imdbKaynakField.getText().trim();
        if (kaynak.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lutfen IMDb linki veya film adi girin.");
            return;
        }

        try {
            // Once daha stabil olan OMDb API uzerinden bilgi almaya calisir.
            ImdbFilmBilgisi bilgi = omdbdenBilgiGetir(kaynak);

            // Girilen metinden once IMDb kimligini (tt...) bulmaya calisir.
            if (bilgi == null) {
                String imdbId = imdbIdBul(kaynak);
                if (imdbId == null) {
                    JOptionPane.showMessageDialog(this, "IMDb kaydi bulunamadi.");
                    return;
                }

                // OMDb olmadan da calissin diye eski IMDb yontemine fallback yapar.
                bilgi = imdbDetayGetir(imdbId);
            }

            if (bilgi == null) {
                JOptionPane.showMessageDialog(this, "IMDb detayi alinamadi. Lutfen baska bir film veya link deneyin.");
                return;
            }

            // Alanlari otomatik doldurur.
            adField.setText(bilgi.ad);
            puanField.setText(String.valueOf(bilgi.puan));
            oyField.setText(oySayisiniFormatla(bilgi.oySayisi));
            turField.setText(bilgi.tur);

            // Tarih bos ise bugunun tarihini otomatik yazar.
            if (tarihField.getText().trim().isEmpty()) {
                tarihField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }

            JOptionPane.showMessageDialog(this, "IMDb verileri basariyla dolduruldu.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "IMDb verisi alinirken hata olustu: " + e.getMessage());
        }
    }

    // OMDb API ile film adini ve IMDb puanini ceker.
    private ImdbFilmBilgisi omdbdenBilgiGetir(String kaynak) {
        try {
            String apiUrl;

            // Giris icinde tt... varsa IMDb ID ile sorgu yapar.
            Matcher idMatcher = Pattern.compile("(tt\\d{7,9})", Pattern.CASE_INSENSITIVE).matcher(kaynak);
            if (idMatcher.find()) {
                String imdbId = idMatcher.group(1).toLowerCase();
                apiUrl = "https://www.omdbapi.com/?apikey=" + OMDB_API_KEY + "&i=" +
                    URLEncoder.encode(imdbId, StandardCharsets.UTF_8);
            } else {
                // Aksi durumda metni film adi kabul ederek sorgular.
                apiUrl = "https://www.omdbapi.com/?apikey=" + OMDB_API_KEY + "&t=" +
                    URLEncoder.encode(kaynak, StandardCharsets.UTF_8);
            }

            String json = httpGet(apiUrl);

            // API basarisiz donerse null dondurur.
            if (json.contains("\"Response\":\"False\"")) {
                return null;
            }

            // Film adini ve IMDb puanini JSON'dan ceker.
            String ad = ilkEslesenGrup(json, "\"Title\"\\s*:\\s*\"([^\"]+)\"");
            String puanMetni = ilkEslesenGrup(json, "\"imdbRating\"\\s*:\\s*\"([0-9]+(?:\\.[0-9]+)?)\"");
            String oyMetni = ilkEslesenGrup(json, "\"imdbVotes\"\\s*:\\s*\"([0-9,]+)\"");
            String tur = ilkEslesenGrup(json, "\"Genre\"\\s*:\\s*\"([^\"]+)\"");

            if (ad == null || puanMetni == null || "N/A".equalsIgnoreCase(puanMetni)) {
                return null;
            }

            double puan = Double.parseDouble(puanMetni);
            long oySayisi = oyMetni == null ? 0 : Long.parseLong(oyMetni.replace(",", ""));
            return new ImdbFilmBilgisi(ad, puan, oySayisi, tur == null ? "Bilinmiyor" : tur);
        } catch (Exception e) {
            // OMDb tarafinda hata olursa fallback icin null dondurur.
            return null;
        }
    }

    // Girilen metin link ise dogrudan, degilse arama ile IMDb ID bulur.
    private String imdbIdBul(String kaynak) throws IOException, InterruptedException {
        // Dogrudan IMDb linki/ID girildiyse tt... kalibini yakalar.
        Matcher idMatcher = Pattern.compile("(tt\\d{7,9})", Pattern.CASE_INSENSITIVE).matcher(kaynak);
        if (idMatcher.find()) {
            return idMatcher.group(1).toLowerCase();
        }

        // Metin film adiysa IMDb suggestion servisi ile ilk sonucu alir.
        String aranan = kaynak.toLowerCase();
        String ilkHarf = String.valueOf(aranan.charAt(0));
        String encoded = URLEncoder.encode(aranan, StandardCharsets.UTF_8);
        String url = "https://v2.sg.media-imdb.com/suggestion/" + ilkHarf + "/" + encoded + ".json";
        String json = httpGet(url);

        // Donen JSON icindeki ilk tt... kimligini bulur.
        Matcher suggestionMatcher = Pattern.compile("\"id\"\\s*:\\s*\"(tt\\d{7,9})\"").matcher(json);
        if (suggestionMatcher.find()) {
            return suggestionMatcher.group(1);
        }
        return null;
    }

    // IMDb title sayfasindan film adini ve puanini ceker.
    private ImdbFilmBilgisi imdbDetayGetir(String imdbId) throws IOException, InterruptedException {
        // IMDb bazen koruma/yonlendirme sayfasi dondurebildigi icin birden fazla URL ile deneriz.
        String[] urlAdaylari = {
            "https://www.imdb.com/title/" + imdbId + "/",
            "https://m.imdb.com/title/" + imdbId + "/"
        };

        for (String url : urlAdaylari) {
            String html;
            try {
                html = httpGet(url);
            } catch (IOException e) {
                // Bu URL basarisiz olursa sonraki adaya gecer.
                continue;
            }

            // Birden fazla desenden isim bilgisi yakalamaya calisir.
            String ad = ilkEslesenGrup(html, "\"name\"\\s*:\\s*\"([^\"]+)\"");
            if (ad == null) {
                ad = ilkEslesenGrup(html, "<title>\\s*([^<]+?)\\s*-\\s*IMDb\\s*</title>");
            }

            // Puan bilgisi farkli formatlarda gelebildigi icin alternatif desenler kullanir.
            String puanMetni = ilkEslesenGrup(html, "\"ratingValue\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]+)?)\"?");
            if (puanMetni == null) {
                puanMetni = ilkEslesenGrup(html, "hero-rating-bar__aggregate-rating__score[^0-9]*([0-9]+(?:\\.[0-9]+)?)");
            }
            if (puanMetni == null) {
                puanMetni = ilkEslesenGrup(html, "aggregateRating[^\\{\\}]*ratingValue[^0-9]*([0-9]+(?:\\.[0-9]+)?)");
            }
            
            // Oy sayisi icin olasi desenlerden ilk uygun olanini dener.
            String oyMetni = ilkEslesenGrup(html, "\"ratingCount\"\\s*:\\s*\"?([0-9,]+)\"?");
            if (oyMetni == null) {
                oyMetni = ilkEslesenGrup(html, "ratingCount[^0-9]*([0-9,]+)");
            }
            
            // Tur bilgisini sayfadaki JSON-LD iceriginden yakalamaya calisir.
            String tur = ilkEslesenGrup(html, "\"genre\"\\s*:\\s*\\[(.*?)\\]");
            if (tur != null) {
                tur = tur.replace("\"", "").trim();
            }

            // Hem ad hem puan bulunduysa sonucu dondurur.
            if (ad != null && puanMetni != null) {
                ad = htmlDecode(ad).trim();
                double puan = Double.parseDouble(puanMetni);
                long oySayisi = oyMetni == null ? 0 : Long.parseLong(oyMetni.replace(",", ""));
                return new ImdbFilmBilgisi(ad, puan, oySayisi, tur == null || tur.isEmpty() ? "Bilinmiyor" : tur);
            }
        }
        return null;
    }

    // HTTP GET istegi atip cevap govdesini metin olarak dondurur.
    private String httpGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("User-Agent", "Mozilla/5.0")
            .header("Accept-Language", "en-US,en;q=0.9")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("HTTP durum kodu: " + response.statusCode());
        }
        return response.body();
    }

    // Verilen regex ile ilk eslesen grubun degerini dondurur.
    private String ilkEslesenGrup(String metin, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(metin);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // HTML ozel karakterlerini duz metne cevirir.
    private String htmlDecode(String metin) {
        return metin
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">");
    }

    // Oy sayisini Turkce formatta nokta ile binlik ayirarak gosterir.
    private String oySayisiniFormatla(long oySayisi) {
        DecimalFormatSymbols semboller = new DecimalFormatSymbols(new Locale("tr", "TR"));
        semboller.setGroupingSeparator('.');
        DecimalFormat format = new DecimalFormat("#,###", semboller);
        format.setGroupingUsed(true);
        return format.format(oySayisi);
    }

    // Arama kutusundaki metne gore tabloyu canli olarak filtreler.
    private void canliAramaFiltresiUygula() {
        // Arama kutusuna yazilan metni alir.
        String aranan = aramaField.getText().trim();

        // Metin bos ise filtreyi kaldirip tum satirlari tekrar gosterir.
        if (aranan.isEmpty()) {
            tabloSorter.setRowFilter(null);
            return;
        }

        try {
            // Film adi kolonunda (1. kolon) kucuk-buyuk harf duyarsiz filtre uygular.
            tabloSorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + aranan, 1));
        } catch (PatternSyntaxException e) {
            // Ozel karakter kaynakli hata olursa uygulamayi bozmadan filtreyi kaldirir.
            tabloSorter.setRowFilter(null);
        }
    }

    // Ekleme ve guncellemede kullanilacak ortak dogrulama sonucunu tasir.
    private static class FilmGirisi {
        private final String ad;
        private final double puan;
        private final long oySayisi;
        private final String tur;
        private final String tarih;

        private FilmGirisi(String ad, double puan, long oySayisi, String tur, String tarih) {
            this.ad = ad;
            this.puan = puan;
            this.oySayisi = oySayisi;
            this.tur = tur;
            this.tarih = tarih;
        }
    }

    // IMDb'den gelen otomatik film bilgilerini tasir.
    private static class ImdbFilmBilgisi {
        private final String ad;
        private final double puan;
        private final long oySayisi;
        private final String tur;

        private ImdbFilmBilgisi(String ad, double puan, long oySayisi, String tur) {
            this.ad = ad;
            this.puan = puan;
            this.oySayisi = oySayisi;
            this.tur = tur;
        }
    }

    // Giris alanlarindaki verileri dogrular ve gecerliyse tek nesnede dondurur.
    private FilmGirisi filmGirisiniDogrula() {
        // Metin kutularindaki bosluklari temizleyip degerleri alir.
        String ad = adField.getText().trim();
        String puanMetni = puanField.getText().trim();
        String oyMetni = oyField.getText().trim();
        String tur = turField.getText().trim();
        String tarih = tarihField.getText().trim();

        // Zorunlu alanlar bos ise islemi durdurur.
        if (ad.isEmpty() || puanMetni.isEmpty() || oyMetni.isEmpty() || tur.isEmpty() || tarih.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lutfen tum alanlari doldurun.");
            return null;
        }

        // IMDb puani sayisal degilse kullaniciyi uyarir.
        double puan;
        try {
            puan = Double.parseDouble(puanMetni);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "IMDb puani sayisal olmali.");
            return null;
        }

        // IMDb puani mantikli bir aralikta degilse hatali giris kabul eder.
        if (puan < 0 || puan > 10) {
            JOptionPane.showMessageDialog(this, "IMDb puani 0 ile 10 arasinda olmali.");
            return null;
        }

        // IMDb oy sayisini sayisal degere cevirir.
        long oySayisi;
        try {
            // Kullanici 1.234.567 veya 1,234,567 gibi farkli ayiraclarla girebilir.
            oySayisi = Long.parseLong(
                oyMetni.replace(".", "").replace(",", "").replace(" ", "")
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "IMDb oy sayisi sayisal olmali.");
            return null;
        }

        // Oy sayisinin negatif olmasini engeller.
        if (oySayisi < 0) {
            JOptionPane.showMessageDialog(this, "IMDb oy sayisi negatif olamaz.");
            return null;
        }

        // Dogrulama basariliysa temizlenmis veriyi dondurur.
        return new FilmGirisi(ad, puan, oySayisi, tur, tarih);
    }

    // Ayni film adinin daha once kayitli olup olmadigini kontrol eder.
    private boolean filmAdiZatenVarMi(String ad, int haricTutulanIndex) {
        // Tum listeyi dolasip adi karsilastirir.
        for (int i = 0; i < filmler.size(); i++) {
            if (i == haricTutulanIndex) {
                continue;
            }

            Film film = filmler.get(i);
            if (film.getFilmAdi().equalsIgnoreCase(ad)) {
                return true;
            }
        }
        return false;
    }

    // Butonlar icin ortak renk/font/boyut ayarlar.
    private void butonuStille(JButton buton) {
        // Koyu tema ile uyumlu mavi vurgu ve acik yazi uygular.
        buton.setBackground(BUTON_RENGI);
        buton.setForeground(Color.WHITE);
        buton.setFocusPainted(false);
        buton.setBorderPainted(false);
        buton.setOpaque(true);
        buton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        buton.setPreferredSize(new Dimension(150, 34));
        buton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Kullanici fareyi butonun ustune getirdiginde daha acik renk gosterir.
        buton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                buton.setBackground(BUTON_HOVER_RENGI);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                buton.setBackground(BUTON_RENGI);
            }
        });
    }

    // Metin kutularini koyu tema ile uyumlu hale getirir.
    private void metinAlaniniStille(JTextField alan) {
        // Koyu zeminde okunakli olmasi icin arka plan/yazi/caret renklerini ayarlar.
        alan.setBackground(ALAN_RENGI);
        alan.setForeground(YAZI_RENGI);
        alan.setCaretColor(Color.WHITE);
        alan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        alan.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new Color(70, 76, 90)),
            javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        alan.setPreferredSize(new Dimension(alan.getPreferredSize().width, 34));
    }

    // Uygulamanin baslangic noktasi; GUI'yi Event Dispatch Thread'de baslatir.
    public static void main(String[] args) {
        // Mümkünse Nimbus Look&Feel ile daha modern temel Swing gorunumu uygular.
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Look&Feel ayarlanamazsa varsayilan gorunumle devam eder.
        }

        SwingUtilities.invokeLater(() -> {
            // GUI sinifindan nesne olusturup pencereyi gorunur yapar.
            CineTrackGUI gui = new CineTrackGUI();
            gui.setVisible(true);
        });
    }
}
