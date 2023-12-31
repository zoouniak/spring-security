package com.zoouniak.yoursell.service;

import com.zoouniak.yoursell.config.JwtService;
import com.zoouniak.yoursell.dto.LoginDTO;
import com.zoouniak.yoursell.dto.AuthenticationResponse;
import com.zoouniak.yoursell.dto.UserSignupDTO;
import com.zoouniak.yoursell.entity.Role;
import com.zoouniak.yoursell.entity.User;
import com.zoouniak.yoursell.exception.DuplicatedUserException;
import com.zoouniak.yoursell.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse signup(UserSignupDTO signupDTO) {

        if(userRepository.findByEmail(signupDTO.getEmail()).isPresent()){
            throw new DuplicatedUserException();
        }
        User user = User.builder()
                .email(signupDTO.getEmail())
                .password(passwordEncoder.encode(signupDTO.getPassword()))
                .address(signupDTO.getAddress())
                .name(signupDTO.getName())
                .phoneNumber(signupDTO.getPhoneNumber())
                .role(Role.USER)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(LoginDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        //need to generate token and send it back
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
