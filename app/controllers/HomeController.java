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
    private String hostIp = "192.168.1.70";
    public Ansible ansible;

    @Inject
    public HomeController ( RestClient rc, AnsibleService ansibleService,Ansible ansible) {
        this.rc = rc;
        this.ansibleService = ansibleService;
        this.ansible = ansible;
    }

    public Result index() throws InterruptedException, ExecutionException, TimeoutException {
        responsestatus = rc.getRequest(ip,"/api/v2/");
        //ansible.initalAnsibleSetup();
        ansibleService.AnsibleRuntime(appName,hostIp);
        return ok(index.render( responsestatus ));
    }

}
