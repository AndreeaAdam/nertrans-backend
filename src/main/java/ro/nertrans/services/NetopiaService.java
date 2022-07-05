package ro.nertrans.services;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.mobilPay.payment.Address;
import ro.mobilPay.payment.Invoice;
import ro.mobilPay.payment.request.Abstract;
import ro.mobilPay.payment.request.Card;
import ro.mobilPay.util.FileHelper;
import ro.mobilPay.util.ListItem;
import ro.mobilPay.util.MD5;
import ro.mobilPay.util.OpenSSL;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class NetopiaService {
    String host_base_url = "http://sandboxsecure.mobilpay.ro";
    @Value("${apiUrl}")
    private String apiUrl;
    public void pay() throws Exception {

        OpenSSL.extraInit();
        String signature = "Q3F5-2AE2-ESXJ-FLUJ-8WHK";
        String publicCerPath = "src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHK.public.cer";

        String publicCer = FileHelper.getFileContents(publicCerPath);
        Card req = new Card();

        req._signature = signature;
        req._confirmUrl = "http://45.86.220.233/nertrans/cardConfirm";
        req._returnUrl = "http://45.86.220.233";
        req._orderId = MD5.hash(""+Math.random());
        req._type = "card";
        Date date = new Date();
        String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        req._timestamp = Long.parseLong(dateString);

        Invoice inv = new Invoice();
        inv._amount = 1.99;
        inv._currency = "RON";
        inv._details = "Test payment through Java SDK";


        Address addr = new Address();
        addr._address = "Street, building, number, floor";
        addr._type = "person";
        addr._firstName = "John";
        addr._lastName = "Doe";
        addr._city = "MyCity";
        addr._email = "johndoe@johndoesmail.com";
        addr._country = "Country";
        addr._mobilePhone = "0987654321";
        addr._zipCode = "abc123";
        addr._county = "county";
        inv.setBillingAddress(addr);
        inv.setShippingAddress(addr);
        req._invoice = inv;
        ListItem li = req.encrypt(publicCer);
        sendPost(li.key, li.val);
    }
    private void sendPost(String key, String data) throws Exception {
        // form parameters
        RequestBody formBody = new FormBody.Builder()
                .add("env_key", key)
                .add("data", data)
                .build();

        Request request = new Request.Builder()
                .url(host_base_url)
                .addHeader("User-Agent", "OkHttp Bot")
                .post(formBody)
                .build();
        OkHttpClient httpClient = new OkHttpClient();
        try (Response response = httpClient.newCall(request).execute()) {
            System.out.println(response.isSuccessful());
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            System.out.println(response.body().string());
//            System.out.println(response.cacheResponse());
        }
    }


    public void cardConfirm(String env_key, String data) throws Exception {
        OpenSSL.extraInit();
//convert the private key to PEM openssl rsa -inform PEM -in private.key -out private-new.key
//        String privateKeyPath = getServletContext().getRealPath("WEB-INF/mobilpay-private.key");
        String privateKeyPath = "src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key";
        String privateKey = FileHelper.getFileContents(privateKeyPath);

        int errorCode = 0;
        String errorMessage = "";
        int errorType = Abstract.CONFIRM_ERROR_TYPE_NONE;

        URL url = new URL(apiUrl + "/cardConfirm?" + "env_key=" + env_key + "&data=" + data);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        System.out.println(con.getRequestMethod());
        if (con.getRequestMethod().equalsIgnoreCase("post")) {
            if (env_key == null || env_key.isEmpty())
                return;
            if (data == null || data.isEmpty())
                return;
            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, privateKey);
            String action = paymentRequest._objReqNotify._action;
            System.out.println("Got action=" + action + " for orderID=" + paymentRequest._orderId);
            System.out.println("and parameter is " + paymentRequest.__get("parameter"));
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
}

