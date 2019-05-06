package com.lescour.ben.go4lunch.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.lescour.ben.go4lunch.R;
import com.lescour.ben.go4lunch.controller.HomeActivity;
import com.lescour.ben.go4lunch.model.firestore.User;

import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;

/**
 * Created by benja on 24/04/2019.
 */
public class NotificationsService extends FirebaseMessagingService {

    private final int NOTIFICATION_ID = 456;
    private final String NOTIFICATION_TAG = "FIREBASE";

    private String messageBody;
    private User user;
    private String userUid;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e("tag", "something append");
        if (remoteMessage.getNotification() != null) {
            Log.e("tag", "une notif");
            this.getUserUidInSharedPreferences();
            this.retrievesUserData();
        }
    }

    private void getUserUidInSharedPreferences() {
        SharedPreferences mSharedPreferences = getSharedPreferences("currentUser", MODE_PRIVATE);
        userUid = mSharedPreferences.getString("currentUserUid", null);
    }

    private void retrievesUserData() {
        UserHelper.getUser(userUid).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                user = documentSnapshot.toObject(User.class);
                if (user != null && !user.getUserChoicePlaceId().equals("")) {
                    retrievesOtherUsersWithSameChoice();
                }
            }
        });
    }

    private void retrievesOtherUsersWithSameChoice() {
        ArrayList<User> listOfUserWithSameChoice = new ArrayList<>();
        UserHelper.getUsersWhoHaveSameChoice(user.getUserChoicePlaceId()).addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                List<DocumentSnapshot> listOfWorkmatesWithSameChoice = new ArrayList<>(queryDocumentSnapshots.getDocuments());
                if (listOfWorkmatesWithSameChoice.size() != 0) {
                    for (DocumentSnapshot documentSnapshot : listOfWorkmatesWithSameChoice) {
                        User userTemp = documentSnapshot.toObject(User.class);
                        if (userTemp != null && !user.getUid().equals(userTemp.getUid())) {
                            listOfUserWithSameChoice.add(documentSnapshot.toObject(User.class));
                        }
                    }
                }
            }
            buildBodyMessage(listOfUserWithSameChoice);
        });
    }

    private void buildBodyMessage(ArrayList<User> listOfUserWithSameChoice) {
        if (listOfUserWithSameChoice.size() != 0) {
            StringBuilder workmatesName = new StringBuilder();
            int i = 0;
            do {
                if (i == listOfUserWithSameChoice.size() -1) {
                    workmatesName.append(listOfUserWithSameChoice.get(i).getUserName()).append(".");
                } else {
                    workmatesName.append(listOfUserWithSameChoice.get(i).getUserName()).append(", ");
                }
                i++;
            } while (i != listOfUserWithSameChoice.size());
            messageBody = user.getUserChoiceRestaurantName() + getString(R.string.located_at) + user.getUserChoiceRestaurantAddress() + getString(R.string.with) + workmatesName.toString();
        } else {
            messageBody = user.getUserChoiceRestaurantName() + getString(R.string.located_at) + user.getUserChoiceRestaurantAddress() + ".";
        }
        this.sendVisualNotification();
    }

    private void sendVisualNotification() {
        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(getString(R.string.big_content_title));
        inboxStyle.addLine(messageBody);

        String channelId = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.notification_title))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(inboxStyle);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        notificationManager.notify(NOTIFICATION_TAG, NOTIFICATION_ID, notificationBuilder.build());
    }

}