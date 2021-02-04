package immaculateaxolotl.simplytime.database;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;

public class RealmModelAdapter<T extends RealmObject> extends RealmBaseAdapter<T>
{

	public RealmModelAdapter(Context context, OrderedRealmCollection<T> realmResults, boolean auto)
	{

		super(realmResults);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		return null;
	}
}
