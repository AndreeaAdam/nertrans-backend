package ro.nertrans.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.JSON.StringSuccessJSON;
import ro.nertrans.models.Setting;
import ro.nertrans.services.SettingService;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class SettingController {
    @Autowired
    private SettingService settingService;
    /**
     * @Description: Returns the setting
     * @return Optional<Setting>
     */
    @RequestMapping(value = "/getSettings", method = RequestMethod.GET)
    @ApiResponse(description = " Returns the setting")
    public ResponseEntity<?> getSettings() {
        return new ResponseEntity<>(settingService.getSettings(), HttpStatus.OK);
    }

    /**
     * @Description: Updates settings
     * @param setting - the actual setting being created
     * @param request - used to find the current user
     * @return boolean
     */
    @Secured({"ROLE_super_admin"})
    @RequestMapping(value = "/updateSetting", method = RequestMethod.PUT)
    @ApiResponse(description = "Updates settings")
    public ResponseEntity<?> updateSetting(@RequestBody Setting setting,
                                           HttpServletRequest request) {
        String response = settingService.updateSetting(request, setting);
        if (response.equalsIgnoreCase("success")){
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        }else return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

}
