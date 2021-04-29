package controllers;

import methods.InitAnsible;
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
import methods.InitAnsible.*;
import views.html.postrequest;

public class HomeController extends Controller {

    public restclient rc;
    public Integer responsestatus;
    private String ip = "http://192.168.1.72";
    private String path = "/api/v2/job_templates/9/launch/";
    private JsonNode temp = null;
    public InitAnsible initAnsible;

    @Inject
    public HomeController (RestClient rc , InitAnsible initAnsible) {
        this.rc = rc;
        this.initAnsible = initAnsible;
    }

    public Result index() throws InterruptedException, ExecutionException, TimeoutException {
        responsestatus = rc.getRequest("http://192.168.1.72","/api/v2/");
        return ok(index.render( responsestatus ));
    }


}
