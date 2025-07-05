// =================================================================================
// File 8: src/LoginHistory.java (Model)
// =================================================================================
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class LoginHistory {
    private final LocalDateTime loginTime;
    private final String status;
    public LoginHistory(LocalDateTime loginTime, String status) {
        this.loginTime = loginTime; this.status = status;
    }
    public String getStatus() { return status; }
    public String getFormattedLoginTime() {
        return loginTime.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss"));
    }
}