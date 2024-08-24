package ExampleDataBases.MongoDB;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DataBaseManager {
    MongoCollection mongoCollection;
    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    public MongoCollection connectDataBase(String uri, String database, String collection) {
        mongoClient = MongoClients.create(uri);
        mongoDatabase = mongoClient.getDatabase(database);
        mongoCollection = mongoDatabase.getCollection(collection);
        return mongoCollection;
    }
}
