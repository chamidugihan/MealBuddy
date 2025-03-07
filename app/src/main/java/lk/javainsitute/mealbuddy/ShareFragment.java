package lk.javainsitute.mealbuddy;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


public class ShareFragment extends Fragment {

    public static ShareFragment newInstance(String param1, String param2) {
        ShareFragment fragment = new ShareFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
View view = inflater.inflate(R.layout.fragment_share, container, false);
        ImageButton facebook = view.findViewById(R.id.imageButton2);
        ImageButton youtube = view.findViewById(R.id.imageButton3);
        ImageButton tiktik = view.findViewById(R.id.imageButton4);
        ImageButton inster = view.findViewById(R.id.imageButton5);
        TextView website = view.findViewById(R.id.textView64);

        website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://web.javainstitute.org/web-portal/login/student.jsp";
                Uri webpage = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                startActivity(intent);
            }
        });

        inster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInstagram();
            }
        });

        tiktik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTikTok();
            }
        });

        youtube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openYouTube();
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFacebook();
            }
        });
        return view;

    }

    private void openYouTube() {
        String videoId = "E1AxrOz98Kk"; // Replace with your YouTube Video ID
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));

        try {
            startActivity(appIntent); // Open in YouTube app
        } catch (ActivityNotFoundException e) {
            startActivity(webIntent); // Open in browser if YouTube app is not installed
        }
    }
    private void openFacebook() {
        String facebookUrl = "https://www.facebook.com/share/18trXxNEeK/?mibextid=wwXIfr"; // Change to your Facebook page link
        String facebookAppUrl = "fb://facewebmodal/f?href=" + facebookUrl;

        try {
            // Open Facebook App if installed
            requireContext().getPackageManager().getPackageInfo("com.facebook.katana", 0);
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookAppUrl)));
        } catch (Exception e) {
            // Open Facebook in Browser if app is not installed
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }
    }

    private void openTikTok() {
        String tiktokUserId = "javainstitute"; // Replace with actual TikTok username or video ID
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("snssdk1233://user/profile/" + tiktokUserId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@" + tiktokUserId));

        try {
            startActivity(appIntent); // Try opening in the TikTok app
        } catch (ActivityNotFoundException e) {
            startActivity(webIntent); // Open in browser if TikTok app is not installed
        }
    }

    private void openInstagram() {
        String instagramUser = "javainstitute_srilanka?igsh=d2FkZGQ4dXlwczdw"; // Replace with the Instagram username
        Uri appUri = Uri.parse("http://instagram.com/_u/" + instagramUser);
        Uri webUri = Uri.parse("http://instagram.com/" + instagramUser);

        Intent appIntent = new Intent(Intent.ACTION_VIEW, appUri);
        appIntent.setPackage("com.instagram.android");

        Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);

        try {
            startActivity(appIntent); // Try opening in the Instagram app
        } catch (ActivityNotFoundException e) {
            startActivity(webIntent); // Open in browser if Instagram app is not installed
        }
    }

}