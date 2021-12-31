package boardexample.myboard.global;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {


    @PostMapping("/logindd")
    public String e(){
        System.out.println("여까지옴?");
        return "OD";
    }

}
