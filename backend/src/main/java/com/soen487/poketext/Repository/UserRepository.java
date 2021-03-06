package com.soen487.poketext.Repository;

import com.soen487.poketext.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT u FROM User u WHERE u.username=:username AND u.password=:password")
    Optional<User> findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);


    Optional<User> findByUsername(String username);
    Optional<User> findByToken(String token);

}
