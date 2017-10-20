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

import java.util.ArrayList;

/**
 * Created by Nadja on 16.10.2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<View> dataset;
    private Activity activity;

    /**
     * Provide a reference to the views for each data item
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout matchView;

        public CardView card;
        public Button newCard;
        public Button newCardPhoto;


        public ImageView image;
        public TextView text;

        public ViewHolder(LinearLayout matchView, int type) {
            super(matchView);
            this.matchView = matchView;

            if(type == 1) {
                card = (CardView) matchView.findViewById(R.id.pictureView);
                image = (ImageView) matchView.findViewById(R.id.image);
                text = (TextView) matchView.findViewById(R.id.text);
            } else{
                card = (CardView) matchView.findViewById(R.id.buttonView);
                newCard = (Button) matchView.findViewById(R.id.newCard);
                newCardPhoto = (Button) matchView.findViewById(R.id.newCardPhoto);
            }
        }

        public LinearLayout getMatchView() {
            return matchView;
        }

        public void setMatchView(LinearLayout matchView) {
            this.matchView = matchView;
        }

        public CardView getCard() {
            return card;
        }

        public void setCard(CardView card) {
            this.card = card;
        }

        public Button getNewCard() {
            return newCard;
        }

        public void setNewCard(Button newCard) {
            this.newCard = newCard;
        }
        public Button getNewCardPhoto() {
            return newCardPhoto;
        }

        public void setNewCardPhoto(Button newCardPhoto) {
            this.newCardPhoto = newCardPhoto;
        }

        public ImageView getImage() {
            return image;
        }

        public void setImage(ImageView image) {
            this.image = image;
        }

        public TextView getText() {
            return text;
        }

        public void setText(TextView text) {
            this.text = text;
        }
    }

    public void deleteButton(ViewGroup parent, int position){
        LinearLayout matchView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.match, parent, false);
        CardView card   = (CardView) matchView.findViewById(R.id.pictureView);
        ImageView image = (ImageView) matchView.findViewById(R.id.image);
        TextView text   = (TextView) matchView.findViewById(R.id.text);

        dataset.set(position, matchView);
        ///((Button)dataset.get(3).findViewById(R.id.newCard)).setVisibility(View.GONE);
        new ViewHolder(matchView, 1);
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
        Button button = (Button) matchView.findViewById(R.id.newCard);
        int type = 0;

        /*
        LinearLayout matchView = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.match, parent, false);
        CardView card = (CardView) matchView.findViewById(R.id.cardView);
        ImageView image = (ImageView) matchView.findViewById(R.id.image);
        TextView text = (TextView) matchView.findViewById(R.id.text);
        */

        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(matchView, type);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        //((TextView)holder.matchView.findViewById(R.id.text)).setText("test");
        View currentView = dataset.get(position);
        //holder.text.setText(dataset.get(position).toString());
        holder.newCard.setVisibility(currentView.getVisibility());
        //.setText(currentView.getNewCard());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataset.size();
    }
}