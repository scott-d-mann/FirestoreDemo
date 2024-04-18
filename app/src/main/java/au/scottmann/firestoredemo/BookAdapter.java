package au.scottmann.firestoredemo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

public class BookAdapter extends FirestoreRecyclerAdapter <Book, BookAdapter.BookHolder>{
    private OnItemClickListener listener;

    public BookAdapter(@NonNull FirestoreRecyclerOptions<Book> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull BookHolder holder, int position, @NonNull Book model) {
        holder.textViewAuthor.setText(model.getAuthor());
        Log.d("Holder", "Model " + model.toString());
        holder.textViewTitle.setText(model.getTitle());
        holder.textViewISBN.setText(model.getISBN());
        holder.textViewPrice.setText("$" + Integer.toString(model.getPrice()) + ".00");
    }

    @NonNull
    @Override
    public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview, parent, false);
        return new BookHolder(v);
    }

    //must be public!!
    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class BookHolder extends RecyclerView.ViewHolder{

        TextView textViewTitle;
        TextView textViewAuthor;
        TextView textViewISBN;
        TextView textViewPrice;

        public BookHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.rec_title);
            textViewPrice = itemView.findViewById(R.id.rec_price);
            textViewAuthor = itemView.findViewById(R.id.rec_author);
            textViewISBN = itemView.findViewById(R.id.rec_isbn);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null)
                    {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }

                }
            });
        }
    }

    //the interface & method below lets us send data from adapter to activity that implements interface
    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }


}
