package dao;

import methods.AnsibleDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Optional;

@Singleton
public class AnsibleDAO extends BasicDAO<AnsibleDatabase, String> implements IAnsibleDAO{

    @Inject
    public AnsibleDAO(Datastore ds) {
        super(ds);
    }

    @Override
    public Optional<AnsibleDatabase> getName(String name) {
        return Optional.empty();
    }

    @Override
    public Optional<AnsibleDatabase> getInventoryid(String inventoryid) {
        return Optional.empty();
    }

    @Override
    public Optional<AnsibleDatabase> getProjectid(String projectid) {
        return Optional.empty();
    }

    @Override
    public Optional<AnsibleDatabase> getJobtemplateid(String jobtemplateid) {
        return Optional.empty();
    }

}
