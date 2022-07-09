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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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

    //    }
    public void cardConfirm(String env_key, String data) throws Exception {
        OpenSSL.extraInit();
        URL url = new URL(settingService.getSettings().get().getNetopiaPrivateKey().getFilePath());
//        URL url = new URL("http://45.86.220.233:3838/nertrans/files/setting/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivatee.key");
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
        URL confirmUrl = new URL(apiUrl + "/cardConfirm?" + "env_key=" + env_key + "&data=" + data);
        HttpURLConnection con = (HttpURLConnection) confirmUrl.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
//        System.out.println( "convert "+ convertKey("http://45.86.220.233:3838/nertrans/files/setting/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key"));
        if (con.getRequestMethod().equalsIgnoreCase("post")) {
            if (env_key == null || env_key.isEmpty()) return;
            if (data == null || data.isEmpty()) return;
            Abstract paymentRequest = Abstract.factoryFromEncrypted(env_key, data, privateKey.toString());
            String action = paymentRequest._objReqNotify._action;
            System.out.println("action " + action);
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
//
//    public String openssl_unseal(String data, String env_key, String prvkey) {
//        try {
//            StringReader sr = new StringReader(prvkey);
//            PEMReader pm = new PEMReader(sr);
//            System.out.println(prvkey);
//            Object o = pm.readObject();
//            if (o != null && o instanceof KeyPair) {
//                KeyPair kpr = (KeyPair) o;
//                Key key = kpr.getPrivate();
//                Cipher ccRSA = Cipher.getInstance("RSA");
//                ccRSA.init(2, key);
//                byte[] envb = Base64.decode(env_key);
//                byte[] decrkey = ccRSA.doFinal(envb);
//                SecretKeySpec sc = new SecretKeySpec(decrkey, "ARCFOUR");
//                Cipher cc = Cipher.getInstance("ARCFOUR");
//                cc.init(2, sc);
//                byte[] ksrc = cc.doFinal(Base64.decode(data));
//                return new String(ksrc);
//            } else {
//                System.err.println("2 EROARE private key probably DER not PEM. user openssl to convert: " + prvkey.toString());
//                return null;
//            }
//        } catch (Exception var13) {
//            String aux = "data - " + data + "<br/>env_key=" + env_key + "<br/>";
//            System.err.println("2 EROARE unseal SMS : " + var13.getMessage() + aux);
//            var13.printStackTrace();
//            return null;
//        }
//    }
//
//    public Abstract factoryFromEncrypted(String _envKey, String _encData, String prvkey) throws Exception {
//        String data = openssl_unseal(_encData, _envKey, prvkey);
//        return Abstract.factory(data);
//    }


//    public String convertKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
//        Security.addProvider(new BouncyCastleProvider());
//        URL url = new URL("http://45.86.220.233:3838/nertrans/files/setting/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key");
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
//
//        Reader reader = new StringReader(privateKey.toString());
//        PemReader pemReader = new PemReader(reader);
//        KeyFactory fact = KeyFactory.getInstance("RSA");
//        PemObject pemObject = pemReader.readPemObject();
//        byte[] keyContentAsBytesFromBC = pemObject.getContent();
//        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyContentAsBytesFromBC);
//        PublicKey publicKey = fact.generatePublic(pubKeySpec);
//        System.out.println(publicKey);
//        return null;
//
////        // My private key DER file.
////        byte[] privateKeyDerBytes = Files.readAllBytes(Path.of("src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key"));
////        // Convert the DER file to a Java PrivateKey
////        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyDerBytes);
////        KeyFactory factory = KeyFactory.getInstance("RSA");
////        PrivateKey pk = factory.generatePrivate(spec);
////
////        // Use PemWriter to write the key to PEM format
////        StringWriter sw = new StringWriter();
////        JcaPEMWriter writer = new JcaPEMWriter(new PrintWriter(System.out));
////        writer.writeObject(sw);
////        writer.close();
////        try (PemWriter pw = new PemWriter(sw)) {
////            PemObjectGenerator gen = new JcaMiscPEMGenerator(pk);
////            pw.writeObject(gen);
////        }
////        return sw.toString();
//    }
//
//    public  byte[] loadKeyFile() throws InvalidKeySpecException, NoSuchAlgorithmException {
//        Security.addProvider(new BouncyCastleProvider());
//
//        File inFile = new File("src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key");
//        long fileLen = inFile.length();
//        Reader reader = null;
//        PemObject pemObject = null;
//        try {
//            reader = new FileReader(inFile);
//
//            char[] content = new char[(int) fileLen];
//            reader.read(content);
//            String str = new String(content);
//
//            StringReader stringreader = new StringReader(str);
//            PemReader pem = new PemReader(stringreader);
//            pemObject = pem.readPemObject();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        byte[] keyContentAsBytesFromBC = pemObject.getContent();
//        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(keyContentAsBytesFromBC);
//        KeyFactory fact = KeyFactory.getInstance("RSA");
//        System.out.println(pubKeySpec.getFormat());
//        PublicKey publicKey = fact.generatePublic(pubKeySpec);
//        System.out.println(publicKey);
//        return pemObject.getContent();
//    }
//
//    public RSAPrivateKey readPrivateKey() throws Exception {
//        URL url = new URL("http://45.86.220.233:3838/nertrans/files/setting/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key");
//
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
//
//        String privateKeyPEM = privateKey.toString()
//                .replace("-----BEGIN PRIVATE KEY-----", "")
//                .replaceAll(System.lineSeparator(), "")
//                .replace("-----END PRIVATE KEY-----", "");
//
//        byte[] encoded = Base64.decode(privateKeyPEM);
//
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
//        System.out.println(keyFactory.generatePrivate(keySpec));
//        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
//
//



//        try (FileReader keyReader = new FileReader("src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key")) {
//            PEMParser pemParser = new PEMParser(keyReader);
//            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
//            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());
//
//            return (RSAPrivateKey) converter.getPrivateKey(privateKeyInfo);
//        }
//
//


//        KeyFactory factory = KeyFactory.getInstance("RSA");
//
//        try (FileReader keyReader = new FileReader("src/main/resources/sandbox.Q3F5-2AE2-ESXJ-FLUJ-8WHKprivate.key");
//             PemReader pemReader = new PemReader(keyReader)) {
//
//            PemObject pemObject = pemReader.readPemObject();
//            byte[] content = pemObject.getContent();
//            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
//            System.out.println(factory.generatePrivate(privKeySpec).toString());
//            return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
//        }
//    }
}

