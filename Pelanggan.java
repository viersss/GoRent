// =================================================================================
// File 6: src/Pelanggan.java (Model)
// =================================================================================
class Pelanggan {
    private final String id, nama, noTelepon;
    public Pelanggan(String id, String nama, String noTelepon) {
        this.id = id; this.nama = nama; this.noTelepon = noTelepon;
    }
    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getNoTelepon() { return noTelepon; }
}