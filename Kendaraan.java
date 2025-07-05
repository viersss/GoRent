// =================================================================================
// File 5: src/Kendaraan.java (Model)
// =================================================================================
class Kendaraan {
    // Field final untuk data yang tidak berubah
    private final String id, merk, tipe, transmisi, bahanBakar, imageUrl;
    private final int tahun, hargaSewa;
    // Status bisa berubah (Tersedia/Disewa)
    private String status;

    public Kendaraan(String id, String merk, String tipe, int tahun, String transmisi, String bahanBakar, int hargaSewa, String imageUrl) {
        this.id = id; this.merk = merk; this.tipe = tipe; this.tahun = tahun;
        this.transmisi = transmisi; this.bahanBakar = bahanBakar; this.hargaSewa = hargaSewa;
        this.imageUrl = imageUrl; this.status = "Tersedia"; // Default status
    }

    // Getter methods untuk mengakses data kendaraan
    public String getId() { return id; }
    public String getMerk() { return merk; }
    public String getTipe() { return tipe; }
    public int getTahun() { return tahun; }
    public String getTransmisi() { return transmisi; }
    public String getBahanBakar() { return bahanBakar; }
    public int getHargaSewa() { return hargaSewa; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
    // Setter untuk mengubah status sewa
    public void setStatus(String status) { this.status = status; }
}