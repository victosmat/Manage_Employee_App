package com.example.demo;

import com.example.demo.entity.Address;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(true)
public class DemoApplicationTests {
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private UserRepository userRepository;

    //PERSIST
    @Test
    public void testCascadePersist() {
        User user = new User();
        Address address = new Address();
        address.setUser(user);
        user.setAddress(List.of(address));

        testEntityManager.persist(user);
        testEntityManager.flush();
        testEntityManager.clear();
    }

    //MERGE
    @Test
    public void whenParentSavedThenMerged() {
        User user = testEntityManager.find(User.class, 1);
        Role role = (Role) user.getRoles();
        user.setFullName("Minh2");
        role.setNoteRole(Role.NoteRole.EMPLOYEE);
        testEntityManager.merge(user);
        testEntityManager.flush();
        testEntityManager.clear();
    }

    //
//    //REMOVE
    @Test
    public void whenParentRemovedThenChildRemoved() {
        User user = testEntityManager.find(User.class, 1);
        testEntityManager.remove(user);
        testEntityManager.flush();
        testEntityManager.clear();
    }

    //
//    //REFRESH
//    //reread the value of a given instance from the database, not from the persistence object
//    @Test
//    public void whenParentRefreshedThenChildRefreshed() {
//        Employee employee = new Employee();
//        addEmployeeInfo(employee);
//        Note note = new Note();
//        addNoteInfo(note);
//        employee.setNotes(Arrays.asList(note));
//        testEntityManager.persist(employee);
//        testEntityManager.flush();
//        employee.setFirstName("TEST");
//        note.setNote("TEST REFRESH");
//        testEntityManager.refresh(employee);
//
//        assertThat(employee.getFirstName()).isEqualTo("TEST");
//        assertThat(note.getNote()).isEqualTo("TEST REFRESH");
//    }
//
//    //DETACH
//    //The detach operation removes the entity from the persistent context.
//    //When we use CascadeType.DETACH, the child entity will also get removed from the persistent context.
//    @Test
//    public void whenParentDetachedThenChildDetached() {
//        Role role = testEntityManager.find(Role.class, 1);
//        User user = role.getUsers().get(0);
//        assertThat(testEntityManager.getEntityManager().contains(user)).isTrue();
//        assertThat(testEntityManager.getEntityManager().contains(user)).isTrue();
//        testEntityManager.detach(user);
//        assertThat(testEntityManager.getEntityManager().contains(user)).isFalse();
//        assertThat(testEntityManager.getEntityManager().contains(role)).isFalse();
//    }
}