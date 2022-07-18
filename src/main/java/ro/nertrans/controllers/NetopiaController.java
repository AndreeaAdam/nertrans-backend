package ro.nertrans.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.dtos.MobilPayDTO;
import ro.nertrans.services.NetopiaService;

import java.io.IOException;

@CrossOrigin
@RestController
public class NetopiaController {
    @Autowired
    private NetopiaService netopiaService;

    @RequestMapping(value = "/initPayment", method = RequestMethod.POST)
    public ResponseEntity<?> initPayment(@RequestBody MobilPayDTO dto) {
        try {
            return new ResponseEntity<>(netopiaService.initPayment(dto), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/cardConfirm", method = RequestMethod.POST)
    public Object cardConfirm(@RequestParam(value = "env_key") String env_key,
                              @RequestParam(value = "data") String data) {
        try {
             netopiaService.cardConfirm(env_key, data);
//            return netopiaService.cardConfirm(env_key, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("TEST////////////////////////////////////");
        return null;
    }
}
