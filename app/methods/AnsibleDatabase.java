package methods;

import dao.IAnsibleDAO;
import jdk.nashorn.internal.runtime.regexp.joni.ast.StringNode;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateOperations;
import javax.inject.Inject;

@Entity(value="Ansible", noClassnameStored = true)
public class AnsibleDatabase {

    @Inject
    IAnsibleDAO iAnsibleDAO;
    @Id
    private String uId;
    private String name;
    private String inventoryid;
    private String projectid;
    private String jobtemplateid;


    public AnsibleDatabase(String name, String inventoryid, String projectid, String jobtemplateid){
        this.name = name;
        this.inventoryid = inventoryid;
        this.projectid = projectid;
        this.jobtemplateid = jobtemplateid;
    }

    @Inject
    public AnsibleDatabase(){}

    public String getName(String name) {
        return name;
    }

    public void setName(String name) { this.name = name;}

    public String getInventoryid() {
        return inventoryid;
    }

    public void setInventoryid(String inventoryid) {
        this.inventoryid = inventoryid;
    }

    public String getProjectid() {
        return  projectid;
    }

    public void setProjectid(String projectid) {
        this.projectid = projectid;
    }

    public String getJobtemplateid() {
        return jobtemplateid;
    }

    public void setJobtemplateid(String jobtemplateid) { this.jobtemplateid = jobtemplateid; }


}
