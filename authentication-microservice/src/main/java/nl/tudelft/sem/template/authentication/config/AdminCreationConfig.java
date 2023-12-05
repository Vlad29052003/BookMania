//package nl.tudelft.sem.template.authentication.config;
//
//import nl.tudelft.sem.template.authentication.domain.user.AppUser;
//import nl.tudelft.sem.template.authentication.domain.user.Authority;
//import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
//import nl.tudelft.sem.template.authentication.domain.user.Password;
//import nl.tudelft.sem.template.authentication.domain.user.PasswordHashingService;
//import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
//import nl.tudelft.sem.template.authentication.domain.user.Username;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class AdminCreationConfig implements ApplicationRunner {
//    private final transient UserRepository userRepository;
//    private final transient PasswordEncoder passwordEncoder;
//
//    @Autowired
//    public AdminCreationConfig(UserRepository userRepository,
//                               PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        PasswordHashingService passwordHashingService = new PasswordHashingService(passwordEncoder);
//        AppUser admin = new AppUser(new Username("administrator"),
//                "admin@email",
//                new HashedPassword(passwordHashingService.hash(new Password("pass123")).toString()));
//        admin.setAuthority(Authority.ADMIN);
//        userRepository.saveAndFlush(admin);
//    }
//}
