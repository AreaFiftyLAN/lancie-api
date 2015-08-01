package a5l;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;


@Entity
public class User implements Serializable{

    @OneToOne(targetEntity = Profile.class, cascade= CascadeType.ALL)
    private Profile profile;

    @Id
    @GeneratedValue
    private Long id;

    public Profile getProfile() {
        return profile;
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String password;
    public String username;
    public String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.profile = new Profile();
    }

    User() { // jpa only
    }
}
