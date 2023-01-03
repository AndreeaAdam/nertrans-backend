package ro.nertrans.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import ro.nertrans.json.StringSuccessJSON;
import ro.nertrans.json.SuccessJSON;
import ro.nertrans.config.CustomAuthenticationProvider;
import ro.nertrans.dtos.UserDTO;
import ro.nertrans.dtos.UserEditDTO;
import ro.nertrans.dtos.UserLoginDTO;
import ro.nertrans.models.User;
import ro.nertrans.repositories.UserRepository;
import ro.nertrans.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CustomAuthenticationProvider authManager;
    @Value("${apiUrl}")
    private String apiUrl;

    /**
     * @Description: Register user
     * @param user - the actual user being registered
     * @param  request- used to find the current user
     * @return StringSuccessJSON
     */
    @Secured({"ROLE_super_admin"})
    @PostMapping(value = "/addUser")
    @ApiResponse(description = "Registers an user")
    public ResponseEntity<StringSuccessJSON> addUser(@RequestBody UserDTO user, HttpServletRequest request) {
        String response = userService.addUser(user, request);
        if (response.equals("emailExists") || response.equals("youAreNotLoggedIn") || response.equals("notAllowed") || response.equals("passwordTooShort")
                || response.equals("invalidRole")) {
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.CREATED);
    }
    /**
     * @Description: Logs in a user into upsell platform
     * @param userLogin - login dto
     * @param request  used to find out the current user
     * @return successJSON/stringSuccessJSON
     */
    @PostMapping(value = "/login")
    @ApiResponse(description = "Logs in a user ")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLogin, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword());
        if (userRepository.getByEmailIgnoreCase(userLogin.getEmail()) == null) {
            return new ResponseEntity<>(new StringSuccessJSON(false, "invalidEmail"), HttpStatus.BAD_REQUEST);
        }
        User currentUser = userRepository.getByEmailIgnoreCase(userLogin.getEmail());

        if (!currentUser.isActive()) {
            return new ResponseEntity<>(new StringSuccessJSON(false, "userNotActive"), HttpStatus.BAD_REQUEST);
        }
        try {
            Authentication auth = authManager.authenticate(authentication);
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
            return new ResponseEntity<>(new SuccessJSON(true), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new SuccessJSON(false), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping(value = "/activateUser")
    @ApiResponse(description = "Activates an user via the registrationCode")
    public ModelAndView activateUser(@RequestParam(value = "registrationCode") String registrationCode) {
        boolean success = userService.activateUser(registrationCode);
        if (success) {
            return new ModelAndView("redirect:" + apiUrl + "/autentificare");
        } else {
            return new ModelAndView("redirect:" + apiUrl + "/activare-cont");
        }
    }

    /**
     * @Description: Returns a user by id
     * @param userId - id used to find the user
     * @return Optional<User>
     */
    @GetMapping(value = "/getUserById")
    @ApiResponse(description = "Returns an user by id")
    public ResponseEntity<Optional<User>> getUserById(@RequestParam(value = "userId") String userId) {
        return new ResponseEntity<>(userService.getUserById(userId), HttpStatus.OK);
    }

    /**
     * @Description: Returns current user info
     * @param request - used to find the current user
     * @return Optional<User>
     */
    @GetMapping(value = "/getCurrentUser")
    @ApiResponse(description = "Returns current user info")
    public ResponseEntity<Optional<User>> getCurrentUser(HttpServletRequest request) {
        return new ResponseEntity<>(userService.getCurrentUser(request), HttpStatus.OK);
    }

    @GetMapping(value = "/sendRegistrationEmail")
    @ApiResponse(description = "Sends email for activate account")
    public ResponseEntity<SuccessJSON> sendRegistrationEmail(@RequestParam(value = "userId") String userId) {
        boolean response = userService.sendRegistrationEmail(userId);
        if (response){
            return new ResponseEntity<>(new SuccessJSON(true), HttpStatus.OK);
        }else return new ResponseEntity<>(new SuccessJSON(false), HttpStatus.BAD_REQUEST);
    }

    /**
     * @return List<User>
     * @Description: Returns a list with all users
     */
    @Secured({"ROLE_super_admin"})
    @GetMapping(value = "/getAllUsers")
    @ApiResponse(description = " Returns a list with all users")
    public ResponseEntity<List<User>> getAllUser() {
        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
    }

    @Secured({"ROLE_super_admin"})
    @PutMapping(value = "/updateUser")
    @ApiResponse(description = "Updates an user by id")
    public ResponseEntity<StringSuccessJSON> updateUser(@RequestBody UserEditDTO user,
                                        HttpServletRequest request,
                                        @RequestParam(value = "userId") String userId) {
        String response = userService.updateUser(request, user, userId);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    /**
     * @Description: Deletes permanently a user
     * @param userId - used to find the user to delete
     * @param  request- used to find the current user
     * @return SuccessJSON
     */
    @Secured({"ROLE_super_admin"})
    @DeleteMapping(value = "/deleteUser")
    @ApiResponse(description = "Deletes an user (only for super admin)")
    public ResponseEntity<SuccessJSON> deleteUser(@RequestParam(value = "userId") String userId, HttpServletRequest request) {
        boolean response = userService.deleteUser(userId, request);
        if (response){
            return new ResponseEntity<>(new SuccessJSON(true), HttpStatus.OK);
        }else return new ResponseEntity<>(new SuccessJSON(false), HttpStatus.BAD_REQUEST);
    }
    @Secured({"ROLE_super_admin"})
    @PutMapping(value = "/changePasswordForUser")
    @ApiResponse(description = " Allows super admin to change user password")
    public ResponseEntity<StringSuccessJSON> changePasswordForUser(@RequestParam(value = "userId") String userId,
                                                   @RequestParam(value = "newPassword") String newPassword,
                                                   HttpServletRequest request) {
        String response = userService.changePasswordForUser(request, userId, newPassword);
        if (response.equalsIgnoreCase("success")){
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        }else return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);

    }
    /**
     * @Description: Changes a user's status (active-boolean)
     * @param request - used to find the current user
     * @param userId - used to find the user
     * @param active - boolean
     * @return SuccessJSON
     */
    @Secured({"ROLE_super_admin"})
    @PutMapping(value = "/changeUserStatus")
    @ApiResponse(description = "Changes a user's status (active-boolean)")
    public ResponseEntity<SuccessJSON> changeSubUserStatus(@RequestParam(value = "userId") String userId,
                                                 @RequestParam(value = "active") boolean active,
                                                 HttpServletRequest request) {
        return new ResponseEntity<>(new SuccessJSON(userService.changeUserStatus(request, userId, active)), HttpStatus.OK);
    }

}
