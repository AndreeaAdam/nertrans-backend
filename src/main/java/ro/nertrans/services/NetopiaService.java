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
        Card req = new Card();

        req._signature = signature;
        req._confirmUrl = apiUrl + "/nertrans/cardConfirm";
        req._returnUrl = apiUrl + "/confirmare-comanda";
        req._orderId = dto.getOrderId();
        req._type = "card";
        Date date = new Date();
        String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        req._timestamp = Long.parseLong(dateString);

        Invoice inv = new Invoice();
        inv._amount = dto.getAmount();
        inv._currency = dto.getCurrency();
        inv._details = dto.getDetails();
        req._invoice = inv;
        ListItem listItem = req.encrypt(String.valueOf(publicCer));
        NetopiaResponseDTO responseDTO = new NetopiaResponseDTO();
        responseDTO.setKey(listItem.key);
        responseDTO.setVal(listItem.getVal());
        return responseDTO;
    }

    //    }
    public String cardConfirm(String env_key, String data) throws Exception {
        OpenSSL.extraInit();
        int errorCode = 0;
        String errorMessage = "";
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        URL confirmUrl = new URL(apiUrl + "/cardConfirm?" + "env_key=" + env_key + "&data=" + data);
        HttpURLConnection con = (HttpURLConnection) confirmUrl.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        if (con.getRequestMethod().equalsIgnoreCase("post")) {
            if (env_key == null || env_key.isEmpty()) return "env key null";
            if (data == null || data.isEmpty()) return "data null";
            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, privateKey.toString());
            String action = paymentRequest._objReqNotify._action;
            String orderId = paymentRequest._orderId;

            errorCode = paymentRequest._objReqNotify._errorCode;
            errorMessage = paymentRequest._objReqNotify._crc;
            Optional<PaymentDocument> paymentDocument = paymentDocumentRepository.findAll().stream().filter(paymentDocument1 -> (paymentDocument1.getDocSeries() + paymentDocument1.getDocNumber()).equalsIgnoreCase(orderId)).findFirst();
            if (errorCode != 0) {
                paymentDocument.get().setStatus("Plată respinsă");
            } else if (action.equalsIgnoreCase("confirmed")) {
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
        if (errorCode == 0) {
            return "<crc>" + errorMessage + "</crc>";
        } else {
            return "<crc error_type=\"" + errorType + "\" error_code=\"" + errorCode + "\">" + errorMessage + "</crc>";
        }
    }
}

