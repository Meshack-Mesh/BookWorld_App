package com.example.bookworld;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.bookdata.Book;
import com.example.bookworld.bookdata.FictionAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity implements FictionAdapter.OnBookClickListener {

    private FirebaseFirestore db;
    private FictionAdapter trendingAdapter;
    private List<Book> bookList;
    private EditText searchEditText;
    private TextView searchButton;
    private TextView messageTextView;
    private ImageView threeDotsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Initialize views
        RecyclerView recyclerView = findViewById(R.id.recyclerTrendingBooks);
        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        messageTextView = findViewById(R.id.messageTextView);
        threeDotsButton = findViewById(R.id.three_dotButton);
        LinearLayout searchLayout = findViewById(R.id.searchbutton);
        LinearLayout moreLayout = findViewById(R.id.morelayout);
        LinearLayout myBooksLayout = findViewById(R.id.mybookslayout);

        // Setup RecyclerView
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2); // 2 columns
        recyclerView.setLayoutManager(gridLayoutManager);
        // Set horizontal layout
        bookList = new ArrayList<>();
        trendingAdapter = new FictionAdapter(bookList, this);
        recyclerView.setAdapter(trendingAdapter);

        // Retrieve book details from Firestore
        retrieveBooks();

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

        threeDotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the "three dots" activity
                Intent intent = new Intent(Home.this, three_dots.class);
                startActivity(intent);
            }
        });

        // Set the editor action listener for the search EditText
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER || actionId == KeyEvent.ACTION_DOWN || actionId == KeyEvent.KEYCODE_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(query)) {
                    searchBooks(query);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBookClick(Book book) {
        if ("Not for Sale".equalsIgnoreCase(book.getPrice())) {
            // Navigate to BookDetails activity if the book is not for sale
            Intent intent = new Intent(Home.this, BookDetails.class);
            intent.putExtra("BOOK_ID", book.getId());
            intent.putExtra("BOOK_TITLE", book.getTitle());
            intent.putExtra("BOOK_AUTHOR", book.getAuthor());
            intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
            intent.putExtra("BOOK_PRICE", book.getPrice());
            intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
            intent.putExtra("BOOK_RATING", book.getRating());
            intent.putExtra("PDF_URL", book.getPdfUrl());
            startActivity(intent);
        } else {
            // Navigate to BuyBook activity if the book has a price
            Intent intent = new Intent(Home.this, BuyDetails.class);
            intent.putExtra("BOOK_ID", book.getId());
            intent.putExtra("BOOK_TITLE", book.getTitle());
            intent.putExtra("BOOK_AUTHOR", book.getAuthor());
            intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
            intent.putExtra("BOOK_PRICE", book.getPrice());
            intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
            intent.putExtra("BOOK_RATING", book.getRating());
            intent.putExtra("PDF_URL", book.getPdfUrl());
            startActivity(intent);
        }
    }


    private void retrieveBooks() {
        db.collection("Fiction")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            bookList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String id = document.getId();
                                String thumbnailUrl = document.getString("thumbnailUrl");
                                String title = document.getString("title");
                                String author = document.getString("author");
                                String description = document.getString("description");
                                String pdfUrl = document.getString("pdfUrl");

                                // Fetch the daysToBorrow value and ensure it's not null
                                Long daysToBorrowLong = document.getLong("daysToBorrow");
                                int daysToBorrow = (daysToBorrowLong != null) ? daysToBorrowLong.intValue() : 0;  // Default to 0 if null

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
                                Book book = new Book(id, thumbnailUrl, title, author, description, price, rating, pdfUrl, daysToBorrow);
                                bookList.add(book);
                            }
                            // Notify the adapter that the data set has changed
                            trendingAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            Log.e("FirestoreError", "Error getting books: ", task.getException());
                            Toast.makeText(Home.this, "Error fetching books", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void searchBooks(String query) {
        db.collection("Comics")
                .whereEqualTo("title", query)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bookList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String thumbnailUrl = document.getString("thumbnailUrl");
                            String title = document.getString("title");
                            String author = document.getString("author");
                            String description = document.getString("description");
                            String pdfUrl = document.getString("pdfUrl");
                            int daysToBorrow = document.getLong("daysToBorrow").intValue();  // Use intValue() to convert Long to int


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
                            Book book = new Book(id, thumbnailUrl, title, author, description, price, rating, pdfUrl, daysToBorrow);
                            bookList.add(book);
                        }
                        // Notify adapter of data change
                        trendingAdapter.notifyDataSetChanged();

                        // Show/hide messageTextView based on search result
                        if (bookList.isEmpty()) {
                            messageTextView.setText("Book not available");
                            messageTextView.setVisibility(View.VISIBLE);
                        } else {
                            messageTextView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e("FirestoreError", "Error getting documents: ", task.getException());
                        Toast.makeText(getApplicationContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
