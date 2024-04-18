package au.scottmann.firestoredemo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference bookRef = db.collection("Books");
    private String TAG = "FireStore";
    private String RECTAG = "FireStore Recycler";
    private FbHelper fbHelper;

    //UI
    private EditText titleIn, authorIn, priceIn, isbnIn;
    private Button addBookBtn;
    private BookAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make a FbHelper object
        fbHelper = new FbHelper(db);

        //UI
        titleIn = findViewById(R.id.titleIn);
        authorIn = findViewById(R.id.authorIn);
        priceIn = findViewById(R.id.priceIn);
        isbnIn = findViewById(R.id.isbnIn);
        addBookBtn = findViewById(R.id.addBookBtn);

        //Button listener
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //I would also add some conditions here to check each field has been entered, basic java :)
                fbHelper.addBook(new Book(authorIn.getText().toString(), titleIn.getText().toString(), Integer.parseInt(priceIn.getText().toString().trim()), isbnIn.getText().toString()));
                //reset fields
                authorIn.getText().clear();
                titleIn.getText().clear();
                priceIn.getText().clear();
                isbnIn.getText().clear();

                closeKeyboard();
            }
        });

        //seed database
        fbHelper.addBook(new Book("J.R.R.Tolkien", "The Lord of the Rings", 33, "234567"));
        fbHelper.addBook(new Book("Anne McCaffrey", "The White Dragon", 5, "55667722"));
        fbHelper.addBook(new Book("Terry Pratchett", "Hogfather", 17, "66773497"));

        //read Firestore data to log in handler (good for debugging)
        Log.d(TAG, "Printing books in handler");
        fbHelper.readToLog();

        /* Without writing custom callbacks of using lifecycle aware coding using a helper class for get methods
        is not viable as firestore get calls are asynchronous, so I would keep gets in main class (other than maybe
        a readAllTo Log style function).
         */

        //example compound query (results in logcat)
        //db.collection("Books")
        bookRef
                .whereEqualTo("Author", "Terry Pratchett")
                .whereLessThan("Price", 20)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, "Compound Query:" + document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //update book in database
        fbHelper.updatePrice("55667722", 2000);
        fbHelper.updatePrice("234567", 4 );

        //delete book in database
        fbHelper.deleteBook("55667722");

        //example Firestore UI RecyclerView
        setUpRecyclerView();
    }

    private void closeKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setUpRecyclerView(){
        //Query query = bookRef.orderBy("Price").limit(50);  <<this works, its just another option
        Query query = db.collection("Books").orderBy("Author", Query.Direction.ASCENDING);
        Log.d(RECTAG, "Built recycler query");
        FirestoreRecyclerOptions<Book> options = new FirestoreRecyclerOptions.Builder<Book>()
                .setQuery(query, Book.class)
                .build();
        Log.d(RECTAG, "Built recycler options");

        adapter = new BookAdapter(options);
        Log.d(RECTAG, "set adapter");

        RecyclerView recyclerView = findViewById(R.id.recView);
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //note as we are not using up and down gestures the first argument is 0
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getBindingAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);

        //implement interface from book adapter so Main activity can recieve data from the adapter
        adapter.setOnItemClickListener(new BookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                //Use documentSnapshot to create object
                Book book = documentSnapshot.toObject(Book.class);
                //you can now access the book members variables .. book.getTitle() etc etc
                //document snapshot also has many powerful capabilities
                //https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/DocumentSnapshot
                String id = documentSnapshot.getId();
                Toast.makeText(MainActivity.this, "Position:" + position + " ID:" +  id, Toast.LENGTH_SHORT).show();
                //I just used a toast as an example but you could open another Activity, all sorts of things
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
        Log.d(RECTAG, "Listening for firestore changes");
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}