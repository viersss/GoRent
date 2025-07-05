// =================================================================================
// File 4: src/User.java (Model)
// =================================================================================
class User {
    private final String username;
    private final String password;
    private final Role role;
    private String avatarUrl;

    public User(String username, String password, Role role, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}