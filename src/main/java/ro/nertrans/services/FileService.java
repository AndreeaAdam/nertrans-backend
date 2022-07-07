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
import ro.nertrans.models.Setting;
import ro.nertrans.repositories.PaymentDocumentRepository;
import ro.nertrans.repositories.SettingRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;

@Service
public class FileService {
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;
    @Autowired
    private SettingService settingService;
    @Autowired
    private SettingRepository settingRepository;
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
    public void createSettingFolder() {
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            uploadDir.setWritable(true, true);
            uploadDir.setReadable(true, false);
            uploadDir.setExecutable(true, false);
        }
        /**
         * files/setting
         */
        File settingFolder = new File(uploadDirectory + File.separator + "setting");
        if (!settingFolder.exists()) {
            settingFolder.mkdirs();
            settingFolder.setWritable(true, true);
            settingFolder.setReadable(true, false);
            settingFolder.setExecutable(true, false);
        }
    }

//    public ResponseEntity<?> uploadDocAttachments(ArrayList<MultipartFile> files, HttpServletRequest request, String docId) {
//        if (userService.getCurrentUser(request).isEmpty()){
//            response = new ResponseEntity<>(new StringSuccessJSON(false, "youAreNotLoggedIn"), HttpStatus.BAD_REQUEST);
//        }
//        if (paymentDocumentRepository.findById(docId).isEmpty()){
//            response = new ResponseEntity<>(new StringSuccessJSON(false, "invalidId"), HttpStatus.BAD_REQUEST);
//        }
//
//        try {
//            for (MultipartFile file : files) {
//                //File extension limitations
//                if (!file.isEmpty()) {
//                    byte[] bytes = file.getBytes();
//                    Optional<User> currentUser = userService.getCurrentUser(request);
//                    Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(docId);
//                    File paymentDocumentLocation = new File(uploadDirectory + File.separator + "paymentDocuments" + File.separator + docId);
//                    String fileName = file.getOriginalFilename();
//                    File serverFile = new File(paymentDocumentLocation.getAbsolutePath() + File.separator + fileName);
//                    try {
//                        ArrayList<FileDTO> attachments = new ArrayList<>();
//                        if (paymentDocument.get().getAttachment()!= null) {
//                            attachments = paymentDocument.get().getAttachment();
//                        }
//                        // Writes the image
//                        BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
//                        stream.write(bytes);
//                        serverFile.setWritable(true, true);
//                        serverFile.setReadable(true, false);
//                        serverFile.setExecutable(true, false);
//                        stream.close();
//
//                        FileDTO dto = new FileDTO();
//                        dto.setFileName(serverFile.getName());
//                        dto.setFilePath(apiUrlPort + request.getContextPath() + File.separator + "files" + File.separator + "users" + File.separator + currentUser.get().getId() + File.separator + "brandIcons" + File.separator + fileName);
//
//                        attachments.add(dto);
//                        paymentDocument.get().setAttachment(attachments);
//                        paymentDocumentRepository.save(paymentDocument.get());
//                        response = new ResponseEntity<>(new StringSuccessJSON(true, fileName), HttpStatus.OK);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        response = new ResponseEntity<>(new StringSuccessJSON(false, "File Upload Failure.."), HttpStatus.BAD_REQUEST);
//                    }
//                } else {
//                    response = new ResponseEntity<>(new StringSuccessJSON(false, "File of wrong extension type or upload problem"), HttpStatus.BAD_REQUEST);
//                }
//            }
//        } catch (Exception e) {
//            response = new ResponseEntity<>(new StringSuccessJSON(false, "File Upload Failure.."), HttpStatus.BAD_REQUEST);
//        }
//        return response;
//    }

    public ResponseEntity<?> uploadDocAttachment(MultipartFile file, HttpServletRequest request, String docId) {

        try {
            if (!file.isEmpty()) {
            /*
            Check image
             */
                byte[] bytes = file.getBytes();
                Optional<PaymentDocument> document = paymentDocumentRepository.findById(docId);
                File paymentDocumentLocation = new File(uploadDirectory + File.separator + "paymentDocuments" + File.separator + docId);
                String fileName = file.getOriginalFilename();
                File serverFile = new File(paymentDocumentLocation.getAbsolutePath() + File.separator + fileName);
                try {
                    FileDTO dto = new FileDTO();
                    if (document.get().getAttachment() != null) {
                        dto = document.get().getAttachment();
                    }
                    //deletes the old image
                    File oldFileLocation = new File(uploadDirectory + File.separator + "paymentDocuments" + File.separator + docId + File.separator + dto.getFileName());
                    oldFileLocation.delete();
                    // Writes the image
                    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                    stream.write(bytes);
                    serverFile.setWritable(true, true);
                    serverFile.setReadable(true, false);
                    serverFile.setExecutable(true, false);
                    stream.close();
                    dto.setFileName(serverFile.getName());
                    dto.setFilePath(apiUrlPort + request.getContextPath() + File.separator + "files" + File.separator + "paymentDocuments" + File.separator + docId + File.separator + fileName);
                    document.get().setAttachment(dto);
                    paymentDocumentRepository.save(document.get());
                    return new ResponseEntity<>(new StringSuccessJSON(true, fileName), HttpStatus.OK);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(new StringSuccessJSON(false, "uploadFailure"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(new StringSuccessJSON(false, "fileCannotBeEmpty"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new StringSuccessJSON(false, "uploadFailure"), HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<?> uploadMobilPayKey(MultipartFile file, HttpServletRequest request, boolean isPublicKey) {

        try {
            if (!file.isEmpty()) {
            /*
            Check image
             */
                byte[] bytes = file.getBytes();
                Optional<Setting> setting = settingService.getSettings();
                File settingLocation = new File(uploadDirectory + File.separator + "setting");
                String fileName = file.getOriginalFilename();
                File serverFile = new File(settingLocation.getAbsolutePath() + File.separator + fileName);
                try {
                    FileDTO dto = new FileDTO();
                    if (isPublicKey) {
                        if (setting.get().getNetopiaPublicKey() != null) {
                            dto = setting.get().getNetopiaPublicKey();
                        }
                    } else {
                        if (setting.get().getNetopiaPrivateKey() != null) {
                            dto = setting.get().getNetopiaPrivateKey();
                        }
                    }
                    //deletes the old image
                    File oldFileLocation = new File(uploadDirectory + File.separator + "setting" + File.separator + dto.getFileName());
                    oldFileLocation.delete();
                    // Writes the image
                    BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
                    stream.write(bytes);
                    serverFile.setWritable(true, true);
                    serverFile.setReadable(true, false);
                    serverFile.setExecutable(true, false);
                    stream.close();
                    dto.setFileName(serverFile.getName());
                    dto.setFilePath(apiUrlPort + request.getContextPath() + File.separator + "files" + File.separator + "setting" + File.separator + fileName);
                    if (isPublicKey) {
                        setting.get().setNetopiaPublicKey(dto);
                    } else setting.get().setNetopiaPrivateKey(dto);
                    settingRepository.save(setting.get());
                    return new ResponseEntity<>(new StringSuccessJSON(true, fileName), HttpStatus.OK);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ResponseEntity<>(new StringSuccessJSON(false, "uploadFailure"), HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(new StringSuccessJSON(false, "fileCannotBeEmpty"), HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new StringSuccessJSON(false, "uploadFailure"), HttpStatus.BAD_REQUEST);
        }
    }
}
