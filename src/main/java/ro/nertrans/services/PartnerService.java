package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.nertrans.models.Partner;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PartnerRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PartnerService {
    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    UserService userService;

    /**
     * @Description: Creates a new partner
     * @param partner - the new partner
     * @param request - used to find the current user
     * @return String
     */
    public String addPartner(Partner partner, HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }
        if (partnerRepository.getByRegistrationCodeIgnoreCase(partner.getRegistrationCode()) != null) {
            return "registrationCodeMustBeUnique";
        }
        Optional<User> currentUser = userService.getCurrentUser(request);
        partner.setId(null);
        partner.setDate(LocalDateTime.now());
        partner.setUserId(currentUser.get().getId());
        partnerRepository.save(partner);
        return partner.getId();
    }

    /**
     * @Description: Returns a single partner
     * @param request - used to find the current user
     * @param partnerId - used to find the partner
     * @return Object
     */
    public Object getPartnerById(HttpServletRequest request, String partnerId){
        if (userService.getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }else
        return partnerRepository.findById(partnerId);
    }

    /**
     * @Description: Deletes permanently a partner
     * @param request - used to find the current user
     * @param partnerId - used to find the partner to delete
     * @return String
     */
    public String deletePartnerById(HttpServletRequest request, String partnerId){
        if (userService.getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }
        if (partnerRepository.findById(partnerId).isPresent()){
            partnerRepository.deleteById(partnerId);
            return "success";
        }else return "invalidId";
    }

    /**
     * @Description: Updates a partner
     * @param request - used to find the current user
     * @param partner - the new partner
     * @param partnerId - used to find the partner to update
     * @return String
     */
    public String updatePartner(HttpServletRequest request, Partner partner, String partnerId){
        if (userService.getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }
        Optional<Partner> partner1 = partnerRepository.findById(partnerId);
        partner1.get().setAddress(partner.getAddress());
        partner1.get().setTelephone(partner.getTelephone());
        partner1.get().setEmail(partner.getEmail());
        partner1.get().setName(partner.getName());
        if (partnerRepository.getByRegistrationCodeIgnoreCase(partner.getRegistrationCode()) != null &&
                !partnerRepository.getByRegistrationCodeIgnoreCase(partner.getRegistrationCode()).getId().equalsIgnoreCase(partnerId)){
            return "registrationCodeMustBeUnique";
        }
        partner1.get().setRegistrationCode(partner.getRegistrationCode());
        partnerRepository.save(partner1.get());
        return "success";
    }
}
