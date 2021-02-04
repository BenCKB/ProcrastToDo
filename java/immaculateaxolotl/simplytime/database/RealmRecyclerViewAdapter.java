package immaculateaxolotl.simplytime.database;

import androidx.recyclerview.widget.RecyclerView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;

public abstract class RealmRecyclerViewAdapter<T extends RealmObject> extends RecyclerView.Adapter
{

	private RealmBaseAdapter<T> realmBaseAdapter;

	public T getItem(int position)
	{

		return realmBaseAdapter.getItem(position);
	}

	public RealmBaseAdapter<T> getRealmAdapter()
	{

		return realmBaseAdapter;
	}

	public void setRealmAdapter(RealmBaseAdapter<T> realmAdapter)
	{

		realmBaseAdapter = realmAdapter;
	}
}