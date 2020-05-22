package immaculateaxolotl.simplytime.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import immaculateaxolotl.simplytime.HomeActivity;
import immaculateaxolotl.simplytime.R;
import immaculateaxolotl.simplytime.database.RealmRecyclerViewAdapter;
import immaculateaxolotl.simplytime.database.RealmController;

import io.realm.Realm;
import io.realm.RealmResults;

public class TaskAdapter extends RealmRecyclerViewAdapter<Task> implements ItemTouchHelperAdapter{

    private final Context context;
    private Realm realm;
    private LayoutInflater inflater;

    public TaskAdapter(Context context) {
        this.context = context;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a new card view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onItemDismiss(int position) {
        realm.beginTransaction();
        RealmResults<Task> rows = realm.where(Task.class).equalTo("id", getItem(position).getId()).findAll();
        rows.deleteAllFromRealm();
        realm.commitTransaction();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        realm = RealmController.getInstance().getRealm();

        // get the article
        final Task task = getItem(position);
        // cast the generic view holder to our specific one
        final CardViewHolder holder = (CardViewHolder) viewHolder;

        // set the title and the snippet
        holder.nameTitle.setText(task.getName());
        holder.timeTitle.setText((task.getTime()) + " minutes");

        //remove single match from realm
        holder.card.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.popup_delete){
                            RealmResults<Task> results = realm.where(Task.class).findAll();

                            // Get the task title to show it in toast message
                            Task t = results.get(position);
                            String title = t.getName();

                            RealmResults<Task> itemToRem = realm.where(Task.class).equalTo("id", t.getId()).findAll();

                            // All changes to data must happen in a transaction
                            realm.beginTransaction();
                            // remove single match
                            itemToRem.deleteAllFromRealm();
                            realm.commitTransaction();

                            notifyDataSetChanged();

                            Toast.makeText(context, title + " was removed from your tasks", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            LayoutInflater inflater = LayoutInflater.from(context);
                            final View infl = inflater.inflate(R.layout.add_task_box, null);
                            final EditText title = (EditText) infl.findViewById(R.id.add_task_box_title);
                            final NumberPicker minutesO = (NumberPicker) infl.findViewById(R.id.minutesPickerO);
                            final NumberPicker minutesT = (NumberPicker) infl.findViewById(R.id.minutesPickerT);
                            final AlertDialog.Builder updateTaskDialog = new AlertDialog.Builder(context);
                            minutesO.setMinValue(0);
                            minutesO.setMaxValue(9);
                            minutesT.setMinValue(0);
                            minutesT.setMaxValue(5);
                            updateTaskDialog.setView(infl);

                            RealmResults<Task> results = realm.where(Task.class).findAll();

                            final Task existingTask = results.get(position);

                            title.setText(existingTask.getName());
                            minutesO.setValue(existingTask.getTime() % 10);
                            minutesT.setValue(existingTask.getTime()/10);

                                                    /*
                                                     * Set action on "positive" response
                                                     */

                            updateTaskDialog.setPositiveButton("Update", null);

                                                    /*
                                                     * Set action on "negative" response
                                                     */

                            updateTaskDialog.setNegativeButton("Cancel", null);

                            final AlertDialog updateShowDialog = updateTaskDialog.create();

                            updateShowDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    Button b = updateShowDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                    b.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if(title.getText().toString().trim().equals("")){
                                                Toast.makeText(context, "Please enter a valid task name", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Task t = new Task();
                                                t.setId(existingTask.getId());
                                                t.setName(title.getText().toString());
                                                t.setTime(minutesO.getValue() + (10 * minutesT.getValue()));
                                                realm.beginTransaction();
                                                realm.copyToRealmOrUpdate(t);
                                                realm.commitTransaction();
                                                notifyDataSetChanged();
                                                Toast.makeText(context, "\"" + title.getText().toString() + "\" was updated.", Toast.LENGTH_SHORT).show();
                                                updateShowDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });


                            updateShowDialog.show();
                        }

                        return true;
                    }
                });

                popup.show();

                return false;
            }
        });
    }

    // return the size of your data set (invoked by the layout manager)
    public int getItemCount() {

        if (getRealmAdapter() != null) {
            return getRealmAdapter().getCount();
        }
        return 0;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public TextView nameTitle;
        public TextView timeTitle;

        public CardViewHolder(View itemView) {
            super(itemView);

            card = (CardView) itemView.findViewById(R.id.card_books);
            nameTitle = (TextView) itemView.findViewById(R.id.task_title);
            timeTitle = (TextView) itemView.findViewById(R.id.task_time);
        }
    }
}
