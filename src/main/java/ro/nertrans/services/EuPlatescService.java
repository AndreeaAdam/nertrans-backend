package ro.nertrans.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.nertrans.dtos.EuPlatescDTO;
import ro.nertrans.models.Transaction;
import ro.nertrans.repositories.TransactionRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;

@Service
public class EuPlatescService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private SettingService settingService;
    String host_base_url = "https://secure.euplatesc.ro/tdsprocess/tranzactd.php";
    String key = "118B7E71904A64CD5ADC2BE40A208995C41C29E7";
    String mid = "44840989660";

    public URI pay(EuPlatescDTO dto) throws IOException, InterruptedException {
        Date date = new Date();
        String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        dto.setNonce(nonceGen(32));
        dto.setMerchant_id(mid);
        dto.setTimestamp(dateString.trim());
        ArrayList s = new ArrayList();
        s.add(dto.getAmount());
        s.add(dto.getCurr());
        s.add(dto.getInvoice_id());
        s.add(dto.getOrder_desc());
        s.add(mid);
        s.add(dateString);
        String nonce = nonceGen(32);
        s.add(nonce);
        String fp_hash = fp_hash(s, key);
        s.add(fp_hash);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(host_base_url + "?" + "amount=" + dto.getAmount() + "&curr=" + dto.getCurr() + "&invoice_id=" + dto.getInvoice_id() + "&order_desc=" + dto.getOrder_desc() + "&merch_id=" + mid + "&timestamp=" + dateString + "&nonce=" + nonce + "&fp_hash=" + fp_hash))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        Transaction transaction = new Transaction();
        transaction.setId(null);
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurr());
        transaction.setDescription(dto.getOrder_desc());
        transaction.setInvoiceId(dto.getInvoice_id());
        transaction.setPaymentDocumentId(dto.getPaymentDocumentId());
        transaction.setTransactionDate(LocalDateTime.now());
        transactionRepository.save(transaction);
        return response.uri();
    }

    public void getResponse(double amount, String curr, Long invoice_id, String ep_id, String merch_id, int action, String message, String approval, String timestamp, String nonce, String fp_hash) {
        ArrayList s = new ArrayList();
        s.add(amount);
        s.add(curr);
        s.add(invoice_id);
        s.add(ep_id);
        s.add(merch_id);
        s.add(action);
        s.add(message);
        s.add(approval);
        s.add(timestamp);
        s.add(nonce);
        String calc_fp_hash = fp_hash(s, key);

        if (calc_fp_hash.equals(fp_hash)) {
            Transaction transaction = transactionRepository.getByInvoiceId(invoice_id);
            transaction.setStatus(approval);
            transactionRepository.save(transaction);
        }
    }

    public String nonceGen(int len) {
        String AlphaNumericString = "ABCDEF0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    public static byte[] hex2byte(String key) {
        int len = key.length();
        byte[] bkey = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bkey[i / 2] = (byte) ((Character.digit(key.charAt(i), 16) << 4) + Character.digit(key.charAt(i + 1), 16));
        }
        return bkey;
    }

    //RFC2104HMAC
    public static String fp_hash(ArrayList s, String key) {
        StringBuffer ret = new StringBuffer();
        Formatter formatter = new Formatter();
        String t;
        int l;
        for (Object o : s) {
            t = o.toString().trim();
            if (t.length() == 0)
                ret.append("-");
            else {
                l = t.length();
                ret.append(l);
                ret.append(t);
            }
        }
        String data = ret.toString();
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(hex2byte(key), "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(secretKeySpec);
            for (byte b : mac.doFinal(data.getBytes())) {
                formatter.format("%02x", b);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException ignored) {
        }
        return formatter.toString().toUpperCase();
    }

}
