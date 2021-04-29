package methods;

import javax.inject.Inject;

import com.typesafe.config.Config;
import utils.Constants;
import utils.Constants.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import play.Logger;

import static utils.Constants.*;

public class InitAnsible {

    private RestClient rc;
    private Config config;
    private Integer responseStatus;

    @Inject
    public InitAnsible(RestClient rc, Config config)
    {
        this.rc = rc;
        this.config = config;
    }

    @Inject
    public void InitAnsibleTemplates()
    {
        String ipAddress = "http://" +config.getString("ansible_node_ip");

        try {
            responseStatus = rc.getRequest(ipAddress, ANSIBLE_PING_PATH);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            Logger.error("Unable to PING Ansible Tower : ", e);
        }

        if (responseStatus == 200)
        {
            CreateInventories();
            CreateProjects();
            CreateJobTemplates();
        }
        else{
            Logger.error("Response is not valid : ",responseStatus);
        }

    }

    private void CreateInventories() {
        System.out.println("create I");
    }

    private void CreateProjects() {
        System.out.println("create P");
    }

    private void CreateJobTemplates() {
        System.out.println("create J");
    }



}
