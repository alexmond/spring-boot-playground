package org.alexmond.sample.jpa.repository;

import org.alexmond.sample.auth.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    List<User> users = new ArrayList<>();

    public List<User> findAll(){
        return users;
    }

    public User addUser(User user){
        users.add(user);
        return user;
    }

    public Optional<User> updateUser(int id, String newName){
        Optional<User> findUser =  users.stream()
                .filter(user -> user.getId()==id)
                .findFirst();
        findUser.ifPresent(user -> user.setName(newName));
        return findUser;
    }

    public boolean deleteUser(int id){
        return users.removeIf(user -> user.getId() == id);
    }


}
