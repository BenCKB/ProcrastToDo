package immaculateaxolotl.simplytime.database;

import android.content.Context;

import immaculateaxolotl.simplytime.tasks.Task;
import io.realm.OrderedRealmCollection;


public class RealmTaskAdapter extends RealmModelAdapter<Task>
{

	public RealmTaskAdapter(Context context, OrderedRealmCollection<Task> realmResults, boolean automaticUpdate)
	{

		super(context, realmResults, automaticUpdate);
	}
}
