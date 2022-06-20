package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ro.nertrans.JSON.StringSuccessJSON;
import ro.nertrans.dtos.FileDTO;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PaymentDocumentRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class FileService {
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;
    @Value("${UPLOAD_DIRECTORY}")
    private String uploadDirectory;
    @Value("${apiUrlPort}")
    private String apiUrlPort;

    ResponseEntity<?> response;

    public void createPaymentDocumentFolder(String docId) {
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            uploadDir.setWritable(true, true);
            uploadDir.setReadable(true, false);
            uploadDir.setExecutable(true, false);
        }
        /**
         * files/paymentDocuments/{docId}
         */
        File paymentDocFolder = new File(uploadDirectory + File.separator + "paymentDocuments" + File.separator + docId);
        if (!paymentDocFolder.exists()) {
            paymentDocFolder.mkdirs();
            paymentDocFolder.setWritable(true, true);
            paymentDocFolder.setReadable(true, false);
            paymentDocFolder.setExecutable(true, false);
        }
    }


    /**
     * @Description: Uploads an array of brand icons for a specific paymentDoc
     * @param files - the actual files being uploaded
     * @param request - used to find out the current user
     * @return false + Brand Icons File Upload Failure.
     */
    public ResponseEntity<?> uploadDocAttachments(ArrayList<MultipartFile> files, HttpServletRequest request, String docId) {
        if (userService.getCurrentUser(request).isEmpty()){
            response = new ResponseEntity<>(new StringSuccessJSON(false, "youAreNotLoggedIn"), HttpStatus.BAD_REQUEST);
        }
        if (paymentDocumentRepository.findById(docId).isEmpty()){
            response = new ResponseEntity<>(new StringSuccessJSON(false, "invalidId"), HttpStatus.BAD_REQUEST);
        }

        try {
            for (MultipartFile file : files) {
                //File extension limitations
                if (!file.isEmpty()) {
                    byte[] bytes = file.getBytes();
                    Optional<User> currentUser = userService.getCurrentUser(request);
                    Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(docId);
                    File paymentDocumentLocation = new File(uploadDirectory + File.separator + "paymentDocuments" + File.separator + docId);
                    String fileName = file.getOriginalFilename();
                    File serverFile = new File(paymentDocumentLocation.getAbsolutePath() + File.separator + fileName);
                    try {
                        ArrayList<FileDTO> attachments = new ArrayList<>();
                        if (paymentDocument.get().getAttachments()!= null) {
                            attachments = paymentDocument.get().getAttachments();
                        }
                        // Writes the image
                        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                        stream.write(bytes);
                        serverFile.setWritable(true, true);
                        serverFile.setReadable(true, false);
                        serverFile.setExecutable(true, false);
                        stream.close();

                        FileDTO dto = new FileDTO();
                        dto.setFileName(serverFile.getName());
                        dto.setFilePath(apiUrlPort + request.getContextPath() + File.separator + "files" + File.separator + "users" + File.separator + currentUser.get().getId() + File.separator + "brandIcons" + File.separator + fileName);

                        attachments.add(dto);
                        paymentDocument.get().setAttachments(attachments);
                        paymentDocumentRepository.save(paymentDocument.get());
                        response = new ResponseEntity<>(new StringSuccessJSON(true, fileName), HttpStatus.OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                        response = new ResponseEntity<>(new StringSuccessJSON(false, "File Upload Failure.."), HttpStatus.BAD_REQUEST);
                    }
                } else {
                    response = new ResponseEntity<>(new StringSuccessJSON(false, "File of wrong extension type or upload problem"), HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception e) {
            response = new ResponseEntity<>(new StringSuccessJSON(false, "File Upload Failure.."), HttpStatus.BAD_REQUEST);
        }
        return response;
    }
}
