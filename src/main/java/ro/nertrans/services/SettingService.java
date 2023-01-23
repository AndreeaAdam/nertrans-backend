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
    @Autowired
    private FileService fileService;

    /**
     * @Description: Creates setting when application is first running
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createSetting() {
        if (settingRepository.findAll().size() == 0) {
            Setting setting = new Setting();
            setting.setId(null);
            settingRepository.save(setting);
            fileService.createSettingFolder();
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
            Optional<Setting> setting1 = getSettings();
            setting1.get().setSmartBillEmail(setting.getSmartBillEmail());
            setting1.get().setSmartBillToken(setting.getSmartBillToken());
            setting1.get().setSmartBillFiscalCode(setting.getSmartBillFiscalCode());
            setting1.get().setUserOffices(setting.getUserOffices());
            setting1.get().setNetopiaSignature(setting.getNetopiaSignature());
            setting1.get().setNetopiaPrivateKey(setting.getNetopiaPrivateKey());
            setting1.get().setNetopiaPublicKey(setting.getNetopiaPublicKey());
            settingRepository.save(setting1.get());
            return "success";
        } else return "notAllowed";
    }

    /**
     * @Description: Returns the setting
     * @return Optional<Setting>
     */
    public Optional<Setting> getSettings() {
        if (!settingRepository.findAll().isEmpty()) {
            return settingRepository.findById(settingRepository.findAll().get(settingRepository.findAll().size() - 1).getId());
        } else return Optional.empty();
    }
}
