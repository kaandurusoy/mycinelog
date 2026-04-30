import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Uygulamanın ana sınıfı. Program buradan başlar.
public class Main {
    // Film verilerinin saklanacağı dosyanın adını sabit olarak tutuyoruz.
    private static final String DOSYA_ADI = "filmler.txt";

    // Filmleri tutmak için ArrayList kullanıyoruz.
    private static final ArrayList<Film> filmler = new ArrayList<>();

    // Kullanıcıdan veri almak için Scanner nesnesi oluşturuyoruz.
    private static final Scanner scanner = new Scanner(System.in);

    // Java uygulaması çalıştığında ilk çağrılan metot burasıdır.
    public static void main(String[] args) {
        // Program açılırken varsa dosyadaki filmleri belleğe yüklüyoruz.
        dosyadanFilmleriYukle();

        // Sonsuz döngü ile menüyü tekrar tekrar gösteriyoruz.
        while (true) {
            menuyuGoster();
            int secim = secimAl();

            // Kullanıcının seçimine göre ilgili işlemi yapıyoruz.
            switch (secim) {
                case 1:
                    filmEkle();
                    break;
                case 2:
                    filmleriListele();
                    break;
                case 3:
                    filmleriPuanaGoreSirala();
                    break;
                case 4:
                    filmSil();
                    break;
                case 5:
                    filmGuncelle();
                    break;
                case 6:
                    dosyayaKaydet();
                    break;
                case 7:
                    // Programdan çıkmadan önce kullanıcıya bilgi veriyoruz.
                    System.out.println("Programdan cikiliyor. Iyi gunler!");
                    scanner.close();
                    return;
                default:
                    // Geçersiz seçim yapılırsa kullanıcıyı uyarıyoruz.
                    System.out.println("Gecersiz secim yaptiniz. Lutfen tekrar deneyin.");
            }
        }
    }

    // Ana menüde kullanıcıya sunulacak seçenekleri ekrana yazar.
    private static void menuyuGoster() {
        System.out.println("\n=== MyCineLog Ana Menu ===");
        System.out.println("1) Film Ekle");
        System.out.println("2) Filmleri Listele");
        System.out.println("3) IMDb Puanina Gore Sirala (Yuksekten Dusuge)");
        System.out.println("4) Film Sil");
        System.out.println("5) Film Guncelle");
        System.out.println("6) Verileri Dosyaya Kaydet");
        System.out.println("7) Cikis");
        System.out.print("Seciminizi girin: ");
    }

    // Kullanıcıdan menü seçimi alır ve sayıya çevirir.
    private static int secimAl() {
        // Girilen satırı alıp sayıya çevirmeye çalışıyoruz.
        String giris = scanner.nextLine();
        try {
            return Integer.parseInt(giris);
        } catch (NumberFormatException e) {
            // Sayı değilse -1 döndürüp geçersiz seçim olarak işliyoruz.
            return -1;
        }
    }

    // Kullanıcıdan film bilgilerini alıp listeye yeni film ekler.
    private static void filmEkle() {
        System.out.print("Film adini girin: ");
        String filmAdi = scanner.nextLine();

        System.out.print("IMDb puanini girin: ");
        double imdbPuani;

        // IMDb puanı sayısal girilene kadar kullanıcıdan tekrar ister.
        while (true) {
            String puanGirdisi = scanner.nextLine();
            try {
                imdbPuani = Double.parseDouble(puanGirdisi);
                break;
            } catch (NumberFormatException e) {
                System.out.print("Hatali giris! Lutfen gecerli bir IMDb puani girin: ");
            }
        }

        System.out.print("Izlenme tarihini girin (ornek: 24.04.2026): ");
        String izlenmeTarihi = scanner.nextLine();

        // Girilen bilgilerle bir Film nesnesi oluşturuyoruz.
        Film yeniFilm = new Film(filmAdi, imdbPuani, izlenmeTarihi);

        // Oluşturulan filmi ArrayList'e ekliyoruz.
        filmler.add(yeniFilm);

        // Yeni film eklendiği anda dosyayı güncelliyoruz.
        dosyayaKaydet();

        System.out.println("Film basariyla eklendi.");
    }

    // Filmleri ekrana listeler. Her filme otomatik ID atar (liste sırasına göre).
    private static void filmleriListele() {
        // Eğer listede film yoksa kullanıcıya bilgi veriyoruz.
        if (filmler.isEmpty()) {
            System.out.println("Listede henuz film bulunmuyor.");
            return;
        }

        System.out.println("\n=== Film Listesi ===");

        // Her film için index + 1 değerini ID olarak kullanıyoruz.
        for (int i = 0; i < filmler.size(); i++) {
            Film film = filmler.get(i);
            int id = i + 1;

            System.out.println(
                "ID: " + id +
                " | Film Adi: " + film.getFilmAdi() +
                " | IMDb: " + film.getImdbPuani() +
                " | Izlenme Tarihi: " + film.getIzlenmeTarihi()
            );
        }
    }

    // Filmleri IMDb puanına göre yüksekten düşüğe sıralar ve sonucu gösterir.
    private static void filmleriPuanaGoreSirala() {
        // Sıralanacak film yoksa işlemi sonlandırıyoruz.
        if (filmler.isEmpty()) {
            System.out.println("Siralama icin listede film bulunmuyor.");
            return;
        }

        // Comparator ile IMDb puanına göre azalan (büyükten küçüğe) sıralama yapıyoruz.
        filmler.sort(Comparator.comparingDouble(Film::getImdbPuani).reversed());

        // Sıralama sonrası yeni sıralamayı dosyaya yansıtıyoruz.
        dosyayaKaydet();

        System.out.println("Filmler IMDb puanina gore yuksekten dusuge siralandi.");

        // Sıralama sonrası güncel listeyi ekrana yazdırıyoruz.
        filmleriListele();
    }

