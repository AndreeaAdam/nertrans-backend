package ro.nertrans.utils;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

@Service
public class RandomCodeService {

    /**
     * @Description Generates a random code for registration mails and keys
     * @return random code
     */
    public String generateRandomCode(){
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[9];
        random.nextBytes(bytes);
        String randomCode;
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        randomCode =encoder.encodeToString(bytes);
        return randomCode;
    }


    /**
     * @Description: Generates a random string
     * @return String
     */
    public String randomString(int stringLength){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for(int i = 0; i < stringLength; i++) {
            int index = r.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }
}
