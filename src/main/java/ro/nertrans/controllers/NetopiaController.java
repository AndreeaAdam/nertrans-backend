package ro.nertrans.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.dtos.MobilPayDTO;
import ro.nertrans.dtos.NetopiaResponseDTO;
import ro.nertrans.services.NetopiaService;

import java.io.IOException;

@CrossOrigin
@RestController
public class NetopiaController {
    @Autowired
    private NetopiaService netopiaService;

    @PostMapping(value = "/initPayment")
    public ResponseEntity<NetopiaResponseDTO> initPayment(@RequestBody MobilPayDTO dto) {
        try {
            return new ResponseEntity<>(netopiaService.initPayment(dto), HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping(value = "/cardConfirm")
    public Object cardConfirm(@RequestParam(value = "env_key") String env_key,
                              @RequestParam(value = "data") String data) {
        try {
            return netopiaService.cardConfirm(env_key, data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
