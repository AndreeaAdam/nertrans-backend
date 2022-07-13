package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.nertrans.models.Partner;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PartnerRepository;
import ro.nertrans.repositories.PaymentDocumentRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PartnerService {
    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;

    @Autowired
    private NumberCounterService numberCounterService;

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
        if (partnerRepository.getByCUIIgnoreCase(partner.getCUI()) != null) {
            return "CUIMustBeUnique";
        }
        Optional<User> currentUser = userService.getCurrentUser(request);
        partner.setId(null);
        partner.setNumberPartner(Long.toString(numberCounterService.getNextPartner()));
        partner.setDate(LocalDateTime.now());
        partner.setUserId(currentUser.get().getId());
        partnerRepository.save(partner);
        return partner.getId();
    }

    /**
     * @Description: Returns a single partner
     * @param partnerId - used to find the partner
     * @return Object
     */
    public Object getPartnerById(String partnerId){
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
    public String updatePartner(HttpServletRequest request, Partner partner, String partnerId) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        Optional<Partner> partner1 = partnerRepository.findById(partnerId);
        partner1.get().setAddress(partner.getAddress());
        partner1.get().setTelephone(partner.getTelephone());
        partner1.get().setEmail(partner.getEmail());
        partner1.get().setName(partner.getName());
        partner1.get().setCity(partner.getCity());
        partner1.get().setCountry(partner.getCountry());
        partner1.get().setVATPayer(partner.isVATPayer());
        if (partnerRepository.getByCUIIgnoreCase(partner.getCUI()) != null &&
                !partnerRepository.getByCUIIgnoreCase(partner.getCUI()).getId().equalsIgnoreCase(partnerId)) {
            return "registrationCodeMustBeUnique";
        }
        if (!partner.getName().equalsIgnoreCase(partner1.get().getName())) {
            List<PaymentDocument> documents = paymentDocumentRepository.findAll().stream().filter(paymentDocument -> paymentDocument.getPartnerId().equalsIgnoreCase(partnerId)).collect(Collectors.toList());
            documents.forEach(paymentDocument -> {
                paymentDocument.setPartnerName(partner.getName());
                paymentDocumentRepository.save(paymentDocument);
            });
        }
        partner1.get().setCUI(partner.getCUI());
        partnerRepository.save(partner1.get());
        return "success";
    }

    /**
     * @Description: Returns partners from a list of ids
     * @param ids - array
     * @return List<Partner>
     */
    public List<Partner> getPartners(ArrayList<String> ids){
        List<Partner> partners = new ArrayList<>();
        for (String id: ids) {
            if (partnerRepository.findById(id).isPresent()){
                partners.add(partnerRepository.findById(id).get());
            }
        }
        return partners;
    }
}
