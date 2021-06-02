package dao;

import methods.AnsibleDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;

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
    public Optional<AnsibleDatabase> getAnsibleDatabaseByName(String appName) {
        Query<AnsibleDatabase> ansibleDatabaseQuery = this.createQuery().field("name").equal(appName);
        QueryResults<AnsibleDatabase> ansibleDatabaseQueryResults = this.find(ansibleDatabaseQuery);
        return Optional.ofNullable(ansibleDatabaseQueryResults.get());
    }


}
