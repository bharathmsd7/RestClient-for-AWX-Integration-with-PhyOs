package dao;

import methods.AnsibleDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.QueryResults;
import org.mongodb.morphia.query.UpdateResults;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class AnsibleDAO extends BasicDAO<AnsibleDatabase, String> implements IAnsibleDAO{

    @Inject
    public AnsibleDAO(Datastore ds) {
        super(ds);
    }

    @Override
    public Optional<AnsibleDatabase> getAnsibleDatabaseByName(String id) {
        Query<AnsibleDatabase> ansibleDatabaseQuery = this.createQuery().field("_id").equal(id);
        QueryResults<AnsibleDatabase> ansibleDatabaseQueryResults = this.find(ansibleDatabaseQuery);
        return Optional.ofNullable(ansibleDatabaseQueryResults.get());
    }

    @Override
    public UpdateResults updateJobList(String id, List<String> joblist){
        Query<AnsibleDatabase> ansibleDatabaseQuery = this.createQuery().field("_id").equal(id);
        return this.update(ansibleDatabaseQuery, this.createUpdateOperations().set("RunningJobsList",joblist));
    }

}
