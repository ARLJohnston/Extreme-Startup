package uk.ac.gla.spre.warmup.persistence;

import jakarta.persistence.*;
import org.springframework.data.repository.CrudRepository;

@Entity
@Table(name="Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private String password;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
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
}
