package ExampleDataBases.MongoDB;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDateTime;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CollectionManager {
    public static void insertOneDocument(MongoCollection<Document> collection, String json) {
        try {
            Document doc = Document.parse(json);
            collection.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void insertOneDocument(MongoCollection<Document> collection, Document doc) {
        try {
            collection.insertOne(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void insertManyDocument(MongoCollection<Document> collection, List<Document> documentList) {
        try {
            collection.insertMany(documentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertManyJson(MongoCollection<Document> collection, List<String> jsonList) {
        List<Document> documentList = new ArrayList<>();
        try {
            for (String json : jsonList) {
                documentList.add(Document.parse(json));
            }
            collection.insertMany(documentList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> findDocumentByQuery(MongoCollection<Document> collection, Document query) {
        List<String> documentList = new ArrayList<String>();

        try {
            for (Document document : collection.find(query)) {
                documentList.add(document.toString());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return documentList;
    }

    public static List<String> findAllDocument(MongoCollection<Document> collection) {
        List<String> documentList = new ArrayList<>();
        try {
            for (Document document : collection.find()) {
                documentList.add(document.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentList;
    }

    public static Document findLastDocument(MongoCollection<Document> collection) {
        Document doc = collection.find().sort(new Document("timestamp", -1)).first();
        return doc;
    }

    public static String findLastString(MongoCollection<Document> collection) {
        try {
            Document doc = collection.find().sort(new Document("timestamp", -1)).first();
            if (doc == null) {
                Document nullDoc = new Document();
                nullDoc.append("systemSummary", "Empty");
                nullDoc.append("timeStamp", new BsonDateTime(new Date().getTime()));
                insertOneDocument(collection, nullDoc);
            }
            return JsonParser.parseString(doc.toJson()).getAsJsonObject().get("systemSummary").getAsString();
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
