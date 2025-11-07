import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BioskopQueueSystem {
    // === DATA BIOSKOP ===
    private static ArrayList<String> movies = new ArrayList<>();
    private static String[][] schedules = {
        {"10:00", "13:30", "16:00", "19:00"},
        {"11:00", "14:30", "17:00", "20:00"}, 
        {"10:30", "13:00", "15:30", "18:30"},
        {"12:00", "15:00", "18:00", "21:00"},
        {"11:20", "12:55", "15:00", "20:00"},
        {"08:10", "10:30", "12:30", "15:00"},
    };
    private static boolean[][] seats = new boolean[5][8];
    
    // === SISTEM ANTRIAN ===
    private static Queue<Customer> regularQueue = new LinkedList<>();
    private static Queue<Customer> priorityQueue = new LinkedList<>();
    private static int queueNumber = 1;
    
    // === RIWAYAT ===
    private static Stack<String> recentTickets = new Stack<>();
    private static ArrayList<String> transactionHistory = new ArrayList<>();
    
    // === HARGA ===
    private static final int REGULAR_PRICE = 35000;
    private static final int DISCOUNT_PRICE = 25000;
    private static Scanner scanner = new Scanner(System.in);
    
    // CLASS CUSTOMER
    static class Customer {
        String name;
        int queueNumber;
        String queueType;
        
        Customer(String name, int queueNumber, String queueType) {
            this.name = name;
            this.queueNumber = queueNumber;
            this.queueType = queueType;
        }
    }
    
    public static void main(String[] args) {
        initializeData();
        showMainMenu();
    }
    
    private static void initializeData() {
        // Inisialisasi film
        movies.add("JUAN: KING OF FISHING");
        movies.add("SPIDER-MAN: JUAN HOME"); 
        movies.add("RASYA MENUJU SUKSES");
        movies.add("ADA APA DENGAN JUAN");
        movies.add("JUAN MENGEJAR MASA DEPAN");
        movies.add("MIMPI MENJADI DPR");
        // Semua kursi tersedia
        for (int i = 0; i < seats.length; i++) {
            Arrays.fill(seats[i], true);
        }
        
        // Tidak ada transaksi awal - RIWAYAT KOSONG!
    }
    
    private static void showMainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("       SISTEM ANTRIAN TIKET BIOSKOP");
            System.out.println("=".repeat(50));
            System.out.println("1. Ambil Nomor Antrian");
            System.out.println("2. Panggil Antrian Berikutnya (Kasir)");
            System.out.println("3. Lihat Denah Kursi");
            System.out.println("4. Lihat Antrian Saat Ini");
            System.out.println("5. Lihat Tiket Terakhir");
            System.out.println("6. Lihat Riwayat Transaksi");
            System.out.println("7. Keluar");
            System.out.print("Pilih menu: ");
            
            int choice = getIntegerInput();
            
            switch (choice) {
                case 1: takeQueueNumber(); break;
                case 2: callNextQueue(); break;
                case 3: showSeatMap(); break;
                case 4: showCurrentQueue(); break;
                case 5: showRecentTickets(); break;
                case 6: showTransactionHistory(); break;
                case 7: running = false; break;
                default: System.out.println("Pilihan tidak valid!");
            }
        }
        System.out.println("Terima kasih! Program selesai.");
    }
    
    // === FITUR 1: AMBIL ANTRIAN ===
    private static void takeQueueNumber() {
        System.out.println("\n=== AMBIL NOMOR ANTRIAN ===");
        scanner.nextLine(); // clear buffer
        
        System.out.print("Masukkan nama Anda: ");
        String name = scanner.nextLine();
        
        System.out.println("\nPilih tipe antrian:");
        System.out.println("1. Antrian Reguler");
        System.out.println("2. Antrian Prioritas (Lansia/Disabilitas/Ibu Hamil)");
        System.out.print("Pilihan: ");
        
        int typeChoice = getIntegerInput();
        String queueType = "";
        
        if (typeChoice == 1) {
            queueType = "REGULER";
            regularQueue.add(new Customer(name, queueNumber, queueType));
            System.out.println("✅ Nomor antrian REGULER: " + queueNumber);
        } else if (typeChoice == 2) {
            queueType = "PRIORITAS"; 
            priorityQueue.add(new Customer(name, queueNumber, queueType));
            System.out.println("✅ Nomor antrian PRIORITAS: " + queueNumber);
        } else {
            System.out.println("❌ Pilihan tidak valid!");
            return;
        }
        
        System.out.println("👋 Selamat datang, " + name + "! Silakan menunggu dipanggil.");
        queueNumber++;
    }
    
    // === FITUR 2: PANGGIL ANTRIAN ===
    private static void callNextQueue() {
        System.out.println("\n=== PANGGIL ANTRIAN BERIKUTNYA ===");
        
        Customer nextCustomer = null;
        
        // Prioritaskan antrian prioritas
        if (!priorityQueue.isEmpty()) {
            nextCustomer = priorityQueue.poll();
            System.out.println("📢 Memanggil: " + nextCustomer.queueNumber + " (PRIORITAS)");
        } else if (!regularQueue.isEmpty()) {
            nextCustomer = regularQueue.poll(); 
            System.out.println("📢 Memanggil: " + nextCustomer.queueNumber + " (REGULER)");
        } else {
            System.out.println("❌ Tidak ada antrian!");
            return;
        }
        
        System.out.println("Atas nama: " + nextCustomer.name);
        processTicketPurchase(nextCustomer);
    }
    
    // === PROSES PEMBELIAN TIKET ===
    private static void processTicketPurchase(Customer customer) {
        System.out.println("\n--- PROSES PEMBELIAN TIKET ---");
        
        // 1. PILIH FILM
        String selectedMovie = selectMovie();
        if (selectedMovie == null) return;
        
        // 2. PILIH JADWAL  
        String selectedSchedule = selectSchedule(selectedMovie);
        if (selectedSchedule == null) return;
        
        // 3. PILIH KURSI
        String selectedSeat = selectSeat();
        if (selectedSeat == null) return;
        
        // 4. PROSES PEMBAYARAN
        processPayment(customer, selectedMovie, selectedSchedule, selectedSeat);
    }
    
    private static String selectMovie() {
        System.out.println("\n🎬 DAFTAR FILM TERSEDIA:");
        for (int i = 0; i < movies.size(); i++) {
            System.out.println((i + 1) + ". " + movies.get(i));
        }
        System.out.print("Pilih film (1-" + movies.size() + "): ");
        
        int choice = getIntegerInput();
        if (choice < 1 || choice > movies.size()) {
            System.out.println("❌ Pilihan film tidak valid!");
            return null;
        }
        
        String selected = movies.get(choice - 1);
        System.out.println("✅ Film dipilih: " + selected);
        return selected;
    }
    
    private static String selectSchedule(String movie) {
        int movieIndex = movies.indexOf(movie);
        String[] availableSchedules = schedules[movieIndex];
        
        System.out.println("\n🕐 JADWAL TAYANG '" + movie + "':");
        for (int i = 0; i < availableSchedules.length; i++) {
            System.out.println((i + 1) + ". " + availableSchedules[i]);
        }
        System.out.print("Pilih jadwal (1-" + availableSchedules.length + "): ");
        
        int choice = getIntegerInput();
        if (choice < 1 || choice > availableSchedules.length) {
            System.out.println("❌ Pilihan jadwal tidak valid!");
            return null;
        }
        
        String selected = availableSchedules[choice - 1];
        System.out.println("✅ Jadwal dipilih: " + selected);
        return selected;
    }
    
    private static String selectSeat() {
        boolean seatSelected = false;
        String seatCode = "";
        
        while (!seatSelected) {
            showSeatMap();
            System.out.print("\nPilih baris (A-E): ");
            String rowInput = scanner.next().toUpperCase();
            System.out.print("Pilih nomor kursi (1-8): ");
            int seatNum = getIntegerInput();
            
            if (rowInput.length() != 1 || rowInput.charAt(0) < 'A' || rowInput.charAt(0) > 'E') {
                System.out.println("❌ Baris harus A sampai E!");
                continue;
            }
            
            if (seatNum < 1 || seatNum > 8) {
                System.out.println("❌ Nomor kursi harus 1 sampai 8!");
                continue;
            }
            
            int row = rowInput.charAt(0) - 'A';
            int seat = seatNum - 1;
            
            if (!seats[row][seat]) {
                System.out.println("❌ Kursi " + rowInput + seatNum + " sudah terisi!");
                continue;
            }
            
            seatCode = rowInput + String.valueOf(seatNum);
            System.out.print("Konfirmasi pilih kursi " + seatCode + "? (Y/N): ");
            String confirm = scanner.next();
            
            if (confirm.equalsIgnoreCase("Y")) {
                seats[row][seat] = false;
                seatSelected = true;
                System.out.println("✅ Kursi " + seatCode + " berhasil dipilih!");
            }
        }
        
        return seatCode;
    }
    
    private static void processPayment(Customer customer, String movie, String schedule, String seat) {
        int price = customer.queueType.equals("PRIORITAS") ? DISCOUNT_PRICE : REGULAR_PRICE;
        
        System.out.println("\n--- RINCIAN PEMBAYARAN ---");
        System.out.println("Film    : " + movie);
        System.out.println("Jadwal  : " + schedule);
        System.out.println("Kursi   : " + seat);
        System.out.println("Tipe    : " + customer.queueType);
        System.out.println("Harga   : Rp " + price + 
                          (customer.queueType.equals("PRIORITAS") ? " (Diskon)" : ""));
        
        System.out.print("\nLanjutkan pembayaran? (Y/N): ");
        String confirm = scanner.next();
        
        if (confirm.equalsIgnoreCase("Y")) {
            // GENERATE TIKET
            String ticket = generateTicket(customer, movie, schedule, seat, price);
            recentTickets.push(ticket);
            
            // TAMBAH KE RIWAYAT
            String transaction = String.format("%03d - %s - %s - %s - Rp %d%s",
                customer.queueNumber, customer.name, movie, seat, price,
                customer.queueType.equals("PRIORITAS") ? " (Diskon)" : "");
            transactionHistory.add(transaction);
            
            System.out.println("\n🎉 PEMBAYARAN BERHASIL!");
            System.out.println(ticket);
        } else {
            System.out.println("❌ Pembayaran dibatalkan.");
            // Kembalikan kursi yang sudah dipilih
            int row = seat.charAt(0) - 'A';
            int seatNum = Integer.parseInt(seat.substring(1)) - 1;
            seats[row][seatNum] = true;
        }
    }
    
    private static String generateTicket(Customer customer, String movie, String schedule, String seat, int price) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String timestamp = LocalDateTime.now().format(dtf);
        
        return "\n" + "=".repeat(50) + "\n" +
               "               TIKET BIOSKOP\n" +
               "=".repeat(50) + "\n" +
               "No. Antrian : " + customer.queueNumber + " (" + customer.queueType + ")\n" +
               "Nama        : " + customer.name + "\n" +
               "Film        : " + movie + "\n" +
               "Jadwal      : " + schedule + "\n" +
               "Kursi       : " + seat + "\n" +
               "Harga       : Rp " + price + "\n" +
               "Waktu       : " + timestamp + "\n" +
               "=".repeat(50);
    }
    
    // === FITUR 3: DENAH KURSI ===
    private static void showSeatMap() {
        System.out.println("\n=== DENAH KURSI BİOSKOP ===");
        System.out.println("      🎬 LAYAR 🎬");
        System.out.println("[ ] = Tersedia   [X] = Terisi");
        System.out.print("   ");
        for (int i = 1; i <= 8; i++) System.out.print(" " + i + " ");
        System.out.println();
        
        for (int i = 0; i < seats.length; i++) {
            System.out.print((char)('A' + i) + "  ");
            for (int j = 0; j < seats[i].length; j++) {
                System.out.print(seats[i][j] ? "[ ]" : "[X]");
            }
            System.out.println("  " + (char)('A' + i));
        }
        
        System.out.print("   ");
        for (int i = 1; i <= 8; i++) System.out.print(" " + i + " ");
        System.out.println();
    }
    
    // === FITUR 4: LIHAT ANTRIAN ===
    private static void showCurrentQueue() {
        System.out.println("\n=== ANTRIAN SAAT INI ===");
        
        System.out.print("Antrian Prioritas: ");
        if (priorityQueue.isEmpty()) System.out.println("Kosong");
        else {
            for (Customer c : priorityQueue) 
                System.out.print(c.queueNumber + "(" + c.name + ") ");
            System.out.println();
        }
        
        System.out.print("Antrian Reguler  : ");
        if (regularQueue.isEmpty()) System.out.println("Kosong");
        else {
            for (Customer c : regularQueue)
                System.out.print(c.queueNumber + "(" + c.name + ") ");
            System.out.println();
        }
    }
    
    // === FITUR 5: TIKET TERAKHIR ===
    private static void showRecentTickets() {
        System.out.println("\n=== TIKET TERAKHIR ===");
        if (recentTickets.isEmpty()) {
            System.out.println("Belum ada tiket yang dicetak!");
        } else {
            System.out.println(recentTickets.peek());
        }
    }
    
    // === FITUR 6: RIWAYAT TRANSAKSI ===
    private static void showTransactionHistory() {
        System.out.println("\n=== RIWAYAT TRANSAKSI ===");
        if (transactionHistory.isEmpty()) {
            System.out.println("Belum ada transaksi!");
        } else {
            for (String transaction : transactionHistory) {
                System.out.println(transaction);
            }
        }
    }
    
    // === UTILITY METHOD ===
    private static int getIntegerInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Input harus angka! Coba lagi: ");
            scanner.next();
        }
        int input = scanner.nextInt();
        scanner.nextLine(); // clear buffer
        return input;
    }
}