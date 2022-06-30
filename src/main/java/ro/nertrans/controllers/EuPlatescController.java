package ro.nertrans.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.dtos.EuPlatescDTO;
import ro.nertrans.services.EuPlatescService;
import ro.nertrans.services.NetopiaService;

import java.io.IOException;
import java.net.URI;

@CrossOrigin
@RestController
public class EuPlatescController {
    @Autowired
    private EuPlatescService euPlatescService;
    @Autowired
    private NetopiaService netopiaService;

    @RequestMapping(value = "/pay", method = RequestMethod.POST)
    public URI testAdd(@RequestBody EuPlatescDTO dto) {
        try {
            return euPlatescService.pay(dto);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    @RequestMapping(value = "/mobilPay", method = RequestMethod.POST)
    public Object mobilPay(@RequestBody EuPlatescDTO dto) {
        try {
            netopiaService.pay(dto);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
