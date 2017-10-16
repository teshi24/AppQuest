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

/**
 * Created by Nadja on 16.10.2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private String[] dataset;
    private Activity activity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout matchView;

        public CardView cardLeft;
        public Button buttonLeft;
        public ImageView imageLeft;
        public TextView textLeft;

        public CardView cardRight;
        public Button buttonRight;
        public ImageView imageRight;
        public TextView textRight;

        public ViewHolder(LinearLayout matchView) {
            super(matchView);
            this.matchView = matchView;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(String[] dataset, Activity activity) {
        dataset = dataset;
        activity = activity;
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
        LinearLayout matchView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.match, parent, false);

        CardView cardLeft = (CardView) matchView.findViewById(R.id.cardViewLeft);
        Button buttonLeft = (Button) matchView.findViewById(R.id.buttonLeft);
        ImageView imageLeft = (ImageView) matchView.findViewById(R.id.imageLeft);
        TextView textLeft = (TextView) matchView.findViewById(R.id.textLeft);
        
        CardView cardRight = (CardView) matchView.findViewById(R.id.cardViewRight);
        Button buttonRight = (Button) matchView.findViewById(R.id.buttonRight);
        ImageView imageRight = (ImageView) matchView.findViewById(R.id.imageRight);
        TextView textRight = (TextView) matchView.findViewById(R.id.textRight);

        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(matchView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.matchView.findViewById(R.id.textLeft).setText(dataset[position]);
        Toast.makeText(activity, "here comes the bind view holder", Toast.LENGTH_LONG);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.length;
    }
}