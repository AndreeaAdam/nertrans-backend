package ro.nertrans.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.nertrans.JSON.StringSuccessJSON;
import ro.nertrans.dtos.PartnerEditDTO;
import ro.nertrans.models.Partner;
import ro.nertrans.services.PartnerService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;

@CrossOrigin
@RestController
public class PartnerController {
    @Autowired
    private PartnerService partnerService;

    /**
     * @Description: Creates a new partner
     * @param partner - the new partner
     * @param request - used to find the current user
     * @return StringSuccessJSON
     */
    @RequestMapping(value = "/addPartner", method = RequestMethod.POST)
    @ApiResponse(description = "Creates a new partner")
    public ResponseEntity<?> addPartner(@RequestBody Partner partner,
                                     HttpServletRequest request) {
        String response = partnerService.addPartner(partner, request);
        if (response.equals(partner.getId())) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.CREATED);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    /**
     * @Description: Returns a single partner
     * @param partnerId - used to find the partner
     * @return Object
     */
    @RequestMapping(value = "/getPartnerById", method = RequestMethod.GET)
    @ApiResponse(description = "Returns a single partner")
    public ResponseEntity<?> getPartnerById(@RequestParam(value = "partnerId") String partnerId) {
        return new ResponseEntity<>(partnerService.getPartnerById(partnerId), HttpStatus.OK);
    }

    @RequestMapping(value = "/getPartners", method = RequestMethod.POST)
    @ApiResponse(description = "Returns partners from a list of ids")
    public ResponseEntity<?> getPartners(@RequestBody ArrayList<String> ids) {
        return new ResponseEntity<>(partnerService.getPartners(ids), HttpStatus.OK);
    }


    /**
     * @Description: Updates a partner
     * @param request - used to find the current user
     * @param partnerEditDTO - the new partner
     * @param partnerId - used to find the partner to update
     * @return StringSuccessJSON
     */
    @RequestMapping(value = "/updatePartner", method = RequestMethod.PUT)
    @ApiResponse(description = "Updates a partner")
    public ResponseEntity<?> updatePartner(@RequestBody PartnerEditDTO partnerEditDTO,
                                           HttpServletRequest request,
                                           @RequestParam(value = "partnerId") String partnerId) {
        String response = partnerService.updatePartner(request, partnerEditDTO, partnerId);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    /**
     * @Description: Deletes permanently a partner
     * @param request - used to find the current user
     * @param partnerId - used to find the partner to delete
     * @return StringSuccessJSON
     */
    @RequestMapping(value = "/deletePartnerById", method = RequestMethod.DELETE)
    @ApiResponse(description = "Deletes permanently a partner")
    public ResponseEntity<?> deletePartnerById(@RequestParam(value = "partnerId") String partnerId,
                                               HttpServletRequest request) {
        String response = partnerService.deletePartnerById(request, partnerId);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    /**
     * @Description: Imports a list with partners
     * @param request - used to find the current user
     * @param file - the actual file to import
     * @return boolean
     */
    @RequestMapping(value = "/importPartners", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importPartners(@RequestParam(value = "file") MultipartFile file,
                                         HttpServletRequest request) {
        try {
            return new ResponseEntity<>(partnerService.importPartners(file, request), HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
    }
}
