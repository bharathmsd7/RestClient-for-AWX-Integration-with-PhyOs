package dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.ImplementedBy;
import methods.Ansible;
import org.mongodb.morphia.Key;
import play.api.libs.json.Json;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@ImplementedBy(AnsibleDAO.class)
public interface IAnsibleDAO {

    Key<Ansible> save(Ansible key);

    Optional<Ansible> getName(String name);

    Optional<Ansible> getInventoryid(String inventoryid);

    Optional<Ansible> getProjectid(String projectid);

    Optional<Ansible> getjobtemplateid(String jobtemplateid);

    Optional<Ansible> getAnsibleproducts (HashMap<String, List> products);
}
