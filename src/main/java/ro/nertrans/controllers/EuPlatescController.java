package ro.nertrans.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.dtos.MobilPayDTO;
import ro.nertrans.services.EuPlatescService;
import ro.nertrans.services.NetopiaService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@CrossOrigin
@RestController
public class EuPlatescController {
    @Autowired
    private EuPlatescService euPlatescService;
    @Autowired
    private NetopiaService netopiaService;

//    @RequestMapping(value = "/initPayment", method = RequestMethod.POST)
//    public URI testAdd(@RequestBody EuPlatescDTO dto) {
//        try {
//            return euPlatescService.pay(dto);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }
    @RequestMapping(value = "/initPayment", method = RequestMethod.POST)
    public ResponseEntity<?> initPayment(@RequestBody MobilPayDTO dto) {
        try {
            return new ResponseEntity<>(netopiaService.initPayment(dto), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
//    @RequestMapping(value = "/cardConfirm", method = RequestMethod.POST)
//    public Object cardConfirm(@RequestParam(value = "env_key") String env_key,
//                              @RequestParam(value = "data") String data) {
//        try {
//            netopiaService.cardConfirm(env_key, data);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return null;
//    }

    @RequestMapping(value = "/cardConfirm", method = RequestMethod.POST)
    public Object cardConfirm(HttpServletRequest request) {
        try {
            netopiaService.cardConfirm(request);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
