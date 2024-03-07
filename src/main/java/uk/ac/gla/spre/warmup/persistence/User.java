package uk.ac.gla.spre.warmup.persistence;

import jakarta.persistence.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

@Entity
@Table(name="Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private String password;

    private String betterPassword;

    @Transient
    private PasswordEncoder encoder = SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8();

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
        setBetterPassword(password);
    }

    public String getName() {
        return name;
    }

    public void setName(java.lang.String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    private String encodePassword(String password) {
        return encoder.encode(password);
    }

    public void setBetterPassword(String password) {
        this.betterPassword = encodePassword(password);
    }

    public Boolean checkBetterPassword(String password) {
        return this.encoder.matches(betterPassword, this.betterPassword);
    }
}
