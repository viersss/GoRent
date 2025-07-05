// =================================================================================
// File 7: src/Transaksi.java (Model)
// =================================================================================
import java.time.LocalDate;

class Transaksi {
    private final String id, idKendaraan, merkKendaraan, idPelanggan, namaPelanggan;
    private String status;
    private final LocalDate tglPinjam;
    private double denda;

    public Transaksi(String id, Kendaraan kendaraan, Pelanggan pelanggan, LocalDate tglPinjam) {
        this.id = id;
        this.idKendaraan = kendaraan.getId();
        this.merkKendaraan = kendaraan.getMerk() + " (" + kendaraan.getTipe() + ")";
        this.idPelanggan = pelanggan.getId();
        this.namaPelanggan = pelanggan.getNama();
        this.status = "Disewa"; this.tglPinjam = tglPinjam; this.denda = 0.0;
    }
    public String getId() { return id; }
    public String getIdKendaraan() { return idKendaraan; }
    public String getMerkKendaraan() { return merkKendaraan; }
    public String getIdPelanggan() { return idPelanggan; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public String getStatus() { return status; }
    public LocalDate getTglPinjam() { return tglPinjam; }
    public double getDenda() { return denda; }
    public void setStatus(String status) { this.status = status; }
    public void setDenda(double denda) { this.denda = denda; }
}