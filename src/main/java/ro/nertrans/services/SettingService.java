package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ro.nertrans.config.UserRoleEnum;
import ro.nertrans.models.Setting;
import ro.nertrans.models.User;
import ro.nertrans.repositories.SettingRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
public class SettingService {
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private UserService userService;

    @EventListener(ApplicationReadyEvent.class)
    public void createSetting() {
        if (settingRepository.findAll().size() == 0) {
            Setting setting = new Setting();
            setting.setId(null);
            settingRepository.save(setting);
        }
    }

    /**
     * @Description: Updates settings
     * @param setting - the actual setting being created
     * @param request - used to find the current user
     * @return boolean
     */
    public String updateSetting(HttpServletRequest request, Setting setting) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        Optional<User> user = userService.getCurrentUser(request);
        if (user.get().getRoles().contains(UserRoleEnum.ROLE_super_admin)) {
            Optional<Setting> setting1;
            setting1 = Optional.ofNullable(setting);
            settingRepository.save(setting1.get());
            return "success";
        } else return "notAllowed";
    }

    /**
     * @Description: Returns the setting
     * @return Optional<Setting>
     */
    public Optional<Setting> getSettings() {
        if (settingRepository.findAll().size() > 0) {
            return settingRepository.findById(settingRepository.findAll().get(settingRepository.findAll().size() - 1).getId());
        } else return Optional.empty();
    }
}
