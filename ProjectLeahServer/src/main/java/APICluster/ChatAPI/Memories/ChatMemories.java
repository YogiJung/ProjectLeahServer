package APICluster.ChatAPI.Memories;

import ExampleDataBases.MongoDB.CollectionManager;
import ExampleDataBases.MongoDB.DataBaseManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDateTime;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatMemories {
    List<String> memories = new ArrayList<>();
    DataBaseManager dataBaseManager = new DataBaseManager();
    MongoCollection mongoCollection;
    public ChatMemories(String uri, String database, String collection) {
        mongoCollection = dataBaseManager.connectDataBase(uri, database, collection);
    }

    public JsonArray makeChatMemories(String prompt, int flag) {
        JsonObject message = new JsonObject();
        JsonObject systemMessage = new JsonObject();
        JsonArray messages = new JsonArray();
        String memory = "You are my friend";
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", memory);
        messages.add(systemMessage);

//        if (flag == 0) {
//            String summary = CollectionManager.findLastString(mongoCollection);
//            memories.add(summary);
//        }
//        if (memories.size() > 5) {
//            String summary = SummaryAlgorithm.makeSummary(memories);
//            memories.clear();
//            memories.add(summary);
//            Document doc = new Document();
//            doc.append("systemSummary", summary);
//            doc.append("timeStamp", new BsonDateTime(new Date().getTime()));
//            CollectionManager.insertOneDocument(mongoCollection, doc);
//        }
//        for (int i = 0; i < memories.size(); i++) {
//            JsonObject memoryMessage = new JsonObject();
//            if (i == 0) {
//                memoryMessage.addProperty("role", "system");
//                memoryMessage.addProperty("content",memories.get(i));
//            } else {
//                memoryMessage.addProperty("role", "user");
//                memoryMessage.addProperty("content", memories.get(i));
//            }
//            messages.add(memoryMessage);
//        }



        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);

        return messages;
    }

    public void addMemory(String content) {
        memories.add(content);
    }
    public void addMemory(List<String> contents) {
        memories.addAll(contents);
    }

    public List<String> getMemory() {
        return memories;
    }


}
