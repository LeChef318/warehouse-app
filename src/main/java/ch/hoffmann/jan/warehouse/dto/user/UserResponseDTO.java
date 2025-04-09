package ch.hoffmann.jan.warehouse.dto.user;

public class UserResponseDTO {
    private final Long id;
    private final String username;
    private final String role;
    private final String firstname;
    private final String lastname;


    public UserResponseDTO(Long id, String username, String role, String firstname, String lastname) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

}

