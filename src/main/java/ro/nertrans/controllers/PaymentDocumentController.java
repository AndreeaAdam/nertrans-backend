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
import ro.nertrans.dtos.DocExportDTO;
import ro.nertrans.dtos.FileDTO;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.services.FileService;
import ro.nertrans.services.PaymentDocumentService;
import ro.nertrans.services.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class PaymentDocumentController {
    @Autowired
    private FileService fileService;
    @Autowired
    private PaymentDocumentService paymentDocumentService;
    @Autowired
    private UserService userService;

    @PostMapping(value = "/createPaymentDocument")
    @ApiResponse(description = "Creates a new payment document")
    public ResponseEntity<?> createPaymentDocument(@RequestBody PaymentDocument document,
                                                   HttpServletRequest request) {
        String response = paymentDocumentService.createPaymentDocument(request, document);
        if (response.equals(document.getId())) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.CREATED);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/changePaymentDocumentStatus")
    @ApiResponse(description = "Changes status for a payment document")
    public ResponseEntity<?> changePaymentDocumentStatus(@RequestParam(value = "status") String status,
                                                         @RequestParam(value = "docId") String docId,
                                                         HttpServletRequest request) {
        String response = paymentDocumentService.changePaymentDocumentStatus(docId, status, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/changePaymentDocumentOperationStatus")
    @ApiResponse(description = "Changes operation status for a payment document")
    public ResponseEntity<?> changePaymentDocumentOperationStatus(@RequestParam(value = "operationStatus") String operationStatus,
                                                         @RequestParam(value = "docId") String docId,
                                                         HttpServletRequest request) {
        String response = paymentDocumentService.changePaymentDocumentOperationStatus(docId, operationStatus, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/changePaymentDocumentLimitDate")
    @ApiResponse(description = "Changes limit date for a payment document")
    public ResponseEntity<?> changePaymentDocumentLimitDate(@RequestParam(value = "limitDate") String limitDate,
                                                         @RequestParam(value = "docId") String docId,
                                                         HttpServletRequest request) {
        String response = paymentDocumentService.changePaymentDocumentLimitDate(docId, limitDate, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/updatePaymentDocument")
    @ApiResponse(description = "Updates a payment document")
    public ResponseEntity<?> updatePaymentDocument(@RequestBody PaymentDocument paymentDocument,
                                                   @RequestParam(value = "paymentDocId") String paymentDocId,
                                                   HttpServletRequest request) {
        String response = paymentDocumentService.updatePaymentDocument(paymentDocId, paymentDocument, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/setFiscalBillLink")
    @ApiResponse(description = "Changes fiscal bill link")
    public ResponseEntity<?> setFiscalBillLink(@RequestParam(value = "link") String link,
                                               @RequestParam(value = "docId") String docId,
                                               HttpServletRequest request) {
        String response = paymentDocumentService.setFiscalBillLink(docId, link, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PutMapping(value = "/uploadDocAttachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocAttachment(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request,
                                                  @RequestParam("docId") String docId) {
        return fileService.uploadDocAttachment(file, request, docId);
    }

    @GetMapping(value = "/getPaymentDocumentById")
    @ApiResponse(description = "Returns a single doc by id")
    public ResponseEntity<?> getPaymentDocumentById(@RequestParam(value = "docId") String docId) {
        return new ResponseEntity<>(paymentDocumentService.getPaymentDocumentById(docId), HttpStatus.OK);
    }

    @GetMapping(value = "/getPaymentDocumentBySeriesAndNumber")
    @ApiResponse(description = "Returns a single doc by series and number")
    public ResponseEntity<?> getPaymentDocumentBySeriesAndNumber(@RequestParam(value = "docSeries") String docSeries, @RequestParam(value = "docNumber") Long docNumber) {
        return new ResponseEntity<>(paymentDocumentService.getPaymentDocumentBySeriesAndNumber(docSeries,docNumber), HttpStatus.OK);
    }

    @GetMapping(value = "/getMaxNumberByOffice")
    @ApiResponse(description = "Returns the maximum number for a payment document by office")
    public ResponseEntity<?> getMaxNumberByOffice(@RequestParam(value = "office") String office) {
        return new ResponseEntity<>(paymentDocumentService.getMaxNumberByOffice(office), HttpStatus.OK);
    }
    @PostMapping(value = "/getDocumentNumberByStatuses")
    @ApiResponse(description = "Returns the maximum number for a payment document by office")
    public ResponseEntity<?> getDocumentNumberByStatuses(@RequestBody List<String> statuses,
                                                         HttpServletRequest request,
                                                         @RequestParam("currentExpirationDate") boolean currentExpirationDate) {
        if (userService.getCurrentUser(request).isEmpty()){
            return new ResponseEntity<>(new StringSuccessJSON(false, "notLoggedIn"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(paymentDocumentService.getDocumentNumberByStatuses(statuses,currentExpirationDate, request), HttpStatus.OK);
    }
    @Secured({"ROLE_super_admin"})
    @DeleteMapping(value = "/deletePaymentDocument")
    @ApiResponse(description = "Deletes permanently a payment document")
    public ResponseEntity<?> deletePaymentDocument(@RequestParam(value = "docId") String docId,
                                                   HttpServletRequest request) {
        String response = paymentDocumentService.deletePaymentDocument(docId, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "/exportDocumentReport")
    public List<DocExportDTO> exportDocumentReport(@RequestParam(value = "startDate") String startDate,
                                                   @RequestParam(value = "endDate") String endDate,
                                                   HttpServletRequest request) {
       return paymentDocumentService.exportDocumentReport(startDate, endDate, request);
    }

    @PostMapping(value = "/exportDocumentReportXLS")
    public void exportDocumentReportXLS(HttpServletResponse response,
                                        @RequestParam("startDate") String startDate,
                                        @RequestParam("endDate") String endDate,
                                        HttpServletRequest request) {
        try {
            paymentDocumentService.exportDocumentReportXLS(response, request, startDate, endDate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
