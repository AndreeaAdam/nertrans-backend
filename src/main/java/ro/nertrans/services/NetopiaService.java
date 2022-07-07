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
import ro.nertrans.repositories.PaymentDocumentRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
        StringBuilder publicCer = new StringBuilder("");
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
        Card req = new Card();

        req._signature = signature;
        req._confirmUrl = "http://45.86.220.233/nertrans/cardConfirm";
        req._returnUrl = "http://45.86.220.233/autentificare";
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
        ListItem listItem = req.encrypt(String.valueOf(publicCer));
        NetopiaResponseDTO responseDTO = new NetopiaResponseDTO();
        responseDTO.setKey(listItem.key);
        responseDTO.setVal(listItem.getVal());
        return responseDTO;
    }

//    private void sendPost(String key, String data) throws Exception {
//        // form parameters
//        RequestBody formBody = new FormBody.Builder()
//                .add("env_key", key)
//                .add("data", data)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(host_base_url)
//                .addHeader("User-Agent", "OkHttp Bot")
//                .post(formBody)
//                .build();
//        OkHttpClient httpClient = new OkHttpClient();
//        try (Response response = httpClient.newCall(request).execute()) {
//            System.out.println(response.isSuccessful());
//            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//            System.out.println(response.request().url());
//            if (response.body() != null) {
//                System.out.println(response.body().string());
//            }
////            System.out.println(response.cacheResponse());
//        }
//    }


    public void cardConfirm(HttpServletRequest req) throws Exception {
        OpenSSL.extraInit();
        URL url = new URL(settingService.getSettings().get().getNetopiaPrivateKey().getFilePath());
//        String privateKey = ro.mobilPay.util.FileHelper.getFileContents(settingService.getSettings().get().getNetopiaPrivateKey().getFilePath());

        StringBuilder privateKey = new StringBuilder("");
        try {
            InputStreamReader isr = new InputStreamReader(url.openStream());
            BufferedReader brd = new BufferedReader(isr);
            List<String> lines = brd.lines().collect(Collectors.toList());
            for (String line : lines) {
                privateKey.append(line).append("\n");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        String errorMessage = "";
        if (req.getMethod().equalsIgnoreCase("post")) {
            String env_key = req.getParameter("env_key");
            String data = req.getParameter("data");
            if (env_key == null || env_key.isEmpty()) return;
            if (data == null || data.isEmpty()) return;
            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, String.valueOf(privateKey));
            String action = paymentRequest._objReqNotify._action;
            System.out.println("action" + action);
            errorMessage = paymentRequest._objReqNotify._crc;
            if (action.equalsIgnoreCase("confirmed")) {
                System.out.println("confirmed");
            } else if (action.equalsIgnoreCase("confirmed_pending")) {
                System.out.println("confirmed_pending");
            } else if (action.equalsIgnoreCase("paid_pending")) {
                System.out.println("paid_pending");
            } else if (action.equalsIgnoreCase("paid")) {
                System.out.println("paid");
            } else if (action.equalsIgnoreCase("canceled")) {
                System.out.println("canceled");
            } else if (action.equalsIgnoreCase("credit")) {
                System.out.println("credit");
            }
        }//end if is post
        System.out.print("<crc>" + errorMessage + "</crc>");
    }

//    }
//    public void cardConfirm(String env_key, String data) throws Exception {
//        OpenSSL.extraInit();
//        URL url = new URL(settingService.getSettings().get().getNetopiaPrivateKey().getFilePath());
////        URL url = new URL("http://45.86.220.233:3838/nertrans/files/setting/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key");
//        StringBuilder privateKey = new StringBuilder("");
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
//        URL confirmUrl = new URL(apiUrl + "/cardConfirm?" + "env_key=" + env_key + "&data=" + data);
//        HttpURLConnection con = (HttpURLConnection) confirmUrl.openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Content-Type", "application/json");
//        if (con.getRequestMethod().equalsIgnoreCase("post")) {
//            if (env_key == null || env_key.isEmpty()) return;
//            if (data == null || data.isEmpty()) return;
//            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, String.valueOf(privateKey));
//            String action = paymentRequest._objReqNotify._action;
//            System.out.println("action" + action);
//            errorMessage = paymentRequest._objReqNotify._crc;
//            if (action.equalsIgnoreCase("confirmed")) {
//                System.out.println("confirmed");
//            } else if (action.equalsIgnoreCase("confirmed_pending")) {
//                System.out.println("confirmed_pending");
//            } else if (action.equalsIgnoreCase("paid_pending")) {
//                System.out.println("paid_pending");
//            } else if (action.equalsIgnoreCase("paid")) {
//                System.out.println("paid");
//            } else if (action.equalsIgnoreCase("canceled")) {
//                System.out.println("canceled");
//            } else if (action.equalsIgnoreCase("credit")) {
//                System.out.println("credit");
//            }
//        }//end if is post
//        System.out.print("<crc>" + errorMessage + "</crc>");
//    }
}

