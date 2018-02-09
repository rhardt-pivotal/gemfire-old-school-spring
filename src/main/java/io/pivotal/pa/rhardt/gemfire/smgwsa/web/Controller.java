package io.pivotal.pa.rhardt.gemfire.smgwsa.web;

//import com.gemstone.gemfire.cache.Region;
//import com.gemstone.gemfire.cache.query.Struct;
//import com.gemstone.gemfire.pdx.PdxInstance;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.query.Struct;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@org.springframework.stereotype.Controller
public class Controller {

    @Resource(name="Album_default")
    Region<String, PdxInstance> albums;


    @RequestMapping("/")
    public ResponseEntity<String> hello(HttpSession sess) {
        sess.setAttribute("putinsession", "youbetcha");
        return ResponseEntity.ok("Hello there");
    }

    @RequestMapping("/albums")
    public ResponseEntity<String> albums() throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><table><tr><th>Artist</th><th>Title</th></tr>");

        List<Object> results = albums.query("select artist, title from /Album_default").asList();

        for (Object r : results){
            Struct struct = (Struct) r;
            sb.append("<tr><td>"+struct.get("artist")+"</td><td>"+struct.get("title")+"</td></tr>");
        }

        sb.append("</table></body></html>");

        return ResponseEntity.ok(sb.toString());


    }


}
