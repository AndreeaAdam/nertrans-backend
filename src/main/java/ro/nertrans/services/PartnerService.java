package ro.nertrans.services;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.nertrans.models.Partner;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PartnerRepository;
import ro.nertrans.repositories.PaymentDocumentRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public boolean importPartners(MultipartFile reapExcelDataFile, HttpServletRequest request) throws IOException {
        if (userService.getCurrentUser(request).isEmpty()){
            return false;
        }
        XSSFWorkbook workbook = new XSSFWorkbook(reapExcelDataFile.getInputStream());
        if (reapExcelDataFile.isEmpty() ||
                !FilenameUtils.getExtension(Objects.requireNonNull(reapExcelDataFile.getOriginalFilename()).toLowerCase()).equals("xlsx")) {
            return false;
        }
        XSSFSheet worksheet = workbook.getSheetAt(0);
        Optional<User> user = userService.getCurrentUser(request);
        try {
            for (int i = 1; i < worksheet.getPhysicalNumberOfRows(); i++) {
                Partner partner = new Partner();
                partner.setId(null);
                partner.setNumberPartner(Long.toString(numberCounterService.getNextPartner()));
                partner.setDate(LocalDateTime.now());
                partner.setUserId(user.get().getId());

                XSSFRow row = worksheet.getRow(i);
                int cell = 0;

                if (row.getCell(cell) != null) {
                    partner.setName(row.getCell(cell++).toString());
                } else {
                    partner.setName(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setCIF(row.getCell(cell++).toString());
                } else {
                    partner.setCIF(row.getCell(cell++).toString());
                }
                int CUICell = 0;
                if (row.getCell(cell) != null) {
//                    if (partnerRepository.getByCUIIgnoreCase(row.getCell(cell).toString()) == null) {
//                        partner.setCUI(row.getCell(cell++).toString());
//                    }
//                    CUICell = cell;
//                } else {
                    partner.setCUI(row.getCell(cell++).toString());
                }
//                if (row.getCell(cell) != null) {
//                    partner.setUserId(row.getCell(cell++).toString());
//                } else {
//                    partner.setUserId(row.getCell(cell++).toString());
//                }
                if (row.getCell(cell) != null) {
                    partner.setAddress(row.getCell(cell++).toString());
                } else {
                    partner.setAddress(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setCity(row.getCell(cell++).toString());
                } else {
                    partner.setCity(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setCounty(row.getCell(cell++).toString());
                } else {
                    partner.setCounty(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setBank(row.getCell(cell++).toString());
                } else {
                    partner.setBank(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setIban(row.getCell(cell++).toString());
                } else {
                    partner.setIban(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setCountry(row.getCell(cell++).toString());
                } else {
                    partner.setCountry(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setEmail(row.getCell(cell++).toString());
                } else {
                    partner.setEmail(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setContact(row.getCell(cell++).toString());
                } else {
                    partner.setContact(row.getCell(cell++).toString());
                }
                if (row.getCell(cell) != null) {
                    partner.setTelephone(row.getCell(cell++).toString());
                } else {
                    partner.setTelephone(row.getCell(cell++).toString());
                }
                if (partnerRepository.getByCUIIgnoreCase(row.getCell(CUICell).toString()) == null) {
                    partnerRepository.save(partner);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
