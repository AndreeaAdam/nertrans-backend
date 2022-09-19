package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import ro.nertrans.dtos.OfficeDTO;
import ro.nertrans.dtos.OfficeNumberDTO;
import ro.nertrans.models.Partner;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.Setting;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PartnerRepository;
import ro.nertrans.repositories.PaymentDocumentRepository;
import ro.nertrans.repositories.SettingRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentDocumentService {

    @Autowired
    private UserService userService;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private SettingService settingService;
    @Autowired
    private SettingRepository settingRepository;
    @Autowired
    private PartnerRepository partnerRepository;

    /**
     * @Description: Creates a new payment document
     * @param request - used to find the current user
     * @param paymentDocument - the actual document being created
     * @return String
     */
    public String createPaymentDocument(HttpServletRequest request, PaymentDocument paymentDocument) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        if (settingService.getSettings().get().getUserOffices() != null && settingService.getSettings().get().getUserOffices().stream().filter(officeDTO -> officeDTO.getCode().equalsIgnoreCase(paymentDocument.getDocSeries())).findFirst().isEmpty()){
            return "invalidDocSeries";
        }
        paymentDocument.setId(null);
        paymentDocument.setDate(LocalDateTime.now());
        paymentDocument.setUserId(userService.getCurrentUser(request).get().getId());
        paymentDocument.setDocNumber(incrementDocNumberByOffice(paymentDocument.getDocSeries()));
        if (paymentDocumentRepository.findByDocSeriesAndDocNumber(paymentDocument.getDocSeries(), paymentDocument.getDocNumber()).isPresent()){
            return "alreadyExists";
        }
        if (paymentDocument.getPartnerId() != null && partnerRepository.findById(paymentDocument.getPartnerId()).isPresent()){
            paymentDocument.setPartnerName(partnerRepository.findById(paymentDocument.getPartnerId()).get().getName());
        }
        paymentDocument.setLocalReferenceNumber(paymentDocument.getDocSeries() + " " + paymentDocument.getDocNumber());
        paymentDocumentRepository.save(paymentDocument);
        fileService.createPaymentDocumentFolder(paymentDocument.getId());
        return paymentDocument.getId();
    }

    /**
     * @Description: Updates a paymentDocument
     * @param paymentDocId - used to find the doc
     * @param paymentDocument - the new document
     * @param request - used to find the current user
     * @return String
     */
    public String updatePaymentDocument(String paymentDocId, PaymentDocument paymentDocument, HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        if (paymentDocumentRepository.findById(paymentDocId).isEmpty()) {
            return "invalidId";
        }
        Optional<PaymentDocument> paymentDocument1 = paymentDocumentRepository.findById(paymentDocId);
        paymentDocument1.get().setAttachment(paymentDocument.getAttachment());
        paymentDocument1.get().setName(paymentDocument.getName());
        paymentDocument1.get().setPaymentMethod(paymentDocument.getPaymentMethod());
        paymentDocument1.get().setCurrency(paymentDocument.getCurrency());
        paymentDocument1.get().setValue(paymentDocument.getValue());
        paymentDocument1.get().setFiscalBillSeries(paymentDocument.getFiscalBillSeries());
        paymentDocument1.get().setFiscalBillNumber(paymentDocument.getFiscalBillNumber());
        paymentDocument1.get().setStatus(paymentDocument.getStatus());
        paymentDocument1.get().setPartnerId(paymentDocument.getPartnerId());
        paymentDocument1.get().setApplyTVA(paymentDocument.isApplyTVA());
        paymentDocument1.get().setLocalReferenceNumber(paymentDocument.getDocSeries() + " " + paymentDocument.getDocNumber());
        paymentDocument1.get().setLicenseNumber(paymentDocument.getLicenseNumber());
        paymentDocument1.get().setWarranty(paymentDocument.getWarranty());
        paymentDocument1.get().setProformaNumber(paymentDocument.getProformaNumber());
        paymentDocument1.get().setProformaSeries(paymentDocument.getProformaSeries());

        if (paymentDocument.getPartnerId() != null && partnerRepository.findById(paymentDocument.getPartnerId()).isPresent()){
            paymentDocument1.get().setPartnerName(partnerRepository.findById(paymentDocument.getPartnerId()).get().getName());
        }
        paymentDocumentRepository.save(paymentDocument1.get());
        return "success";
    }

    /**
     * @param partnerId - used to find the partner
     * @Description: updates the payment documents with partner name and partner CUI
     */
    public void updatePaymentDocumentPartnerNameAndCUI(String partnerId) {
        Optional<Partner> partner = partnerRepository.findById(partnerId);
        List<PaymentDocument> paymentDocuments = paymentDocumentRepository.findAllByPartnerId(partnerId);
        for (PaymentDocument paymentDocument1 : paymentDocuments ) {
            paymentDocument1.setPartnerName(partner.get().getName());
            paymentDocument1.setPartnerCUI(partner.get().getCUI());
            paymentDocumentRepository.save(paymentDocument1);
        }
    }
    /**
     * @param docId - used to find the document
     * @return Optional<PaymentDocument>
     * @Description: Returns a single doc by id
     */
    public Optional<PaymentDocument> getPaymentDocumentById(String docId) {
        return paymentDocumentRepository.findById(docId);
    }

    /**
     * @param docSeries - used to find the document by series
     * @param docNumber - used to find the document by number
     * @return Optional<PaymentDocument>
     * @Description: Returns a single doc by series and number
     */
    public Optional<PaymentDocument> getPaymentDocumentBySeriesAndNumber(String docSeries, Long docNumber) {
        return paymentDocumentRepository.findByDocSeriesAndDocNumber(docSeries,docNumber);
    }

    /**
     * @Description: Returns the maximum number for a payment document by office
     * @param office - the office
     * @return Long
     */
    public Long getMaxNumberByOffice(String office) {
        Optional<OfficeDTO> dto = settingService.getSettings().get().getUserOffices().stream().filter(officeDTO -> officeDTO.getCode().equalsIgnoreCase(office)).findFirst();
        return dto.map(officeDTO -> officeDTO.getNumber() + 1).orElse(1L);
    }

    /**
     * @Description: Changes fiscal bill link for a payment document
     * @param paymentDocId - used to find the payment document
     * @param link - the new link
     * @param request - used to find the current user
     * @return String
     */
    public String setFiscalBillLink(String paymentDocId, String link, HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        if (paymentDocumentRepository.findById(paymentDocId).isEmpty()) {
            return "invalidId";
        }
        Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(paymentDocId);
        paymentDocument.get().setFiscalBillSeries(link);
        paymentDocumentRepository.save(paymentDocument.get());
        return "success";
    }
    /**
     * @Description: Increments the doc number by office
     * @param office -  the office
     * @return Long
     */
    public Long incrementDocNumberByOffice(String office) {
        Optional<Setting> setting = settingService.getSettings();
        Optional<OfficeDTO> dto = setting.get().getUserOffices().stream().filter(officeDTO -> officeDTO.getCode().equalsIgnoreCase(office)).findFirst();
        dto.get().setNumber(dto.get().getNumber() + 1);
        settingRepository.save(setting.get());
        return dto.get().getNumber();
    }
    /**
     * @Description: Changes status for a payment document
     * @param docId - used to find the payment document
     * @param status - the new status
     * @param request - used to find the current user
     * @return String
     */
    public String changePaymentDocumentStatus(String docId, String status, HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        if (paymentDocumentRepository.findById(docId).isEmpty()) {
            return "invalidId";
        }
        Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(docId);
        Optional<User> currentUser = userService.getCurrentUser(request);
        paymentDocument.get().setStatus(status);
        paymentDocumentRepository.save(paymentDocument.get());
        return "success";
    }


    /**
     * @Description: Deletes a paymentDocument
     * @param docId - used to find the document
     * @param request - used to find the current user
     * @return String
     */
    public String deletePaymentDocument(String docId, HttpServletRequest request){
        if (userService.getCurrentUser(request).isEmpty()){
            return "youAreNotLoggedIn";
        }
        if (paymentDocumentRepository.findById(docId).isPresent()){
            paymentDocumentRepository.deleteById(docId);
            return "success";
        }else return "invalidId";
    }


}
