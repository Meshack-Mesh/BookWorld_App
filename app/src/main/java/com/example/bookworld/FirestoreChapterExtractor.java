package com.example.bookworld;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreChapterExtractor {

    private static final String TAG = "FirestoreChapterExtractor";

    public interface OnChaptersExtractedListener {
        void onChaptersExtracted(List<String> chapters);
    }

    public static void extractChapters(Context context, String bookId, OnChaptersExtractedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> chapters = new ArrayList<>();

        // Define the path to the book's chapters collection
        String path = "books/" + bookId + "/chapters";
        Log.d(TAG, "Extracting chapters from path: " + path);

        db.collection(path)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String chapterTitle = document.getString("title");
                                    String chapterContent = document.getString("content");
                                    if (chapterTitle != null && chapterContent != null) {
                                        chapters.add(chapterTitle + "\n" + chapterContent);
                                        Log.d(TAG, "Chapter added: " + chapterTitle);
                                    } else {
                                        Log.d(TAG, "Missing title or content for document: " + document.getId());
                                    }
                                }
                                if (context instanceof android.app.Activity) {
                                    ((android.app.Activity) context).runOnUiThread(() -> listener.onChaptersExtracted(chapters));
                                }
                            } else {
                                showToast(context, "No chapters found");
                                Log.d(TAG, "No chapters found in querySnapshot");
                            }
                        } else {
                            showToast(context, "Error fetching chapters");
                            Log.e(TAG, "Error fetching chapters", task.getException());
                        }
                    }
                });
    }

    private static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}
