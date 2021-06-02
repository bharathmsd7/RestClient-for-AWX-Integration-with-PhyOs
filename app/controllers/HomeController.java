package controllers;

import methods.Ansible;
import methods.AnsibleService;
import play.mvc.*;
import views.html.index;

import javax.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


public class HomeController extends Controller {

    public restclient rc;
    public Integer responsestatus;
    private String ip = "http://192.168.1.72";
    public AnsibleService ansibleService;
    private String appName = "gitlab";
    private String hostIp = "192.168.1.177";

    @Inject
    public HomeController ( RestClient rc, AnsibleService ansibleService) {
        this.rc = rc;
        this.ansibleService = ansibleService;
    }

    public Result index() throws InterruptedException, ExecutionException, TimeoutException {
        responsestatus = rc.getRequest(ip,"/api/v2/");
        ansibleService.AnsibleRuntime(appName,hostIp);
        return ok(index.render( responsestatus ));
    }

}
