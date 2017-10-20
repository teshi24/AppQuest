package ch.appquest.brudinne.memory;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends
        RecyclerView.Adapter<MyAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public ImageView imageView;
        public Button photoButton;
        public Button backgroundButton;
        public TextView textView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            imageView        = (ImageView) itemView.findViewById(R.id.image);
            textView         = (TextView) itemView.findViewById(R.id.text);
            photoButton      = (Button) itemView.findViewById(R.id.newCardPhoto);
            backgroundButton = (Button) itemView.findViewById(R.id.newCard);
        }
    }

    private ArrayList<Card> thePictures;
    // Store the context for easy access
    private Context theContext;

    public MyAdapter(ArrayList<Card> pictures, Context context) {
        theContext = context;
        thePictures = pictures;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return theContext;
    }

    public void add(int position, Card item){
        thePictures.add(position, item);
        notifyItemInserted(position);
    }

    @Override
    public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.card_view, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(MyAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Card card = thePictures.get(position);
        TextView textView = viewHolder.textView;
        ImageView imageView = viewHolder.imageView;
        Button photoButton = viewHolder.photoButton;
        Button backgroundButton = viewHolder.backgroundButton;

        if(card instanceof PictureCard){
            // Set item views based on your views and data model
            textView.setText(((PictureCard) card).getDescription());
            imageView.setImageBitmap(((PictureCard) card).getPicture());
            photoButton.setVisibility(View.GONE);
            backgroundButton.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return thePictures.size();
    }

}