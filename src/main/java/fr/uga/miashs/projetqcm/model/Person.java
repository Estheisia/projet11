package fr.uga.miashs.projetqcm.model;

import fr.uga.miashs.projetqcm.util.MyLocalDateXmlAdapter;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;


@Entity
@NamedQueries({
        @NamedQuery(name = "Person.login",
                query = "SELECT u FROM Person u WHERE u.email=:email")
})
//@Cacheable
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Person {

    @Id
    @GeneratedValue
    private long id;

    @NotBlank(message = "a nickname must be given")
    private String nickname;

    //@Basic(fetch=FetchType.LAZY)
    @Email(message = "mail should be valid")
    // exemple avec le fichier ValidationMessages.properties (voir dossier resources du projet)
    @NotNull(message = "{person.email.notnull}")

    @NotNull
    @Column(unique = true)
    private String email;

    //  @JsonbDateFormat("dd.MM.yyyy")
    @NotNull(message = "a birthdate must be given")
    @Past(message = "You are not born, ${validatedValue} is in the past")
    private LocalDate birthdate;

    @NotBlank
    private String passwordHash;

    @Transient
    private String password;

    public enum Role{
        teacher,
        user
    }

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "listeInscrits")
    Set<Exam> listeExams;

    // constructeur par défaut nécessaire pour l'utiliser comme un bean
    public Person() {
    }

    //@JsonbCreator
    public Person(@JsonbProperty("nickname") String nickname, @JsonbProperty("email") String email, @JsonbProperty("birthdate") LocalDate birthdate, @JsonbProperty("role") Role role) {
        this.nickname = nickname;
        this.email = email;
        this.birthdate = birthdate;
        this.role = role;
    }

    @JsonbProperty("_id")
    // Permet d'avoir l'id en tant qu'attribut au lieu d'element xml
    @XmlAttribute
    public long getId() {
        return id;
    }

    @JsonbTransient
    public void setId(long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // il faudrait mettre la propriété en transient si on ne veux pas renvoyer le password hash
    //@JsonbTransient
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @XmlJavaTypeAdapter(MyLocalDateXmlAdapter.class)
    @XmlElement
    public LocalDate getBirthdate() {
        return birthdate;
    }


    //@XmlJavaTypeAdapter(MyLocalDateXmlAdapter.class) // ca ne fonctionne pas ....
    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person personne = (Person) o;
        return Objects.equals(email, personne.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "Person{" +
                "nickname='" + nickname + '\'' +
                ", email='" + email + '\'' +
                ", birthdate=" + birthdate +
                '}';
    }
}
