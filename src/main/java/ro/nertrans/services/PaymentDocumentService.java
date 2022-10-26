package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.nertrans.dtos.OfficeDTO;
import ro.nertrans.dtos.OperationStatusDTO;
import ro.nertrans.models.Partner;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.Setting;
import ro.nertrans.repositories.PartnerRepository;
import ro.nertrans.repositories.PaymentDocumentRepository;
import ro.nertrans.repositories.SettingRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        if (paymentDocumentRepository.findById(paymentDocId).isPresent()) {
            Optional<PaymentDocument> paymentDocument1 = paymentDocumentRepository.findById(paymentDocId);
            if (paymentDocument1.isPresent()){
                PaymentDocument document = paymentDocument1.get();
                document.setAttachment(paymentDocument.getAttachment());
                document.setName(paymentDocument.getName());
                document.setPaymentMethod(paymentDocument.getPaymentMethod());
                document.setCurrency(paymentDocument.getCurrency());
                document.setGoodsValue(paymentDocument.getGoodsValue());
                document.setValue(paymentDocument.getValue());
                document.setFiscalBillSeries(paymentDocument.getFiscalBillSeries());
                document.setFiscalBillNumber(paymentDocument.getFiscalBillNumber());
                document.setStatus(paymentDocument.getStatus());
                document.setPartnerId(paymentDocument.getPartnerId());
                document.setApplyTVA(paymentDocument.isApplyTVA());
                document.setLocalReferenceNumber(paymentDocument.getDocSeries() + " " + paymentDocument.getDocNumber());
                document.setLicenseNumber(paymentDocument.getLicenseNumber());
                document.setWarranty(paymentDocument.getWarranty());
                document.setProformaNumber(paymentDocument.getProformaNumber());
                document.setProformaSeries(paymentDocument.getProformaSeries());
                document.setExpirationDate(paymentDocument.getExpirationDate());
                if (paymentDocument.getPartnerId() != null && partnerRepository.findById(paymentDocument.getPartnerId()).isPresent()){
                    paymentDocument1.get().setPartnerName(partnerRepository.findById(paymentDocument.getPartnerId()).get().getName());
                }
                paymentDocumentRepository.save(paymentDocument1.get());
                return "success";
            }
        }
       return "invalidId";
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
        paymentDocument.get().setStatus(status);
        paymentDocumentRepository.save(paymentDocument.get());
        return "success";
    }
    /**
     * @Description: Changes operation status for a payment document
     * @param docId - used to find the payment document
     * @param operationStatus - the new  operation status
     * @param request - used to find the current user
     * @return String
     */
    public String changePaymentDocumentOperationStatus(String docId, String operationStatus, HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(docId);
        if (paymentDocument.isPresent()) {
            paymentDocument.get().setOperationStatus(operationStatus);
            paymentDocumentRepository.save(paymentDocument.get());
            return "success";
        }
        return "invalidId";
    }

    public String changePaymentDocumentLimitDate(String docId, String limitDate, HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()) {
            return "youAreNotLoggedIn";
        }
        Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(docId);
        if (paymentDocument.isPresent()) {
            paymentDocument.get().setExpirationDate(LocalDate.parse(limitDate));
            paymentDocumentRepository.save(paymentDocument.get());
            return "success";
        }
        return "invalidId";
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

    /**
     * @Description: Returns a list with documents number by status
     * @param statuses - status's list
     * @return List<OperationStatusDTO>
     */
    public List<OperationStatusDTO> getDocumentNumberByStatuses(List<String> statuses){
       List<OperationStatusDTO> statusDTOS = new ArrayList<>();
       for (String status: statuses){
           OperationStatusDTO dto = new OperationStatusDTO(status, paymentDocumentRepository.findByOperationStatusIgnoreCase(status).size());
           statusDTOS.add(dto);
       }
       return statusDTOS;
    }
}
