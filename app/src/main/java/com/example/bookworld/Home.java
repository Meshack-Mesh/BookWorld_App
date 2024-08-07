package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.BookAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements BookAdapter.OnBookClickListener {

    private FirebaseFirestore db;
    private BookAdapter trendingAdapter;
    private BookAdapter newReleasesAdapter;
    private List<Book> trendingBooksList;
    private List<Book> newReleasesList;
    private TextView searchButton;
    private EditText searchEditText;
    private TextView messageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize layouts and button
        messageTextView = findViewById(R.id.messageTextView);
        searchEditText = findViewById(R.id.searchEditText);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout);
        LinearLayout searchLayout = findViewById(R.id.searchbutton);
        LinearLayout moreLayout = findViewById(R.id.morelayout);
        searchButton = findViewById(R.id.searchButton);
        ImageView threeDotButton = findViewById(R.id.three_dotButton); // Ensure this ID matches your XML
        RecyclerView recyclerTrendingBooks = findViewById(R.id.recyclerTrendingBooks);
        RecyclerView recyclerNewReleases = findViewById(R.id.recyclerNewReleases);
        db = FirebaseFirestore.getInstance();

        recyclerTrendingBooks.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerNewReleases.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        trendingBooksList = new ArrayList<>();
        newReleasesList = new ArrayList<>();

        trendingAdapter = new BookAdapter(trendingBooksList, this);
        newReleasesAdapter = new BookAdapter(newReleasesList, this);

        recyclerTrendingBooks.setAdapter(trendingAdapter);
        recyclerNewReleases.setAdapter(newReleasesAdapter);


        // Set onClick listeners
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    searchBooks(query);
                } else {
                    Toast.makeText(Home.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                }
            }
        });
        myBooksLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, MyBooks.class);
                startActivity(intent);
            }
        });

        searchLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, search_discovery.class);
                startActivity(intent);
            }
        });

        moreLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, More.class);
                startActivity(intent);
            }
        });

        threeDotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, three_dots.class);
                startActivity(intent);
            }
        });

        // Set click listener for BookAdapters
        trendingAdapter.setOnBookClickListener(this);
        newReleasesAdapter.setOnBookClickListener(this);

        // Retrieve trending books from Firestore
        retrieveTrendingBooks();

        // Retrieve new releases from Firestore
        retrieveNewReleases();
    }

    @Override
    public void onBookClick(Book book) {
        Intent intent = new Intent(Home.this, BookDetails.class);
        intent.putExtra("BOOK_ID", book.getId());
        intent.putExtra("BOOK_TITLE", book.getTitle());
        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
        intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
        intent.putExtra("BOOK_PRICE", book.getPrice());
        intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
        intent.putExtra("BOOK_RATING", book.getRating());
        intent.putExtra("PDF_URL", book.getPdfUrl()); // Pass the PDF URL
        startActivity(intent);
    }

    private void retrieveTrendingBooks() {
        db.collection("Fiction")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String thumbnailUrl = document.getString("thumbnailUrl");
                                String title = document.getString("title");
                                String author = document.getString("author");
                                String description = document.getString("description");
                                String pdfUrl = document.getString("pdfUrl");

                                // Retrieve price as a string (ensure it's stored as string in Firestore)
                                String price = document.getString("price");

                                float rating = 0.0f; // Default value if not found or conversion fails
                                Object ratingObj = document.get("rating");
                                if (ratingObj instanceof Double) {
                                    rating = ((Double) ratingObj).floatValue();
                                } else if (ratingObj instanceof Float) {
                                    rating = (Float) ratingObj;
                                }

                                // Create a Book object and add it to the trending books list
                                Book book = new Book(id, thumbnailUrl, title, author, description, price, rating, pdfUrl);
                                trendingBooksList.add(book);
                            }
                            // Notify the adapter that the data set has changed
                            trendingAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            // Log the error message
                            Log.e("FirestoreError", "Error getting trending books: ", task.getException());
                        }
                    }
                });
    }

    private void retrieveNewReleases() {
        db.collection("New Releases Books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            newReleasesList.clear(); // Clear the list before adding new data
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String thumbnailUrl = document.getString("thumbnailUrl");
                                String title = document.getString("title");
                                String author = document.getString("author");
                                String description = document.getString("description");
                                String pdfUrl = document.getString("pdfUrl");

                                // Retrieve price as a string (ensure it's stored as string in Firestore)
                                String price = document.getString("price");

                                float rating = 0.0f; // Default value if not found or conversion fails
                                Object ratingObj = document.get("rating");
                                if (ratingObj instanceof Double) {
                                    rating = ((Double) ratingObj).floatValue();
                                } else if (ratingObj instanceof Float) {
                                    rating = (Float) ratingObj;
                                }

                                // Create a Book object and add it to the new releases list
                                Book book = new Book(id, thumbnailUrl, title, author, description, price, rating, pdfUrl);
                                newReleasesList.add(book);
                            }
                            // Notify the adapter that the data set has changed
                            newReleasesAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            // Log the error message
                            Log.e("FirestoreError", "Error getting new releases: ", task.getException());
                        }
                    }
                });
    }
    private void searchBooks(String query) {
        List<String> collections = List.of("Fiction", "Technology", "Fantasy", "Animation", "Comics", "Art", "Non-fiction", "Business"); // Add all your collection names here
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        // Query each collection
        for (String collection : collections) {
            tasks.add(db.collection(collection)
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + "\uf8ff")
                    .get());
        }

        // Wait for all tasks to complete
        Tasks.whenAllSuccess(tasks).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                trendingBooksList.clear();
                boolean hasResults = false;

                for (Object result : task.getResult()) {
                    QuerySnapshot querySnapshot = (QuerySnapshot) result;
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String id = document.getId();
                        String thumbnailUrl = document.getString("thumbnailUrl");
                        String title = document.getString("title");
                        String author = document.getString("author");
                        String description = document.getString("description");
                        String pdfUrl = document.getString("pdfUrl");

                        // Retrieve price as a string (ensure it's stored as string in Firestore)
                        String price = document.getString("price");

                        float rating = 0.0f; // Default value if not found or conversion fails
                        Object ratingObj = document.get("rating");
                        if (ratingObj instanceof Double) {
                            rating = ((Double) ratingObj).floatValue();
                        } else if (ratingObj instanceof Float) {
                            rating = (Float) ratingObj;
                        }

                        // Create a Book object and add it to the list
                        Book book = new Book(id, thumbnailUrl, title, author, description, price, rating, pdfUrl);
                        trendingBooksList.add(book);
                        hasResults = true;
                    }
                }

                // Notify adapter of data change
                trendingAdapter.notifyDataSetChanged();

                // Show/hide messageTextView based on search result
                if (hasResults) {
                    messageTextView.setVisibility(View.GONE);
                } else {
                    messageTextView.setText("Book not available");
                    messageTextView.setVisibility(View.VISIBLE);
                    messageTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_empty_book, 0, 0, 0);
                }
            } else {
                Log.e("FirestoreError", "Error getting documents: ", task.getException());
                Toast.makeText(getApplicationContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
