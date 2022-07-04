package ro.nertrans.services;

import org.springframework.stereotype.Service;
import ro.mobilPay.payment.request.Abstract;
import ro.nertrans.dtos.EuPlatescDTO;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class NetopiaService {
    String host_base_url = "http://sandboxsecure.mobilpay.ro";

    public void pay(EuPlatescDTO dto) throws Exception {

        ro.mobilPay.util.OpenSSL.extraInit();
        String signature = "Q3F5-2AE2-ESXJ-FLUJ-8WHK";

//        String publicCerPath = getServletContext().getRealPath("resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHK.public.cer");
        String publicCerPath = "src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHK.public.cer";
        String paymentURL = "http://sandboxsecure.mobilpay.ro";

        String publicCer = ro.mobilPay.util.FileHelper.getFileContents(publicCerPath);
        ro.mobilPay.payment.request.Card req = new ro.mobilPay.payment.request.Card();

        req._signature = signature;
        req._confirmUrl = "http://45.86.220.233:3838/nertrans/cardConfirm";
        req._returnUrl = "http://localhost:8080/mobilPayJ/cardReturn.jsp";
        req._orderId = ro.mobilPay.util.MD5.hash(""+Math.random());
        req._type = "card";
        Date date = new Date();
        String dateString = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
        req._timestamp = Long.parseLong(dateString);

        ro.mobilPay.payment.Invoice inv = new ro.mobilPay.payment.Invoice();
        inv._amount = 1.99;
        inv._currency = "RON";
        inv._details = "Test payment through Java SDK";


        ro.mobilPay.payment.Address addr = new ro.mobilPay.payment.Address();
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
//        req.__set("parameter", "valueofparameter");
//        System.out.println("invoice" + req._invoice);
//        System.out.println("_confirmUrl" + req._confirmUrl);
//        System.out.println("_returnUrl" + req._returnUrl);
//        System.out.println("_orderId" + req._orderId);
//        System.out.println("_signature" + req._signature);
//        System.out.println("_cancelUrl" + req._cancelUrl);
//        System.out.println("_service" + req._service);
//        System.out.println("_timestamp" + req._timestamp);
//        System.out.println("_type" + req._type);
//        System.out.println("_objRequestParams" + req._objRequestParams);
        ro.mobilPay.util.ListItem li = req.encrypt(publicCer);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(host_base_url + "?" + "env_key=" + li.key + "&data=" + li.getVal()))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        System.out.println(response.uri());
        System.out.println(li.key);
        System.out.println(li.val);
    }
//public void get

    public void cardConfirm(String env_key, String data) throws Exception {
        ro.mobilPay.util.OpenSSL.extraInit();
//convert the private key to PEM openssl rsa -inform PEM -in private.key -out private-new.key
//        String privateKeyPath = getServletContext().getRealPath("WEB-INF/mobilpay-private.key");
        String privateKeyPath = "src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key";
        String privateKey = ro.mobilPay.util.FileHelper.getFileContents(privateKeyPath);

        int errorCode = 0;
        String errorMessage = "";
        int errorType = Abstract.CONFIRM_ERROR_TYPE_NONE;
        URL url = new URL(host_base_url + "?" + "env_key=" + env_key + "&data=" + data);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        System.out.println(con.getRequestMethod());
        if (con.getRequestMethod().equalsIgnoreCase("post")) {

            if (env_key == null || env_key.isEmpty())
                return;
            if (data == null || data.isEmpty())
                return;
            ro.mobilPay.payment.request.Abstract paymentRequest = ro.mobilPay.payment.request.Abstract.factoryFromEncrypted(env_key, data, privateKey);
            String action = paymentRequest._objReqNotify._action;
            //System.out.println("PR:signature:"+paymentRequest._signature);
            //System.out.println("PR:NTF:OA:"+paymentRequest._objReqNotify._originalAmount);
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

