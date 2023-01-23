package ro.nertrans.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.nertrans.config.UserRoleEnum;
import ro.nertrans.dtos.DocExportDTO;
import ro.nertrans.dtos.OfficeDTO;
import ro.nertrans.dtos.OperationStatusDTO;
import ro.nertrans.models.Partner;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.models.Setting;
import ro.nertrans.models.User;
import ro.nertrans.repositories.PartnerRepository;
import ro.nertrans.repositories.PaymentDocumentRepository;
import ro.nertrans.repositories.SettingRepository;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Value("${server.servlet.contextPath}")
    private String contextPath;
    @Value("${apiUrl}")
    private String apiUrl;

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
                document.setBillableProductName(paymentDocument.getBillableProductName());
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
        Optional<User> user = userService.getCurrentUser(request);
        if (user.isEmpty()) {
            return "youAreNotLoggedIn";
        }
        Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findById(docId);
        if (paymentDocument.isPresent()) {
            if ((operationStatus.equalsIgnoreCase("DGV") || paymentDocument.get().getOperationStatus().equalsIgnoreCase("DGV")) && !user.get().isAdminDgv()) {
                return "notAllowed";
            }
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
    public List<OperationStatusDTO> getDocumentNumberByStatuses(List<String> statuses, boolean currentExpirationDate, HttpServletRequest request) {
        Optional<User> user = userService.getCurrentUser(request);
        List<OperationStatusDTO> statusDTOS = new ArrayList<>();
        for (String status : statuses) {
            OperationStatusDTO dto;
            if (!currentExpirationDate) {
                if (user.get().getRoles().contains(UserRoleEnum.ROLE_super_admin) || user.get().getRoles().contains(UserRoleEnum.ROLE_admin)) {
                    dto = new OperationStatusDTO(status, paymentDocumentRepository.findByOperationStatusIgnoreCase(status).size());
                } else
                    dto = new OperationStatusDTO(status, paymentDocumentRepository.findByOperationStatusIgnoreCase(status).stream().filter(paymentDocument -> user.get().getOffice().contains(paymentDocument.getDocSeries())).count());
            } else {
                if (user.get().getRoles().contains(UserRoleEnum.ROLE_super_admin) || user.get().getRoles().contains(UserRoleEnum.ROLE_admin)) {
                    dto = new OperationStatusDTO(status, (int) paymentDocumentRepository.findByOperationStatusIgnoreCase(status).stream().filter(
                            paymentDocument -> paymentDocument.getExpirationDate() != null && (paymentDocument.getExpirationDate().isBefore(LocalDate.now()) || paymentDocument.getExpirationDate().equals(LocalDate.now()))).count());
                } else
                    dto = new OperationStatusDTO(status, (int) paymentDocumentRepository.findByOperationStatusIgnoreCase(status).stream().filter(
                            paymentDocument -> paymentDocument.getExpirationDate() != null && (paymentDocument.getExpirationDate().isBefore(LocalDate.now()) || paymentDocument.getExpirationDate().equals(LocalDate.now())) && user.get().getOffice().contains(paymentDocument.getDocSeries())).count());
            }
            statusDTOS.add(dto);
        }
        return statusDTOS;
    }

    public List<DocExportDTO> exportDocumentReport(String startDate, String endDate, HttpServletRequest request) {
        List<DocExportDTO> docs = new ArrayList<>();
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("ro"));
        Stream<LocalDate> dates = LocalDate.parse(startDate).datesUntil(LocalDate.parse(endDate).plusDays(1));
        List<LocalDate> localDates = dates.sorted().collect(Collectors.toList());
        Set<String> uniqueDates = new HashSet<>();
        localDates.forEach(localDate -> uniqueDates.add(formatter2.format(localDate)));
        List<String> finalDates;
        try {
            finalDates = sortDates(uniqueDates);
            Collections.reverse(finalDates);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        for (String date : finalDates) {
            List<PaymentDocument> paymentDocuments = paymentDocumentRepository.findAll().stream().filter(paymentDocument -> formatter2.format(paymentDocument.getDate()).equals(date)).collect(Collectors.toList());
            DocExportDTO dto = new DocExportDTO();
            dto.setDate(date.substring(0, 1).toUpperCase() + date.substring(1));
            dto.setDocNumbers(paymentDocuments.size());
            dto.setTotalEuro(paymentDocuments.stream().mapToDouble(PaymentDocument::getGoodsValue).sum());
            dto.setRelatedWarranty(paymentDocuments.stream().mapToDouble(PaymentDocument::getWarranty).sum());
            docs.add(dto);
        }
        return docs;
    }
    public void exportDocumentReportXLS(HttpServletResponse response,HttpServletRequest request,String startDate, String endDate) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        // creating sheet with name "Report" in workbook
        XSSFSheet sheet = workbook.createSheet("Documents");
        // this method creates header for our table
        XSSFCellStyle textStyle = workbook.createCellStyle();
        textStyle.setAlignment(HorizontalAlignment.CENTER);
        textStyle.setWrapText(true);
        sheet.createRow(0).createCell(1);
        sheet.createRow(0).createCell(3);
        sheet.createRow(4).createCell(1);
        sheet.createRow(4).createCell(3);
        addImageToCell(workbook,sheet, 0, 1, 4, 3,apiUrl + contextPath + File.separator + "logo-nertrans.png");
        sheet.createRow(5);
        sheet.getRow(5).setHeight((short) 1000);
        for (int i = 1; i <= 6; i++) {
            sheet.getRow(5).createCell(i);
            sheet.getRow(5).getCell(i).setCellStyle(textStyle);
        }
        sheet.getRow(5).getCell(1)
        .setCellValue("Situația operațiunilor de tranzit unional/comun efectuat în cele "+ exportDocumentReport(startDate, endDate, request).size() + " luni precedente," +
                " prevăzute în art. 12 alin. (2) lit. h) din Normele tehnice privind autorizarea emiterii de titluri de garanție izolată, utilizare a garanției globale și a exonerării de garanție în cadrul regimului de tranzit" +
                " unional/comun, aprobate prin Ordinul președintelui Agenției Naționale de Administrare Fiscală nr. 1889/2016");
        sheet.getRow(5).createCell(6);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, 1, 6));
        createHeader(sheet, workbook);
        int rowCount = 8;
        for (DocExportDTO doc : exportDocumentReport(startDate, endDate, request)){
            int cell = 1;
            // creating row
            Row row = sheet.createRow(rowCount++);
            row.createCell(cell++).setCellValue(doc.getDate());
            row.createCell(cell++).setCellValue(doc.getDocNumbers());
            row.createCell(cell++).setCellValue(doc.getTotalEuro());
            row.createCell(cell++).setCellValue(25);
            row.createCell(cell++).setCellValue(doc.getRelatedWarranty());
            row.createCell(cell).setCellValue("");
            for (int i = 1; i <= 6; i++) {
                row.getCell(i).setCellStyle(borderStyle(workbook));
                if (i == 3 || i == 5 || i == 6){
                    DataFormat format = workbook.createDataFormat();
                    row.getCell(i).getCellStyle().setDataFormat(format.getFormat("###.##0"));
                }
            }
        }
        sheet.createRow(rowCount);
        for (int i = 1; i <= 6; i++) {
            sheet.getRow(rowCount).createCell(i);
            sheet.getRow(rowCount).getCell(i).setCellStyle(borderStyle(workbook));
        }
        sheet.getRow(rowCount).getCell(1).setCellValue("Total");
        sheet.getRow(rowCount).getCell(2).setCellFormula("SUM(" + sheet.getRow(7).getCell(2).getAddress() + ":" + sheet.getRow(rowCount-1).getCell(2).getAddress() + ")");
        sheet.getRow(rowCount).getCell(3).setCellFormula("SUM(" + sheet.getRow(7).getCell(3).getAddress() + ":" + sheet.getRow(rowCount-1).getCell(3).getAddress()+ ")");
        sheet.getRow(rowCount).getCell(5).setCellFormula("SUM(" + sheet.getRow(7).getCell(5).getAddress() + ":" + sheet.getRow(rowCount-1).getCell(5).getAddress()+ ")");
        sheet.getRow(rowCount).getCell(6).setCellFormula("SUM(" + sheet.getRow(7).getCell(6).getAddress() + ":" + sheet.getRow(rowCount-1).getCell(6).getAddress()+ ")");
        for (int i = 1; i <= 6; i++) {
            if (i != 4){
                DataFormat format = workbook.createDataFormat();
                sheet.getRow(rowCount).getCell(i).getCellStyle().setDataFormat(format.getFormat("###.##0"));
            }
        }
        rowCount++;
        sheet.createRow(rowCount);
        for (int i = 1; i <= 6; i++) {
            sheet.getRow(rowCount).createCell(i);
            XSSFCellStyle style = borderStyle(workbook);
            style.setAlignment(HorizontalAlignment.LEFT);
            sheet.getRow(rowCount).getCell(i).setCellStyle(style);
        }
        sheet.getRow(rowCount).getCell(1).setCellValue("Valoarea de referință propusă");
        sheet.addMergedRegion(new CellRangeAddress(rowCount, rowCount++, 1, 6));
        sheet.createRow(++rowCount).createCell(1).setCellValue("Nertrans Cargo SRL - Administrator,");
        sheet.getRow(rowCount++).createCell(5).setCellValue("Întocmit,");

        sheet.createRow(rowCount).createCell(1).setCellValue("Iulian Cirlan");
        sheet.getRow(rowCount).createCell(5).setCellValue("Nicoleta Leopa");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("content-disposition", "attachment; filename=Documents - " + LocalDate.now() + ".xlsx");
        workbook.write(response.getOutputStream());
    }

    private void createHeader(XSSFSheet sheet, XSSFWorkbook workbook) {
        Row headerRow = sheet.createRow(7);
        int cell = 1;

        headerRow.createCell(cell++).setCellValue("Luna");
        headerRow.createCell(cell++).setCellValue("Numărul de operațiuni");
        headerRow.createCell(cell++).setCellValue("Valoarea mărfurilor tranzitate în euro");
        headerRow.createCell(cell++).setCellValue("Nivelul taxei vamale cel mai ridicat (%)");
        headerRow.createCell(cell++).setCellValue("Garanția aferentă mărfurilor tranzitate în lei");
        headerRow.createCell(cell).setCellValue("Suma taxelor vamale, TVA și accize pentru săptămâna cea mai reprezentativă (lei)");
        for (int i = 1; i <= 6; i++) {
            headerRow.getCell(i).setCellStyle(borderStyle(workbook));
        }
        sheet.setColumnWidth(1, 5000);
        sheet.setColumnWidth(2, 4000);
        sheet.setColumnWidth(3, 5000);
        sheet.setColumnWidth(4, 5000);
        sheet.setColumnWidth(5, 5000);
        sheet.setColumnWidth(6, 7500);
    }
    public XSSFCellStyle borderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderTop(BorderStyle.MEDIUM);
        borderStyle.setBorderBottom(BorderStyle.MEDIUM);
        borderStyle.setBorderLeft(BorderStyle.MEDIUM);
        borderStyle.setBorderRight(BorderStyle.MEDIUM);
        borderStyle.setBorderRight(BorderStyle.MEDIUM);
        borderStyle.setAlignment(HorizontalAlignment.CENTER);
        borderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        borderStyle.setWrapText(true);
        return borderStyle;
    }
    public void addImageToCell(XSSFWorkbook workbook, XSSFSheet sheet, int firstRow, int firstCell, int lastRow, int lastCell, String path) throws IOException {
        URL url = new URL(path);
        InputStream is = url.openStream();
        BufferedImage image = ImageIO.read(is);
        ByteArrayOutputStream baps = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baps);

        int pictureIdx = workbook.addPicture(baps.toByteArray(), Workbook.PICTURE_TYPE_PNG);
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFCreationHelper helper = workbook.getCreationHelper();
        XSSFClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(firstCell);
        anchor.setRow1(firstRow);
        anchor.setCol2(lastCell);
        anchor.setRow2(lastRow);
        Picture picture = drawing.createPicture(anchor, pictureIdx);
        picture.getImageDimension().setSize(4, 4);
    }
    private List<String> sortDates(Set<String> dates) throws ParseException {
        SimpleDateFormat f = new SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("ro"));
        Map <Date, String> dateFormatMap = new TreeMap<>();
        for (String date: dates)
            dateFormatMap.put(f.parse(date), date);
        return new ArrayList<>(dateFormatMap.values());
    }
}
