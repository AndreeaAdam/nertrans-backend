package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.mobilPay.payment.Invoice;
import ro.mobilPay.payment.request.Abstract;
import ro.mobilPay.payment.request.Card;
import ro.mobilPay.util.ListItem;
import ro.mobilPay.util.OpenSSL;
import ro.nertrans.dtos.MobilPayDTO;
import ro.nertrans.dtos.NetopiaResponseDTO;
import ro.nertrans.models.PaymentDocument;
import ro.nertrans.repositories.PaymentDocumentRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NetopiaService {
    String host_base_url = "http://sandboxsecure.mobilpay.ro";
    @Value("${apiUrl}")
    private String apiUrl;
    @Autowired
    private PaymentDocumentRepository paymentDocumentRepository;
    @Autowired
    private SettingService settingService;

    public NetopiaResponseDTO initPayment(MobilPayDTO dto) throws Exception {
        OpenSSL.extraInit();
        String signature = settingService.getSettings().get().getNetopiaSignature();
        URL url = new URL(settingService.getSettings().get().getNetopiaPublicKey().getFilePath());
        //String signature = "Q3F5-2AE2-ESXJ-FLUJ-8WHK";
        //URL url = new URL("https://nertrans.eu:3838/nertrans/files/setting/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHK.public.cer");
        StringBuilder publicCer = new StringBuilder();
        try {
            InputStreamReader isr = new InputStreamReader(url.openStream());
            BufferedReader brd = new BufferedReader(isr);
            List<String> lines = brd.lines().collect(Collectors.toList());
            for (String line : lines) {
                publicCer.append(line).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("signature " + signature);
        System.out.println("amount " + dto.getAmount());
        Card req = new Card();

        req._signature = signature;
        req._confirmUrl = apiUrl +"/nertrans/cardConfirm";
        req._returnUrl = apiUrl +"/confirmare-comanda";
        req._orderId = dto.getOrderId();
        req._type = "card";
        Date date = new Date();
        String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        req._timestamp = Long.parseLong(dateString);

        Invoice inv = new Invoice();
        inv._amount = dto.getAmount();
        inv._currency = dto.getCurrency();
        inv._details = dto.getDetails();

//
//        Address addr = new Address();
//        addr._address = "Street, building, number, floor";
//        addr._type = "person";
//        addr._firstName = "John";
//        addr._lastName = "Doe";
//        addr._city = "MyCity";
//        addr._email = "johndoe@johndoesmail.com";
//        addr._country = "Country";
//        addr._mobilePhone = "0987654321";
//        addr._zipCode = "abc123";
//        addr._county = "county";
//        inv.setBillingAddress(addr);
//        inv.setShippingAddress(addr);
        req._invoice = inv;
        System.out.println( "public cert" + publicCer);
        ListItem listItem = req.encrypt(String.valueOf(publicCer));
        NetopiaResponseDTO responseDTO = new NetopiaResponseDTO();
        responseDTO.setKey(listItem.key);
        responseDTO.setVal(listItem.getVal());
        return responseDTO;
    }
//    public void cardConfirm(HttpServletRequest req) throws Exception {
//        OpenSSL.extraInit();
//        URL url = new URL(settingService.getSettings().get().getNetopiaPrivateKey().getFilePath());
//        StringBuilder privateKey = new StringBuilder();
//        try {
//            InputStreamReader isr = new InputStreamReader(url.openStream());
//            BufferedReader brd = new BufferedReader(isr);
//            List<String> lines = brd.lines().collect(Collectors.toList());
//            for (String line : lines) {
//                privateKey.append(line).append("\n");
//            }
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        String errorMessage = "";
//        if (req.getMethod().equalsIgnoreCase("post")) {
//            String env_key = req.getParameter("env_key");
//            String data = req.getParameter("data");
//            if (env_key == null || env_key.isEmpty()) return;
//            if (data == null || data.isEmpty()) return;
//            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, privateKey.toString());
//            System.out.println(paymentRequest._orderId);
//            String action = paymentRequest._objReqNotify._action;
//            errorMessage = paymentRequest._objReqNotify._crc;
//            Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findAll().stream().filter(paymentDocument1 -> (paymentDocument1.getDocSeries() + paymentDocument1.getDocNumber()).equalsIgnoreCase(paymentRequest._orderId)).findFirst();
//            if (action.equalsIgnoreCase("confirmed")) {
//                System.out.println("confirmed");
//                paymentDocument.get().setStatus("Confirmed");
//            } else if (action.equalsIgnoreCase("confirmed_pending")) {
//                paymentDocument.get().setStatus("Confirmed pending");
//                System.out.println("confirmed_pending");
//            } else if (action.equalsIgnoreCase("paid_pending")) {
//                paymentDocument.get().setStatus("Paid pending");
//                System.out.println("paid_pending");
//            } else if (action.equalsIgnoreCase("paid")) {
//                paymentDocument.get().setStatus("Paid");
//                System.out.println("paid");
//            } else if (action.equalsIgnoreCase("canceled")) {
//                paymentDocument.get().setStatus("Canceled");
//                System.out.println("canceled");
//            } else if (action.equalsIgnoreCase("credit")) {
//                paymentDocument.get().setStatus("Credit");
//                System.out.println("credit");
//            }
//            paymentDocumentRepository.save(paymentDocument.get());
//        }//end if is post
//        System.out.print("<crc>" + errorMessage + "</crc>");
//    }

    //    }
    public String cardConfirm(String env_key, String data) throws Exception {
        //System.out.println("TEST///////////////////////////1cardconfirm///////////////////////////////TEST");
        OpenSSL.extraInit();
        int errorCode = 0;
        String errorMessage = "";
        //String errorMessage2 = "";
        int errorType = ro.mobilPay.payment.request.Abstract.CONFIRM_ERROR_TYPE_NONE;
        URL url = new URL(settingService.getSettings().get().getNetopiaPrivateKey().getFilePath());
        StringBuilder privateKey = new StringBuilder("");
        try {
            InputStreamReader isr = new InputStreamReader(url.openStream());
            BufferedReader brd = new BufferedReader(isr);
            List<String> lines = brd.lines().collect(Collectors.toList());
            for (String line : lines) {
                privateKey.append(line).append("\n");
            }
            //System.out.println("TEST///////////////////////////2cardconfirm TRY///////////////////////////////TEST");
            //System.out.println(errorType+" "+ errorCode +" "+ errorMessage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        URL confirmUrl = new URL(apiUrl + "/cardConfirm?" + "env_key=" + env_key + "&data=" + data);
        HttpURLConnection con = (HttpURLConnection) confirmUrl.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        //System.out.println("TEST///////////////////////////2cardconfirm beforeIF///////////////////////////////TEST");
        //System.out.println(errorType+" "+ errorCode +" "+ errorMessage);
        if (con.getRequestMethod().equalsIgnoreCase("post")) {
            if (env_key == null || env_key.isEmpty()) return "env key null";
            if (data == null || data.isEmpty()) return "data null";
            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, privateKey.toString());
            String action = paymentRequest._objReqNotify._action;
            String orderId = paymentRequest._orderId;

            errorCode = paymentRequest._objReqNotify._errorCode;
            errorMessage = paymentRequest._objReqNotify._crc;
            //errorMessage2 = paymentRequest._objReqNotify._errorMessage;
            //System.out.println(errorType+" "+ errorCode +" "+ errorMessage);
            Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findAll().stream().filter(paymentDocument1 -> (paymentDocument1.getDocSeries() + paymentDocument1.getDocNumber()).equalsIgnoreCase(orderId)).findFirst();
            if (action.equalsIgnoreCase("confirmed")) {
                paymentDocument.get().setStatus("Plătită");
            } else if (action.equalsIgnoreCase("confirmed_pending")) {
                paymentDocument.get().setStatus("Plată în verificare");
            } else if (action.equalsIgnoreCase("paid_pending")) {
                paymentDocument.get().setStatus("Plată în verificare");
            } else if (action.equalsIgnoreCase("paid")) {
                paymentDocument.get().setStatus("Plată în verificare");
            } else if (action.equalsIgnoreCase("canceled")) {
                paymentDocument.get().setStatus("Plată respinsă");
            } else if (action.equalsIgnoreCase("credit")) {
                paymentDocument.get().setStatus("Plată returnată");
            }
            paymentDocumentRepository.save(paymentDocument.get());
        }
        //System.out.println("TEST///////////////////////////2cardconfirm IF///////////////////////////////TEST");
        //System.out.println(errorType+" "+ errorCode +" "+ errorMessage);
        if(errorCode == 0)
        {
            return "<crc>"+errorMessage+"</crc>";
        }
        else
        {
            return "<crc error_type=\""+errorType+"\" error_code=\""+errorCode+"\">"+errorMessage+"</crc>";
        }
    }
}

