package dao;

import methods.Ansible;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.dao.BasicDAO;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class AnsibleDAO extends BasicDAO<Ansible, String> implements IAnsibleDAO{

    @Inject
    public AnsibleDAO(Datastore ds){
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


}
