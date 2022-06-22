package ro.nertrans.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ro.nertrans.dtos.EuPlatescDTO;
import ro.nertrans.services.EuPlatescService;

import java.io.IOException;
import java.net.URI;

@CrossOrigin
@RestController
public class EuPlatescController {
    @Autowired
    private EuPlatescService euPlatescService;

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
}
