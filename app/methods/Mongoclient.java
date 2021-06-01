package methods;
import com.google.inject.Singleton;
import com.mongodb.MongoClient;
import com.typesafe.config.ConfigFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

@Singleton
public class Mongoclient {
    private static Datastore datastore;

    public static Datastore datastore() {
        if (datastore == null) {
            initDatastore();
        }
        return datastore;
    }

    public static void initDatastore() {

        final Morphia morphia = new Morphia();

        // Tell Morphia where to find our models
        morphia.mapPackage("methods");

        MongoClient mongoClient = new MongoClient(
                ConfigFactory.load().getString("ip"),
                ConfigFactory.load().getInt("port"));

        datastore = morphia.createDatastore(
                mongoClient, ConfigFactory.load().getString("dbName"));

    }
}