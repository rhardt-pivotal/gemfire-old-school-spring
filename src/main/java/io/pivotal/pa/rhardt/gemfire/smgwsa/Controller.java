package io.pivotal.pa.rhardt.gemfire.smgwsa;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.stereotype.Controller
public class Controller {

    @RequestMapping("/")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello there");
    }

}
