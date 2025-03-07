package lk.javainsitute.mealbuddy.ui.notifications;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;


import lk.javainsitute.mealbuddy.Order;
import lk.javainsitute.mealbuddy.Product;
import lk.javainsitute.mealbuddy.R;
import lk.javainsitute.mealbuddy.SingleActivity;
import lk.javainsitute.mealbuddy.UserBean;
import lk.javainsitute.mealbuddy.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {
    private ArrayList<UserBean> userArrayList;
    private UserAdapter userAdapter;
    RecyclerView recyclerView;


    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.usermanagementrcy);
        userArrayList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userAdapter = new UserAdapter(userArrayList);
        loadUser();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadUser() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("user")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            userArrayList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserBean userClass = new UserBean();
                                userClass.setFname(document.getString("fname"));
                                userClass.setLname(document.getString("lname"));
                                userClass.setStatus(document.getString("status"));
                                userClass.setMobile(document.getString("mobile"));
                                userClass.setEmail(document.getString("email"));
                                userClass.setLine1(document.getString("line1"));
                                userClass.setLine2(document.getString("line2"));
                                userClass.setCity(document.getString("city"));


                                userClass.setId(document.getId());
                                userArrayList.add(userClass);
                                Log.i("ADG", "loadOrders: " + userClass.toString());
                            }

                                userAdapter = new UserAdapter(userArrayList);

                                userAdapter.setOnitemClickListener(new UserAdapter.OnitemClickListener() {
                                    @Override
                                    public void onClick(UserBean c) {
                                        Gson gson = new Gson();
                                        String p = gson.toJson(c);
                                        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                                        View userStatus = layoutInflater.inflate(R.layout.statustemplate, null);
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setView(userStatus);
                                        AlertDialog orderStatusAlertDialog = builder.create();
                                        orderStatusAlertDialog.show();

                                        RadioButton pending = userStatus.findViewById(R.id.radioButton5);
                                        RadioButton delivering = userStatus.findViewById(R.id.radioButton6);

                                        if (c.getStatus().equals("active")) {
                                            pending.setChecked(true);
                                        }
                                        if (c.getStatus().equals("inactive")) {
                                            delivering.setChecked(true);
                                        }
                                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                        Button b2 = userStatus.findViewById(R.id.update1);
                                        b2.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (pending.isChecked()) {
                                                    firestore.collection("user").document(c.getId()).update("status", "active");
                                                    orderStatusAlertDialog.dismiss();

                                                }
                                                if (delivering.isChecked()) {
                                                    firestore.collection("user").document(c.getId()).update("status", "inactive");
                                                    orderStatusAlertDialog.dismiss();
                                                }

                                                loadUser();

                                            }
                                        });


                                    }
                                });
                            recyclerView.setAdapter(userAdapter);
                        } else {
                            Log.e("FirestoreError", "Error getting orders: ", task.getException());
                        }
                    }
                });
    }

}
class UserAdapter extends RecyclerView.Adapter<UserAdapter.HistoryViewHolder> {
OnitemClickListener onitemClickListener;
    private ArrayList<UserBean> userArrayList;

    public UserAdapter(ArrayList<UserBean> userArrayList) {
        this.userArrayList = userArrayList;
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, mobile, address,active;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textView46);
            email = itemView.findViewById(R.id.textView47);
            mobile = itemView.findViewById(R.id.textView48);
            address = itemView.findViewById(R.id.textView49);
            active = itemView.findViewById(R.id.textView50);
        }
    }

    @NonNull
    @Override
    public UserAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.usermsnsgementtempalte, parent, false);
        return new UserAdapter.HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.HistoryViewHolder holder, int position) {
        UserBean user = userArrayList.get(position);
        holder.name.setText(user.getFname() +""+ user.getLname());
        holder.email.setText(user.getEmail());
        holder.mobile.setText(user.getMobile());
        holder.address.setText(user.getLine1()+user.getLine2());
        holder.active.setText(user.getStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onitemClickListener.onClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }
    public void setOnitemClickListener(UserAdapter.OnitemClickListener onitemClickListener) {
        this.onitemClickListener = onitemClickListener;
    }

    public interface OnitemClickListener {
        void onClick(UserBean c);
    }
}