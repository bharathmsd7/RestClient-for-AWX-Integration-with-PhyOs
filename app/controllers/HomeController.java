package controllers;

import play.mvc.*;
import views.html.index;

import play.libs.Json;
import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.JsonNode;

import views.html.postrequest;

public class HomeController extends Controller {

    public restclient rc;
    public Integer responsestatus;
    private String ip = "http://192.168.1.72";
    private String path = "/api/v2/job_templates/9/launch/";
    private JsonNode temp = null;

    @Inject
    public HomeController (RestClient rc) {
        this.rc = rc;
    }

    public Result index() throws InterruptedException, ExecutionException, TimeoutException {
        responsestatus = rc.getRequest("http://192.168.1.72","/api/v2/");
        return ok(index.render( responsestatus ));
    }


    public Result postrequest() throws InterruptedException, ExecutionException, TimeoutException{

        JsonNode result = rc.postRequestWithoutData(ip, path);
        String res = result.get("job").asText();
        String time = result.get("created").asText();
       
        String res1 = getstatus(res);
        return ok(postrequest.render( res, res1, time ));

    }

    public String  getstatus (String jobid) throws InterruptedException, ExecutionException, TimeoutException {
        String path = "/api/v2/jobs/" + jobid + "/";
        JsonNode result;
        String res = null;

        result = rc.getRequestWithJsonAndTakeJson(ip, path);
        res = result.get("status").asText();
        return res;
    }



}
