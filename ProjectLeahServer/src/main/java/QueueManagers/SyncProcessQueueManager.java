package QueueManagers;

import Utils.BackPressure;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SyncProcessQueueManager {
    List<SyncProcessQueueObject> queueObjects = new ArrayList<>(List.of(new SyncProcessQueueObject<ByteBuf>("InitialQueue")));
    SyncProcessQueueObject<ByteBuf>[] dataBufferObjects;
    public SyncProcessQueueObject generateSyncQueue(String name, int type_num) {
        // type_num => 0(byte[]), 1(String), 2(Integer), 3(ByteBuf), 4(Objects)
        SyncProcessQueueObject queueObject = null;
        if (type_num == 0) {
            queueObject = new SyncProcessQueueObject<byte[]>(name);
        }
        else if (type_num == 1) {
            queueObject = new SyncProcessQueueObject<String>(name);
        }
        else if (type_num == 2) {
            queueObject = new SyncProcessQueueObject<Integer>(name);
        } else if (type_num == 3) {
            queueObject = new SyncProcessQueueObject<ByteBuf>(name);
        } else if (type_num == 4) {
            queueObject = new SyncProcessQueueObject<Objects>(name);
        }

        queueObjects.add(queueObject);
        return queueObject;
    }

    public SyncProcessQueueObject findSyncQueue(String name) {
        for (SyncProcessQueueObject queueObject : queueObjects) {
            if (queueObject.getName().equals(name)) {
                return queueObject;
            }
        }
        return null;
    }

    public List<SyncProcessQueueObject> getQueueObjects() {
        return queueObjects;
    }
   public void setDataBufferObjects(int num_api) {
        dataBufferObjects = new SyncProcessQueueObject[num_api];
        for (int i = 0; i < num_api; i++) {
            dataBufferObjects[i] = new SyncProcessQueueObject<ByteBuf>("DataBufferQueue" + i);
        }
   }
   public SyncProcessQueueObject[] getDataBufferObjects() {
        return dataBufferObjects;
   }
   public void clearInitialQueue() {
        SyncProcessQueueObject initialQueue = findSyncQueue("InitialQueue");
        BackPressure.backPressureFlag = 2;
        initialQueue.clearQueue();

   }


}
