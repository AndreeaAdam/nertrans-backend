package ro.nertrans.controllers;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.nertrans.JSON.StringSuccessJSON;
import ro.nertrans.dtos.MobilPayDTO;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.services.FileService;
import ro.nertrans.services.PaymentDocumentService;
import ro.nertrans.services.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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

    @RequestMapping(value = "/createPaymentDocument", method = RequestMethod.POST)
    @ApiResponse(description = "Creates a new payment document")
    public ResponseEntity<?> createPaymentDocument(@RequestBody PaymentDocument document,
                                                   HttpServletRequest request) {
        String response = paymentDocumentService.createPaymentDocument(request, document);
        if (response.equals(document.getId())) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.CREATED);
        } else
            return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/changePaymentDocumentStatus", method = RequestMethod.PUT)
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

    @RequestMapping(value = "/updatePaymentDocument", method = RequestMethod.PUT)
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

    @RequestMapping(value = "/setFiscalBillLink", method = RequestMethod.PUT)
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

    @RequestMapping(value = "/uploadDocAttachment", method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDocAttachment(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request,
                                                  @RequestParam("docId") String docId) {
        return fileService.uploadDocAttachment(file, request, docId);
    }

    @RequestMapping(value = "/getPaymentDocumentById", method = RequestMethod.GET)
    @ApiResponse(description = "Returns a single doc by id")
    public ResponseEntity<?> getPaymentDocumentById(@RequestParam(value = "docId") String docId) {
        return new ResponseEntity<>(paymentDocumentService.getPaymentDocumentById(docId), HttpStatus.OK);
    }

    @RequestMapping(value = "/getPaymentDocumentBySeriesAndNumber", method = RequestMethod.GET)
    @ApiResponse(description = "Returns a single doc by series and number")
    public ResponseEntity<?> getPaymentDocumentBySeriesAndNumber(@RequestParam(value = "docSeries") String docSeries, @RequestParam(value = "docNumber") Long docNumber) {
        return new ResponseEntity<>(paymentDocumentService.getPaymentDocumentBySeriesAndNumber(docSeries,docNumber), HttpStatus.OK);
    }

    @RequestMapping(value = "/getMaxNumberByOffice", method = RequestMethod.GET)
    @ApiResponse(description = "Returns the maximum number for a payment document by office")
    public ResponseEntity<?> getMaxNumberByOffice(@RequestParam(value = "office") String office) {
        return new ResponseEntity<>(paymentDocumentService.getMaxNumberByOffice(office), HttpStatus.OK);
    }
    @RequestMapping(value = "/getDocumentNumberByStatuses", method = RequestMethod.GET)
    @ApiResponse(description = "Returns the maximum number for a payment document by office")
    public ResponseEntity<?> getDocumentNumberByStatuses(@RequestBody List<String> statuses,
                                                         HttpServletRequest request) {
        if (userService.getCurrentUser(request).isEmpty()){
            return new ResponseEntity<>(new StringSuccessJSON(false, "notLoggedIn"), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(paymentDocumentService.getDocumentNumberByStatuses(statuses), HttpStatus.OK);
    }

    @RequestMapping(value = "/deletePaymentDocument", method = RequestMethod.DELETE)
    @ApiResponse(description = "Deletes permanently a payment document")
    public ResponseEntity<?> deletePaymentDocument(@RequestParam(value = "docId") String docId,
                                                   HttpServletRequest request) {
        String response = paymentDocumentService.deletePaymentDocument(docId, request);
        if (response.equalsIgnoreCase("success")) {
            return new ResponseEntity<>(new StringSuccessJSON(true, response), HttpStatus.OK);
        } else return new ResponseEntity<>(new StringSuccessJSON(false, response), HttpStatus.BAD_REQUEST);
    }



}