    // Kullanıcının girdiği ID'ye göre film siler.
    private static void filmSil() {
        // Silme işlemi için listede film olması gerekir.
        if (filmler.isEmpty()) {
            System.out.println("Silinecek film bulunmuyor.");
            return;
        }

        // Kullanıcı doğru ID'yi görebilsin diye önce mevcut listeyi gösteriyoruz.
        filmleriListele();
        System.out.print("Silmek istediginiz filmin ID degerini girin: ");

        int id = secimAl();

        // ID kontrolü: 1 ile listenin boyutu arasında olmalıdır.
        if (id < 1 || id > filmler.size()) {
            System.out.println("Gecersiz ID girdiniz.");
            return;
        }

        // Liste index'i 0'dan başladığı için ID'den 1 çıkarıyoruz.
        Film silinenFilm = filmler.remove(id - 1);

        // Silme sonrası verileri kalıcı dosyaya kaydediyoruz.
        dosyayaKaydet();

        System.out.println("Film silindi: " + silinenFilm.getFilmAdi());
    }

    // Kullanıcının seçtiği filmin bilgilerini günceller.
    private static void filmGuncelle() {
        // Güncelleme için en az bir film olmalıdır.
        if (filmler.isEmpty()) {
            System.out.println("Guncellenecek film bulunmuyor.");
            return;
        }

        // ID seçimini kolaylaştırmak için önce listeyi gösteriyoruz.
        filmleriListele();
        System.out.print("Guncellemek istediginiz filmin ID degerini girin: ");

        int id = secimAl();

        // ID doğrulaması yapıyoruz.
        if (id < 1 || id > filmler.size()) {
            System.out.println("Gecersiz ID girdiniz.");
            return;
        }

        // Güncellenecek filme erişiyoruz.
        Film film = filmler.get(id - 1);

        // Kullanıcıdan yeni film adını alıyoruz.
        System.out.print("Yeni film adi (mevcut: " + film.getFilmAdi() + "): ");
        String yeniAd = scanner.nextLine();

        // Kullanıcıdan yeni IMDb puanını alıyoruz.
        System.out.print("Yeni IMDb puani (mevcut: " + film.getImdbPuani() + "): ");
        double yeniPuan = imdbPuaniAl();

        // Kullanıcıdan yeni izlenme tarihini alıyoruz.
        System.out.print("Yeni izlenme tarihi (mevcut: " + film.getIzlenmeTarihi() + "): ");
        String yeniTarih = scanner.nextLine();

        // Setter metotlarıyla film nesnesini güncelliyoruz.
        film.setFilmAdi(yeniAd);
        film.setImdbPuani(yeniPuan);
        film.setIzlenmeTarihi(yeniTarih);

        // Güncelleme sonrası değişiklikleri dosyaya kaydediyoruz.
        dosyayaKaydet();

        System.out.println("Film bilgileri basariyla guncellendi.");
    }

    // Kullanıcıdan güvenli şekilde IMDb puanı alır.
    private static double imdbPuaniAl() {
        // Hatalı giriş olursa tekrar istemek için döngü kullanıyoruz.
        while (true) {
            String puanGirdisi = scanner.nextLine();
            try {
                return Double.parseDouble(puanGirdisi);
            } catch (NumberFormatException e) {
                System.out.print("Hatali giris! Lutfen gecerli bir IMDb puani girin: ");
            }
        }
    }

    // Film listesini dosyaya yazarak kalıcı hale getirir.
    private static void dosyayaKaydet() {
        // try-with-resources kullanarak dosyayı güvenli şekilde açıp kapatıyoruz.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DOSYA_ADI))) {
            // Her filmi tek satırda, ayraç karakteri ile kaydediyoruz.
            for (Film film : filmler) {
                writer.write(film.getFilmAdi() + "|" + film.getImdbPuani() + "|" + film.getIzlenmeTarihi());
                writer.newLine();
            }
            System.out.println("Veriler dosyaya kaydedildi.");
        } catch (IOException e) {
            System.out.println("Dosyaya kaydetme sirasinda hata olustu: " + e.getMessage());
        }
    }

    // Program başlarken dosyadaki kayıtlı filmleri belleğe yükler.
    private static void dosyadanFilmleriYukle() {
        // Dosya yoksa bu durum ilk çalıştırma olabilir; hata vermeden devam ediyoruz.
        try (BufferedReader reader = new BufferedReader(new FileReader(DOSYA_ADI))) {
            String satir;

            // Dosyadaki her satırı tek tek okuyup Film nesnesine çeviriyoruz.
            while ((satir = reader.readLine()) != null) {
                String[] parcalar = satir.split("\\|");

                // Beklenen formatta değilse satırı atlıyoruz.
                if (parcalar.length != 3) {
                    continue;
                }

                String filmAdi = parcalar[0];
                double imdbPuani = Double.parseDouble(parcalar[1]);
                String izlenmeTarihi = parcalar[2];

                filmler.add(new Film(filmAdi, imdbPuani, izlenmeTarihi));
            }

            if (!filmler.isEmpty()) {
                System.out.println("Kayitli film verileri dosyadan yuklendi.");
            }
        } catch (IOException e) {
            // Dosya okunamıyorsa, yeni kullanıcı için boş listeyle devam ediyoruz.
            System.out.println("Kayitli veri dosyasi bulunamadi, bos liste ile baslandi.");
        } catch (NumberFormatException e) {
            // IMDb puanı formatı bozuk satırlar varsa kullanıcıyı bilgilendiriyoruz.
            System.out.println("Dosyadaki bazi kayitlar okunamadi (puan formati hatali).");
        }
    }
}
