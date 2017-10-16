package ch.appquest.brudinne.memory;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Nadja on 16.10.2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<View> dataset;
    private View[] dataset2;
    private Activity activity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout matchView;

        public CardView card;
        public Button button;
        public ImageView image;
        public TextView text;

        public ViewHolder(LinearLayout matchView) {
            super(matchView);
            this.matchView = matchView;

            card = (CardView) matchView.findViewById(R.id.cardView);

            if(image != null) {
                image = (ImageView) matchView.findViewById(R.id.image);
                text = (TextView) matchView.findViewById(R.id.text);
            } else{
                button = (Button) matchView.findViewById(R.id.button);
            }
        }
    }

    public void deleteButton(ViewGroup parent, int position){

        LinearLayout matchView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.match, parent, false);
        CardView card = (CardView) matchView.findViewById(R.id.cardView);
        ImageView image = (ImageView) matchView.findViewById(R.id.image);
        TextView text = (TextView) matchView.findViewById(R.id.text);

        dataset.set(position, matchView);
        ///((Button)dataset.get(3).findViewById(R.id.button)).setVisibility(View.GONE);
        new ViewHolder(matchView);
    }

    public void createNewCards(int ind1, int ind2){
        dataset.add(ind1, new View(activity));
        dataset.add(ind2, new View(activity));
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(View[] dataset, Activity activity) {
        this.dataset2 = dataset;
        this.activity = activity;
    }
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(ArrayList<View> dataset, Activity activity) {
        this.dataset = dataset;
        this.activity = activity;
    }

    /**
     * Create new views (invoked by the layout manager)
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {

        LinearLayout matchView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.button, parent, false);
        Button button = (Button) matchView.findViewById(R.id.button);

        /*
        LinearLayout matchView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.match, parent, false);
        CardView card = (CardView) matchView.findViewById(R.id.cardView);
        ImageView image = (ImageView) matchView.findViewById(R.id.image);
        TextView text = (TextView) matchView.findViewById(R.id.text);
        */

        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(matchView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //View currentView = dataset2[position];

        View currentView = dataset.get(position);

        ((TextView)holder.matchView.findViewById(R.id.text)).setText("test");

        Toast.makeText(activity, "here comes the bind view holder", Toast.LENGTH_LONG);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
        //return dataset2.length;
    }
}