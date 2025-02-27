package com.example.bookworld.bookdata;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookworld.R;
import com.example.bookworld.ReturnBook;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ReturnBooksAdapter extends RecyclerView.Adapter<ReturnBooksAdapter.CartViewHolder> {

    private Context mContext;
    private List<Book> mCartItems;
    private FirebaseFirestore mFirestore;
    private String userId;

    public ReturnBooksAdapter(Context context, List<Book> cartItems, String userId) {
        mContext = context;
        mCartItems = cartItems;
        mFirestore = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_borrow, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mCartItems.size();
    }

    public interface OnBookClickListener {
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {

        private ImageView thumbnailImageView;
        private TextView titleTextView;
        private TextView authorTextView;
        private TextView priceTextView;
        private TextView countdownTextView; // Countdown TextView
        private Button returnBookButton; // Button to return the book

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.bookThumbnail);
            titleTextView = itemView.findViewById(R.id.bookTitle);
            authorTextView = itemView.findViewById(R.id.bookAuthor);
            priceTextView = itemView.findViewById(R.id.bookPrice);
            countdownTextView = itemView.findViewById(R.id.daysCountTextView); // Initialize Countdown TextView
            returnBookButton = itemView.findViewById(R.id.delete_button); // Initialize returnBookButton
        }

        public void bind(int position) {
            if (position < mCartItems.size()) {
                Book book = mCartItems.get(position);
                if (book != null) {
                    Picasso.get().load(book.getThumbnailUrl()).into(thumbnailImageView);
                    Log.d("Book Title", "Title: " + book.getTitle());
                    titleTextView.setText(book.getTitle());
                    authorTextView.setText("by " + book.getAuthor());
                    priceTextView.setText("Ksh " + book.getPrice());

                    returnBookButton.setOnClickListener(v -> removeItem(position, book));

                    // Assuming `book.getPrice()` returns the price of the book as a String
                    String price = book.getPrice(); // Assuming Book class has getPrice()

// Check if the price is "Not for sale" for the current book
                    if ("Not for sale".equals(price)) {
                        // Make sure the delete button is visible if the price is "Not for sale"
                        returnBookButton.setVisibility(View.VISIBLE);
                    } else {
                        // Hide the delete button if the price is not "Not for sale"
                        returnBookButton.setVisibility(View.GONE);
                    }

// Always display the thumbnail, title, and author (no changes needed here)
                    thumbnailImageView.setVisibility(View.VISIBLE); // Ensure the thumbnail is visible
                    titleTextView.setVisibility(View.VISIBLE); // Ensure the title is visible
                    authorTextView.setVisibility(View.VISIBLE); // Ensure the author is visible

// Calculate and display countdown
                    String returnDate = book.getReturnDate(); // Assuming Book class has getReturnDate()
                    if (returnDate != null) {
                        long[] remainingTime = calculateRemainingTime(returnDate);
                        if (remainingTime[0] >= 0) {
                            countdownTextView.setText(remainingTime[0] + " days ");
                        } else {
                            countdownTextView.setText("Overdue by " + Math.abs(remainingTime[0]) + " days ");
                        }
                    }


                    // Set the visibility of the "Return Book" button
                    Long returnDateMillis = book.getReturnDateMillis();
                    if (returnDateMillis != null && returnDateMillis > System.currentTimeMillis()) {
                        returnBookButton.setVisibility(View.VISIBLE); // Show the button if the book has a valid return date
                    } else {
                        returnBookButton.setVisibility(View.INVISIBLE); // Hide the button if no return date or overdue
                    }

                    // Add click listener for navigating to ReturnBook activity
                    itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(mContext, ReturnBook.class);
                        intent.putExtra("BOOK_ID", book.getId());
                        intent.putExtra("BOOK_TITLE", book.getTitle());
                        intent.putExtra("BOOK_AUTHOR", book.getAuthor());
                        intent.putExtra("BOOK_DESCRIPTION", book.getDescription());
                        intent.putExtra("BOOK_PRICE", book.getPrice());
                        intent.putExtra("BOOK_THUMBNAIL", book.getThumbnailUrl());
                        intent.putExtra("BOOK_RATING", book.getRating());
                        intent.putExtra("PDF_URL", book.getPdfUrl());
                        mContext.startActivity(intent);
                    });
                }
            }
        }
    }

    private long[] calculateRemainingTime(String returnDate) {
        if (returnDate == null || returnDate.isEmpty()) {
            return new long[]{-1, -1}; // Invalid date
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Calendar returnCalendar = Calendar.getInstance();
            returnCalendar.setTime(sdf.parse(returnDate));

            long currentMillis = System.currentTimeMillis();
            long returnMillis = returnCalendar.getTimeInMillis();

            long diffMillis = returnMillis - currentMillis;

            // Calculate full days
            long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

            // Calculate remaining milliseconds after subtracting full days
            long remainingMillisAfterDays = diffMillis - TimeUnit.DAYS.toMillis(days);

            return new long[]{days};
        } catch (Exception e) {
            e.printStackTrace();
            return new long[]{-1, -1}; // Default to invalid time on error
        }
    }

    private void removeItem(int position, Book book) {
        if (position >= 0 && position < mCartItems.size()) {
            mCartItems.remove(position);
            notifyItemRemoved(position);
            mFirestore.collection("users")
                    .document(userId)
                    .collection("borrowedBooks")
                    .document(book.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Successfully removed
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                    });
            notifyItemRangeChanged(position, mCartItems.size());
        }
    }
}
