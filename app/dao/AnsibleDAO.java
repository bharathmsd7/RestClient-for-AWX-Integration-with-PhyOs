package dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import methods.Ansible;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.w3c.dom.stylesheets.LinkStyle;
import play.api.libs.json.Json;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Singleton
public class AnsibleDAO extends BasicDAO<Ansible, String> implements IAnsibleDAO{

    @Inject
    protected AnsibleDAO(Datastore ds) {
        super(ds);
    }

    @Override
    public Optional<Ansible> getName(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<Ansible> getInventoryid(String inventoryid) {
        return Optional.empty();
    }

    @Override
    public Optional<Ansible> getProjectid(String projectid) {
        return Optional.empty();
    }

    @Override
    public Optional<Ansible> getjobtemplateid(String jobtemplateid) {
        return Optional.empty();
    }

    @Override
    public  Optional<Ansible> getAnsibleproducts (HashMap<String, List> products) { return Optional.empty(); }
}
