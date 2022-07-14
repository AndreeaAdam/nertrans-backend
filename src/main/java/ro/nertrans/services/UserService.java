package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ro.nertrans.config.SecurityConfig;
import ro.nertrans.config.UserRoleEnum;
import ro.nertrans.dtos.UserDTO;
import ro.nertrans.dtos.UserEditDTO;
import ro.nertrans.models.RegActivationCode;
import ro.nertrans.models.User;
import ro.nertrans.repositories.RegActivationCodeRepository;
import ro.nertrans.repositories.UserRepository;
import ro.nertrans.utils.RandomCodeService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityConfig securityConfig;
    @Autowired
    private RegActivationCodeRepository regActivationCodeRepository;
    @Autowired
    private RandomCodeService randomCodeService;
    @Autowired
    private MailService mailService;

    @Autowired
    private NumberCounterService numberUserService;

    @Value("${apiUrl}")
    private String apiUrl;
    @Value("${apiUrlPort}")
    private String apiUrlPort;

    /**
     * @Description: Creates the super admin, when application is first running
     */
    @EventListener(ApplicationReadyEvent.class)
    private void createSuperAdmin() {
        boolean adminExists = userRepository.existsById("1");
        if (!adminExists) {
            User user = new User();
            user.setId("1");
            user.setNumberUser(Long.toString(numberUserService.getNextUser()));
            user.setEmail("admin@admin.ro");
            user.setFirstName("Admin");
            user.setLastName("Admin");
            user.setActive(true);
            user.setActLikeAdmin(true);
            user.setRegistrationDate(LocalDateTime.now());
            ArrayList<UserRoleEnum> roles = new ArrayList<>();
            roles.add(UserRoleEnum.ROLE_super_admin);
            user.setRoles(roles);
            user.setPassword(securityConfig.passwordEncoder().encode("admin"));
            userRepository.save(user);
        }
    }

    /**
     * @Description: Creates a new user account
     * @param user - the new user
     * @param request - used to find the current user
     * @return String
     */
    public String addUser( UserDTO user, HttpServletRequest request){
        if (getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }
        Optional<User> currentUser = getCurrentUser(request);
        if (!currentUser.get().getRoles().contains(UserRoleEnum.ROLE_super_admin)){
            return "notAllowed";
        }
        if (userRepository.getByEmail(user.getEmail()).isPresent()) {
            return "emailExists";
        }
        User user1 = new User();
        user1.setId(null);
        user1.setNumberUser(Long.toString(numberUserService.getNextUser()));
        user1.setActive(false);
        user1.setEmail(user.getEmail());
        user1.setFirstName(user.getFirstName());
        user1.setLastName(user.getLastName());
        user1.setAddress(user.getAddress());
        user1.setTelephone(user.getTelephone());
        user1.setEmployeeCode(user.getEmployeeCode());
        user1.setActLikeAdmin(user.isActLikeAdmin());
        user1.setOffice(user.getOffice());
        user1.setRegistrationDate(LocalDateTime.now());
        user1.setName(user.getFirstName() + " " + user.getLastName());
        ArrayList<UserRoleEnum> userAuthorities = new ArrayList<>();
        userAuthorities.add(UserRoleEnum.ROLE_employee);
        user1.setRoles(userAuthorities);
        if (user.getPassword().length() < 5){
            return "passwordTooShort";
        }
        user1.setPassword(securityConfig.passwordEncoder().encode(user.getPassword()));
        userRepository.save(user1);
        return user1.getId();
    }

    /**
     * @Description: Sends email for activate account
     * @param userId - used to find the user
     * @return boolean
     */
    public boolean sendRegistrationEmail(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.get().isActive()) {
            return false;
        }
        try {
            String registrationCode = randomCodeService.generateRandomCode();
            String url = "<p style=\"margin:0 0 16px\">Pentru a finaliza înregistrarea unui cont nou, trebuie să dați click pe link-ul de mai jos.</p>" +
                    apiUrlPort + "/nertrans/activateUser?registrationCode=" + registrationCode;
            createCode(user.get().getEmail(), user.get().getId(), registrationCode);
            mailService.sendMailSMTPGeneric(user.get().getEmail(), "Confirmare adresă de email NERTRANS", url);
        } catch (Exception e) {
            e.printStackTrace();
            userRepository.deleteById(user.get().getId());
            return false;
        }
        return true;
    }

    /**
     * @Description: Creates a registration code for the user
     * @Details: The code has an expiry date set at current date + 2 days
     * @param userEmail - string used to find the userEmail
     * @param userId - id used to find the user
     * @param registrationCode - string used to find the registrationCode
     */
    public void createCode(String userEmail, String userId, String registrationCode) {
        RegActivationCode code = new RegActivationCode();
        code.setEmail(userEmail);
        code.setUserId(userId);
        code.setRegistrationCode(registrationCode);
        code.setCreationDate(LocalDateTime.now());
        code.setExpiryDate(LocalDateTime.now().plusDays(2));
        regActivationCodeRepository.save(code);
    }
    /**
     * @Description: Returns current user info
     * @param request - used to find the current user
     * @return Optional<User>
     */
    public Optional<User> getCurrentUser(HttpServletRequest request) {
        if (request == null){
            return Optional.empty();
        }else return userRepository.getByEmail(request.getUserPrincipal().getName());
    }
    /**
     * @Description: Returns a list with all users
     * @return List<User>
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    /**
     * @Description: Activates an user via the registration code
     * @param registrationCode - string used to find the registrationCode
     * @return boolean
     */
    public boolean activateUser(String registrationCode) {
        RegActivationCode code = regActivationCodeRepository.findAll().stream().filter(regActivationCode -> regActivationCode.getRegistrationCode().equalsIgnoreCase(registrationCode)).collect(Collectors.toList()).get(0);
        if (code.getRegistrationCode().equals(registrationCode) && code.getExpiryDate().isAfter(LocalDateTime.now())) {
            Optional<User> user = userRepository.getByEmail(code.getEmail());
            user.get().setActive(true);
            userRepository.save(user.get());
            regActivationCodeRepository.deleteById(code.getId());
            return true;
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;
    }
    /**
     * @Description: Returns a user by his id
     * @param userId - used to find the user
     * @return Optional<User>
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * @Description: Updates a specific user
     * @param request - used to find the current user
     * @param user - the new user
     * @param userId - used to find the user to update
     * @return String
     */
    public String updateUser(HttpServletRequest request, UserEditDTO user, String userId){
        if (getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }
        Optional<User> currentUser = getCurrentUser(request);
        Optional<User> targetUser = userRepository.findById(userId);
        if (!currentUser.get().getRoles().contains(UserRoleEnum.ROLE_super_admin)){
            return "notAllowed";
        }
        targetUser.get().setTelephone(user.getTelephone());
        targetUser.get().setAddress(user.getAddress());
        targetUser.get().setOffice(user.getOffice());
        targetUser.get().setEmployeeCode(user.getEmployeeCode());
        targetUser.get().setLastName(user.getLastName());
        targetUser.get().setFirstName(user.getFirstName());
        targetUser.get().setActLikeAdmin(user.isActLikeAdmin());
        targetUser.get().setName(user.getFirstName() + " " + user.getLastName());
        userRepository.save(targetUser.get());
        return "success";
    }
    /**
     * @Description: Allows super admin to change user password
     * @param request - used to find the current user
     * @param userId - used to find the target user
     * @param newPassword - the new password
     * @return boolean
     */
    public String changePasswordForUser(HttpServletRequest request, String userId, String newPassword) {
        Optional<User> user = userRepository.findById(userId);
        Optional<User> currentUser = getCurrentUser(request);
        if (currentUser.get().getRoles().contains(UserRoleEnum.ROLE_super_admin)) {
            user.get().setPassword(securityConfig.passwordEncoder().encode(newPassword));
            userRepository.save(user.get());
            return "success";
        } else return "notAllowed";
    }

    /**
     * @Description: Deletes permanently a user
     * @param userId - used to find the user to delete
     * @param  request- used to find the current user
     * @return boolean
     */
    public boolean deleteUser(String userId, HttpServletRequest request) {
        if (getCurrentUser(request).get().getRoles().contains(UserRoleEnum.ROLE_super_admin) && userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        } else return false;
    }


    /**
     * @Description: Changes a user's status (active-boolean)
     * @param request - used to find the current user
     * @param userId - used to find the user
     * @param active - boolean
     * @return boolean
     */
    public boolean changeUserStatus(HttpServletRequest request, String userId, boolean active) {
        if (getCurrentUser(request).get().getRoles().contains(UserRoleEnum.ROLE_super_admin) && userRepository.existsById(userId)) {
            Optional<User> user = userRepository.findById(userId);
            user.get().setActive(active);
            userRepository.save(user.get());
            return true;
        } else return false;
    }
}
