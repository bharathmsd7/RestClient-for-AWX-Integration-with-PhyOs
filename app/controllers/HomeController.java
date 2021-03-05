package controllers;

import play.mvc.*;
import views.html.index;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class HomeController extends Controller {

    public RestClient rc;
    public String result;

    @Inject
    public HomeController (RestClient rc){
        this.rc = rc;
    }

    public Result index() throws InterruptedException, ExecutionException, TimeoutException {
        //result = rc.getRequest("http://192.168.1.72","/api/v2/");
        result = rc.getRequestWithJson("192.168.1.72", "/api/v2/job_templates/");
        if (result == null){
            String n = "Null";
            return ok(index.render(n));
        }else{
            return ok(index.render(result));
        }

    }

}
