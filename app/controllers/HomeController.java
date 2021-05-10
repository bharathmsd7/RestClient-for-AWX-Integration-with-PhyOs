package controllers;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import methods.InitAnsible;
import play.mvc.*;
import views.html.index;

import play.libs.Json;
import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.databind.JsonNode;
import methods.InitAnsible.*;


public class HomeController extends Controller {

    public restclient rc;
    public Integer responsestatus;
    private String ip = "http://192.168.1.72";
    public InitAnsible initAnsible;

    @Inject
    public HomeController (RestClient rc , InitAnsible initAnsible) {
        this.rc = rc;
        this.initAnsible = initAnsible;
    }

    public Result index() throws InterruptedException, ExecutionException, TimeoutException {
        responsestatus = rc.getRequest(ip,"/api/v2/");
        initAnsible.InitAnsibleSteps();
        return ok(index.render( responsestatus ));
    }



}
