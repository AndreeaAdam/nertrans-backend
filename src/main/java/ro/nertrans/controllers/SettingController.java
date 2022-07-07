package ro.nertrans.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.nertrans.JSON.StringSuccessJSON;
import ro.nertrans.models.Setting;
import ro.nertrans.services.FileService;
import ro.nertrans.services.SettingService;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@RestController
public class SettingController {
    @Autowired
    private SettingService settingService;

    @Autowired
    private FileService fileService;

    /**
     * @return Optional<Setting>
     * @Description: Returns the setting
     */
    @RequestMapping(value = "/getSettings", method = RequestMethod.GET)
    @ApiResponse(description = " Returns the setting")
    public ResponseEntity<?> getSettings() {
        return new ResponseEntity<>(settingService.getSettings(), HttpStatus.OK);
    }

    /**
     * @param setting - the actual setting being created
     * @param request - used to find the current user
     * @return boolean
     * @Description: Updates settings
     */
    @Secured({"ROLE_super_admin"})
    @RequestMapping(value = "/updateSetting", method = RequestMethod.PUT)
    @ApiResponse(description = "Updates settings")
    public ResponseEntity<?> updateSetting(@RequestBody Setting setting,
                                           HttpServletRequest request) {
        String response = settingService.updateSetting(request, setting);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/uploadMobilPayKey", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMobilPayKey(@RequestParam("file") MultipartFile file,
                                               HttpServletRequest request,
                                               @RequestParam("isPublicKey") boolean isPublicKey) {
        return fileService.uploadMobilPayKey(file, request, isPublicKey);
    }

}
