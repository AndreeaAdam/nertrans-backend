package ro.nertrans.services;

import org.springframework.stereotype.Service;
import ro.nertrans.dtos.EuPlatescDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class NetopiaService {
    String host_base_url = "http://sandboxsecure.mobilpay.ro";

    public void pay(EuPlatescDTO dto) throws Exception {

        ro.mobilPay.util.OpenSSL.extraInit();
        String signature = "2P33-YNCB-EXR5-XBWY-AVAU";

//        String publicCerPath = getServletContext().getRealPath("resources/mobilpay-public.cer");
        String publicCerPath = "src/main/resources/live.2P33-YNCB-EXR5-XBWY-AVAU.public.cer";
        String bodyJson = "src/main/resources/s.json";
        String paymentURL = "http://sandboxsecure.mobilpay.ro";

        String publicCer = ro.mobilPay.util.FileHelper.getFileContents(publicCerPath);
        String body = ro.mobilPay.util.FileHelper.getFileContents(bodyJson);
        ro.mobilPay.payment.request.Card req = new ro.mobilPay.payment.request.Card();

        req._signature = signature;
        req._confirmUrl = "http://localhost:8080/mobilPayJ/cardConfirm.php";
        req._returnUrl = "http://localhost:8080/mobilPayJ/cardReturn.jsp";
        req._orderId = ro.mobilPay.util.MD5.hash(""+Math.random());
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
        req.__set("parameter", "valueofparameter");
        ro.mobilPay.util.ListItem li = req.encrypt(publicCer);
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(host_base_url + "?" + "env_key=" + li.key + "&data=" + li.getVal()))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//        HttpResponse<String> response = client.send(request,
//                HttpResponse.BodyHandlers.ofString());
        System.out.println(li.key);
        System.out.println(li.val);
    }

    public void cardConfirm() throws Exception {
        ro.mobilPay.util.OpenSSL.extraInit();
//convert the private key to PEM openssl rsa -inform PEM -in private.key -out private-new.key
//        String privateKeyPath = getServletContext().getRealPath("WEB-INF/mobilpay-private.key");
        String privateKeyPath = "src/main/resources/mobilpay-public.cer";
        String privateKey = ro.mobilPay.util.FileHelper.getFileContents(privateKeyPath);

        int errorCode = 0;
        String errorMessage = "";
        int errorType = ro.mobilPay.payment.request.Abstract.CONFIRM_ERROR_TYPE_NONE;

//        if(request.getMethod().equalsIgnoreCase("post")) {
//            String key = request.getParameter("env_key");
//            String data = request.getParameter("data");
//
//            if(key == null || key.isEmpty())
//                return;
//            if(data == null || data.isEmpty())
//                return;
//            ro.mobilPay.payment.request.Abstract paymentRequest = ro.mobilPay.payment.request.Abstract.factoryFromEncrypted(key, data, privateKey);
//            String action = paymentRequest._objReqNotify._action;
//            //System.out.println("PR:signature:"+paymentRequest._signature);
//            //System.out.println("PR:NTF:OA:"+paymentRequest._objReqNotify._originalAmount);
//            System.out.println("Got action="+action+" for orderID="+paymentRequest._orderId);
//            System.out.println("and parameter is "+paymentRequest.__get("parameter"));
//            errorMessage = paymentRequest._objReqNotify._crc;
//            if(action.equalsIgnoreCase("confirmed")) {
//
//            }
//            else
//            if(action.equalsIgnoreCase("confirmed_pending")) {
//
//            }
//            else
//            if(action.equalsIgnoreCase("paid_pending")) {
//
//            }
//            else
//            if (action.equalsIgnoreCase("paid")) {
//
//            }
//            else
//            if(action.equalsIgnoreCase("canceled")) {
//
//            }
//            else
//            if(action.equalsIgnoreCase("credit")) {
//
//            }
//        }//end if is post
//
//
//        if(errorCode == 0)
//        {
//            System.out.print("<crc>"+errorMessage+"</crc>");
//        }
//        else
//        {
//            System.out.print("<crc error_type=\""+errorType+"\" error_code=\""+errorCode+"\">"+errorMessage+"</crc>");
//        }
//    }
}}
