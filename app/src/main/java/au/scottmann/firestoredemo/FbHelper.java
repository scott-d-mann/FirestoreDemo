package au.scottmann.firestoredemo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FbHelper {
    private FirebaseFirestore db;
    private String TAG = "FireStore";

    public FbHelper(FirebaseFirestore db) {
        this.db = db;
    }

    public void readToLog() {
        db.collection("Books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    public void addBook(Book b) {
        Map<String, Object> book = new HashMap<>();
        book.put("Title", b.getTitle());
        book.put("ISBN", b.getISBN());
        book.put("Author", b.getAuthor());
        book.put("Price", b.getPrice());

        // Add a new document with a generated ID
        db.collection("Books")
                .add(book)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    //private helper
    private void updatePriceWithId(final int price, String Id)
    {
        DocumentReference bookRef = db.collection("Books").document(Id);

        Log.d(TAG, "updated Price");
        bookRef
                .update("Price", price)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, " Now costs : " + price);
                        //readToLog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });

    }

    public void updatePrice(String isbn, final int price)
    {
        db.collection("Books")
                .whereEqualTo("ISBN", isbn)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                updatePriceWithId(price, document.getId());
                                Log.d(TAG, "Book with isbn " + Objects.requireNonNull(document.get("ISBN")).toString());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //private helpers     https://firebase.google.com/docs/firestore/manage-data/delete-data
    private void deleteBookWithId(String ID)
    {
        db.collection("Books").document(ID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Book successfully deleted!");
                        //readToLog();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }



    public void deleteBook(String isbn)
    {
        db.collection("Books")
                .whereEqualTo("ISBN", isbn)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                deleteBookWithId(document.getId());
                                Log.d(TAG, "Delete book with isbn " + Objects.requireNonNull(document.get("ISBN")).toString());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}