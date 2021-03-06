package com.ldhcjs.gmplayer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.squareup.picasso.Picasso;

import java.io.IOException;

/**
 * Created by tony.lee on 2018-02-28.
 */

public class NotificationPlayer {

    private final static int NOTIFICATION_PLAYER_ID = 0x342;
    private AudioService mService;
    private NotificationManager mNotificationManager;
    private NotificationManagerBuilder mNotificationManagerBuilder;
    private boolean isForeground;

    public NotificationPlayer(AudioService service) {
        this.mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @SuppressLint("StaticFieldLeak")
    public void updateNotificationPlayer() {
        /** 기본 노티피케이션을 이용. */
         cancel();
         mNotificationManagerBuilder = new NotificationManagerBuilder();
         mNotificationManagerBuilder.execute();


        /**
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {


                Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.getAudioItem().mAlbumId);
                Bitmap largIcon = null;
                try {
                    largIcon = Picasso.with(mService).load(albumArtUri).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent actionTogglePlay = new Intent(CommandActions.TOGGLE_PLAY);
                Intent actionForward = new Intent(CommandActions.FORWARD);
                Intent actionRewind = new Intent(CommandActions.REWIND);
                Intent actionClose = new Intent(CommandActions.CLOSE);
                PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
                PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
                PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, 0);
                PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(mService);
                builder
                        .setContentTitle(mService.getAudioItem().mTitle)
                        .setContentText(mService.getAudioItem().mArtist)
                        .setLargeIcon(largIcon)
                        .setContentIntent(PendingIntent.getActivity(mService, 0, new Intent(mService, MainActivity.class), 0));

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_fast_rewind_off, "", rewind));
                builder.addAction(new NotificationCompat.Action(mService.isPlaying() ? R.drawable.ic_play_off : R.drawable.ic_play_on, "", togglePlay));
                builder.addAction(new NotificationCompat.Action(R.drawable.ic_fast_forward_off, "", forward));
//                builder.addAction(new NotificationCompat.Action(R.drawable.close, "", close));
                int[] actionsViewIndexs = new int[]{0,1,2};
                builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(actionsViewIndexs)
                        .setShowCancelButton(true).setCancelButtonIntent(close))
                        .setSmallIcon(R.drawable.ic_default_player);

                Notification notification = builder.build();

                NotificationManagerCompat.from(mService).notify(NOTIFICATION_PLAYER_ID, notification);

                if (!isForeground) {
                    isForeground = true;
                    // 서비스를 Foreground 상태로 만든다
                    mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
                }

                return null;
            }
        }.execute();

         **/
    }

    public void removeNotificationPlayer() {
        cancel();
        mService.stopForeground(true);
        isForeground = false;
    }

    private void cancel() {
        if(mNotificationManagerBuilder != null) {
            mNotificationManagerBuilder.cancel(true);
            mNotificationManagerBuilder = null;
        }
    }

    private class NotificationManagerBuilder extends AsyncTask<Void, Void, Notification> {

        private RemoteViews mRemoteViews;
        private NotificationCompat.Builder mNotificationBuilder;
        private PendingIntent mMainPendingIntent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Intent mainActivity = new Intent(mService, MainActivity.class);
            mMainPendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, 0);
            mRemoteViews = createRemoteView(R.layout.notification_player);
            mNotificationBuilder = new NotificationCompat.Builder(mService);
            mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
                    .setOngoing(true)
                    .setContentIntent(mMainPendingIntent)
                    .setContent(mRemoteViews);
            Notification notification = mNotificationBuilder.build();
            notification.priority = Notification.PRIORITY_MAX;
            notification.contentIntent = mMainPendingIntent;
            if(!isForeground) {
                isForeground = true;
                mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
            }
        }

        @Override
        protected Notification doInBackground(Void... voids) {
            mNotificationBuilder.setContent(mRemoteViews);
            mNotificationBuilder.setContentIntent(mMainPendingIntent);
            mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = mNotificationBuilder.build();
            updateRemoteView(mRemoteViews, notification);
            return notification;
        }

        @Override
        protected void onPostExecute(Notification notification) {
            super.onPostExecute(notification);

            try {
                mNotificationManager.notify(NOTIFICATION_PLAYER_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private RemoteViews createRemoteView(int layoutId) {
            RemoteViews remoteView = new RemoteViews(mService.getPackageName(), layoutId);
            Intent actionTogglePlay = new Intent(CommandActions.TOGGLE_PLAY);
            Intent actionForward = new Intent(CommandActions.FORWARD);
            Intent actionRewind = new Intent(CommandActions.REWIND);
            Intent actionClose = new Intent(CommandActions.CLOSE);
            PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
            PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
            PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, 0);
            PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);

            remoteView.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
            remoteView.setOnClickPendingIntent(R.id.btn_forward, forward);
            remoteView.setOnClickPendingIntent(R.id.btn_rewind, rewind);
            remoteView.setOnClickPendingIntent(R.id.btn_close, close);

            return remoteView;
        }

        private void updateRemoteView(final RemoteViews remoteViews, Notification notification) {

            if(mService.isPlaying()) {
                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_play_on);
            } else {
                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.ic_play_off);
            }

            String title = mService.getAudioItem().mTitle;
            remoteViews.setTextViewText(R.id.txt_title, title);
            final Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.getAudioItem().mAlbumId);
            remoteViews.setImageViewUri(R.id.img_albumart, albumArtUri);
            // Glide 적용을 못하겠네...;;
        }

    }
}
