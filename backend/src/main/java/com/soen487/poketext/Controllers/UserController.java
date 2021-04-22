package com.soen487.poketext.Controllers;

import com.soen487.poketext.Model.User;
import com.soen487.poketext.Repository.UserRepository;

import com.soen487.poketext.Utils.JwtUtil;
import com.soen487.poketext.Model.AuthenticationRequest;
import com.soen487.poketext.Model.AuthenticationResponse;
import com.soen487.poketext.Service.UserDetailService;
import com.soen487.poketext.Utils.PasswordUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.OptionalInt;



import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@RestController
@RequestMapping(value="")
public class UserController extends Controller {

    @Autowired

    private final AuthenticationManager authenticationManager;

    @Autowired
    private final UserDetailService userDetailService;

    @Autowired
    private final JwtUtil jwtTokenUtil;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserController(AuthenticationManager authenticationManager, UserDetailService userDetailService, JwtUtil jwtTokenUtil, UserRepository userRepository) {
        super(userRepository);
        this.authenticationManager = authenticationManager;
        this.userDetailService = userDetailService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/auth", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception{





        try {
            this.authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        }catch (BadCredentialsException exception){
            throw new Exception("Incorrect username andor password");
        }
        final UserDetails userDetails = userDetailService.loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt, userDetails.getUsername()));
        //String rs = "username : " +userDetails.getUsername() + " Password: " + userDetails.getPassword();
        //return ResponseEntity.ok(new AuthenticationResponse(rs));

    }

    @PostMapping(value="/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public void createUser(@RequestBody User user) throws Exception {
        if (this.userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new Exception("Username is taken");
        }else {
            User new_user = new User(user.getUsername(), PasswordUtilities.passwordEncoding(user.getPassword()));
            this.userRepository.save(new_user);
        }
    }

    @PostMapping(value="/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public @ResponseBody ResponseEntity<String> login(@RequestBody User user){
        Optional<User> optionalUser = this.userRepository.findByUsername(user.getUsername());

        if (optionalUser.isPresent()){
            User existingUser = optionalUser.get();
            if(PasswordUtilities.isPasswordMatch(user.getPassword(), existingUser.getPassword())){
                user.setToken("GENERATED TOKEN"); //<- Generate JWT
                this.userRepository.save(user);

                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.set("token", user.getToken());

                return ResponseEntity.ok()
                        .headers(responseHeaders)
                        .body("Login Successful");
            }
            else{
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Wrong password");
            }
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Signup first");
        }
    }


//    @PostMapping(value="/login", consumes = MediaType.APPLICATION_JSON_VALUE)
//    @CrossOrigin
//    public @ResponseBody ResponseEntity<String> login(@RequestBody User user){
//        Optional<User> existingUser = this.userRepository.findByUsername(user.getUsername());
//        if(existingUser.isPresent()) {
//            String token = "dummytoken"; // <- generate JWT
//            existingUser.get().setToken(token);
//            HttpHeaders responseHeaders = new HttpHeaders();
//            responseHeaders.set("token", token);
//
//            return ResponseEntity.ok()
//                    .headers(responseHeaders)
//                    .body("Login Successful");
//
//        } else{
//            return ResponseEntity.status(401)
//                    .body("Login Failed");
//        }
//    }

    @PostMapping(value="logout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public String logout(@RequestParam("username") String username){
        return "";
    }

    @GetMapping(value="/info")
    @CrossOrigin
    public @ResponseBody ResponseEntity<User> getInfo(@RequestHeader HttpHeaders headers){
        User user = getUserByHeadersToken(headers);
        return ResponseEntity.ok(user);
    }

    @PutMapping(value="/updatewin")
    @CrossOrigin
    public @ResponseBody ResponseEntity<String> updateWin(@RequestHeader HttpHeaders headers){
        User user = getUserByHeadersToken(headers);
        user.setWincount(user.getWincount()+1);
        this.userRepository.save(user);
        return ResponseEntity.ok("Updated Win count");
    }

    @PutMapping(value="/updateloss")
    @CrossOrigin
    public @ResponseBody ResponseEntity<String> updateLoss(@RequestHeader HttpHeaders headers){
        User user = getUserByHeadersToken(headers);
        user.setLosscount(user.getLosscount()+1);
        this.userRepository.save(user);
        return ResponseEntity.ok("Updated Loss count");
    }
}
