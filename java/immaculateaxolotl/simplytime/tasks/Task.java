package immaculateaxolotl.simplytime.tasks;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Task Realm Object:
 * Containing an id, a task name and an amount of time to complete the task
 */
public class Task extends RealmObject {

    @PrimaryKey
    private String id;
    @Required
    private String name;

    private int time;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

}
