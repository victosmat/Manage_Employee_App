package com.example.demo.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Entity
@Table(name = "User")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Comparable<User> {
    static ArrayList<Long> ids = LongStream.rangeClosed(1000, 9999)
            .boxed() // chuyển số thành int object
            .collect(Collectors.toCollection(ArrayList::new));
    @Id
    @Column(name = "user_id")
    private Long ID;
    private String fullName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> address;

    private String email;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Override
    public int compareTo(User user) {
        return this.fullName.compareTo(user.getFullName());
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    //    khi find person thì sẽ không find address liên quan
//    @ManyToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    //    khi find person thì sẽ find tất cả address liên quan
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "User_Role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "time_id", referencedColumnName = "id")
    private Time time;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JobDetails> jobDetails;


    public User(String fullName, List<Address> address, String email, Account account, AuthProvider authProvider, Set<Role> roles, Time time) {
        Random random = new Random();
        int randomIndex = random.nextInt(ids.size());
        ids.remove(randomIndex);
        this.ID = ids.get(randomIndex);
        this.fullName = fullName;
        this.address = address;
        this.email = email;
        this.account = account;
        this.authProvider = authProvider;
        this.roles = roles;
        this.time = time;
    }
}
