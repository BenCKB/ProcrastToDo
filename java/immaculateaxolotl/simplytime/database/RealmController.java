package immaculateaxolotl.simplytime.database;

import android.app.Activity;
import android.app.Application;

import androidx.fragment.app.Fragment;

import immaculateaxolotl.simplytime.tasks.Task;
import io.realm.Realm;
import io.realm.RealmResults;

public class RealmController
{

	private static RealmController instance;
	private final Realm realm;

	public RealmController(Application application)
	{
		realm = Realm.getDefaultInstance();
	}

	public static RealmController with(Fragment fragment)
	{
		if (instance == null) {
			instance = new RealmController(fragment.getActivity().getApplication());
		}
		return instance;
	}

	public static RealmController with(Activity activity)
	{

		if (instance == null) {
			instance = new RealmController(activity.getApplication());
		}
		return instance;
	}

	public static RealmController with(Application application)
	{

		if (instance == null) {
			instance = new RealmController(application);
		}
		return instance;
	}

	public static RealmController getInstance()
	{

		return instance;
	}

	public Realm getRealm()
	{
		return realm;
	}

	//Refresh the realm instance
	public void refresh()
	{
		realm.refresh();
	}

	//clear all objects from Task.class
	public void clearAll()
	{
		realm.beginTransaction();
		RealmResults<Task> result = realm.where(Task.class).findAll();
		result.deleteAllFromRealm();
		realm.commitTransaction();
	}

	//find all objects in the Task.class
	public RealmResults<Task> getTasks()
	{
		return realm.where(Task.class).findAll();
	}

	//query a single item with the given id
	public Task getTask(String id)
	{

		return realm.where(Task.class).equalTo("id", id).findFirst();
	}

	//check if Task.class is empty
	public boolean hasTasks()
	{
		RealmResults<Task> result = realm.where(Task.class).findAll();

		return !result.isEmpty();
	}

	public int amountTask()
	{
		RealmResults<Task> result = realm.where(Task.class).findAll();
		return result.size();
	}

	//query example
	public RealmResults<Task> queryedTasks(int time)
	{

		return realm.where(Task.class)
				.lessThanOrEqualTo("time", time)
				.findAll();

	}

	public boolean taskExists(Task task)
	{
		if (task != null) {
			realm.beginTransaction();
			RealmResults<Task> rows = realm.where(Task.class).equalTo("id", task.getId()).findAll();
			realm.commitTransaction();
			if (rows.isEmpty()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public void deleteItem(Task task)
	{
		realm.beginTransaction();
		RealmResults<Task> rows = realm.where(Task.class).equalTo("id", task.getId()).findAll();
		rows.deleteAllFromRealm();
		realm.commitTransaction();
	}
}
