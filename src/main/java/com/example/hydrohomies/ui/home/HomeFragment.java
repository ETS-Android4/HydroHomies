package com.example.hydrohomies.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;

import com.example.hydrohomies.R;
import com.example.hydrohomies.databinding.FragmentHomeBinding;
import com.example.hydrohomies.ui.login.LoginFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.IndicatorType;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;
import com.warkiz.widget.TickMarkType;

import org.w3c.dom.Document;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment<DatabaseReference> extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private FirebaseAuth mAuth;

    private ListView drink_list;
    private TextView textView;
    private CircularProgressBar circle;
    private IndicatorSeekBar seekbar;
    private int drink_select;

    private FirebaseFirestore db;
    private ArrayList<Drink> drinks;

    private ArrayAdapter adapter;
    private LocalDate curr_date;
    FirebaseUser current_user;
    private String coll_path;
    private String web_client_id = "64801684111-e388e68gq05850psebp31du5apfm33ok.apps.googleusercontent.com";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.i("onCreate", "Let's see");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        drinks = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        curr_date = LocalDate.now();
        mAuth = FirebaseAuth.getInstance();
        current_user = mAuth.getCurrentUser();

        coll_path = "users/" + current_user.getUid() +"/" + curr_date.toString();
        homeViewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        homeViewModel.setPath("users/" + current_user.getUid() + "/");
        //Get all views in home fragment layout
        textView = binding.textHome;
        circle = binding.circularProgressBar;
        drink_list = binding.drinkList;

        setHasOptionsMenu(true);

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                    // your code here ...
                    textView.setText(s);

            }
        });

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, drinks);
        drink_list.setAdapter(adapter);
        drink_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int posID = (int) id;
                // If "lastAmount > 0" the last API call is a valid request (that the user must
                // respond to.
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Delete Selected Drink?")
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                //Remove drink from list
                                String id = ((Drink) drinks.get(posID)).getId();
                                int amt = -((Drink)drinks.get(posID)).getAmt();
                                drinks.remove(posID);
                                //Remove drink from database
                                db.collection(coll_path).document(id)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("Deleted document", "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("Failed delete", "Error deleting document", e);
                                            }
                                        });
                                //Notify adapter of change
                                adapter.notifyDataSetChanged();
                                //Update circle UI
                                homeViewModel.addTotal(amt);
                                homeViewModel.setProgress();
                                circle.setProgress(homeViewModel.getProg());
                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel(); // Cancel dialog
                            }
                        });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        circle.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Add Drink", "Drink added");
                //Create data object
                Map<String, Object> data = new HashMap<>();
                data.put("amount", homeViewModel.getSelected());
                data.put("date", FieldValue.serverTimestamp());
                //add to drink to firestore
                db.collection(coll_path).add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Doc ID", "DocumentSnapshot written with ID: " + documentReference.getId());
                                DocumentReference docRef =  db.collection(coll_path).document(documentReference.getId());
                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot document = task.getResult();
                                            if (document.exists()) {
                                                Log.d("Doc Retrieve", "DocumentSnapshot data: " + document.getData());
                                                int amt = ((Long) document.get("amount")).intValue();
                                                Drink d = new Drink(amt, ((Timestamp)document.get("date")).toDate(), document.getId());
                                                drinks.add(d);
                                                adapter.notifyDataSetChanged();
                                                homeViewModel.addTotal(amt);
                                                homeViewModel.setProgress();
                                                circle.setProgress(homeViewModel.getProg());

                                            } else {
                                                Log.d("Doc Failed", "No such document");
                                            }
                                        } else {
                                            Log.d("FAILURE", "get failed with ", task.getException());
                                        }
                                    }
                                });
                            }
                        });
                /*
                Map<String, Object> d = new HashMap<>();
                d.put("amt", homeViewModel.getSelected());
                Log.i("amt", String.valueOf(homeViewModel.getSelected()));
                db.collection(coll_path).document("selected").set(d)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Selectttt", "DocumentSnapshot successfully written!");
                            }
                        });

                 */
            }

        });

        seekbar = binding.seekbar;
        seekbar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                Log.i("Seekbar Prog", String.valueOf(seekParams.progress));
                homeViewModel.setSelected(seekParams.progress);
            }
            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                Log.i("seeking stopped","");
            }
        });

        //seekbar.setProgress(homeViewModel.getSelected());
        getDrinks();

        return root;
    }

    public void getDrinks(){
        //Retrieve drinks stored in database based on current date and add to the drink list
        //Get the current date\
        homeViewModel.setCurr(0);
        db.collection(coll_path)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId() != "selected") {
                                    Log.d("Doc Success", document.get("amount") + " + " + document.get("date"));
                                    int amt = ((Long) document.get("amount")).intValue();
                                    //Log.i("Curr", String.valueOf(curr_total));
                                    Drink d = new Drink(amt, ((Timestamp) document.get("date")).toDate(), document.getId());
                                    Log.i("Drink ID", d.getId());
                                    Log.i("Drink", d.toString());
                                    homeViewModel.addTotal(amt);
                                    drinks.add(d);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            homeViewModel.setProgress();
                            circle.setProgress(homeViewModel.getProg());
                        } else {
                            Log.w("No doc exists", "Creating new subcollection", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        inflater.inflate(R.menu.sign_out_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out:
                // Not implemented here

                //Sign-out and close activity (Wasn't really sure how else to implement it because fragments are confusing)
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(web_client_id)
                        .build();
                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

                mAuth.signOut();
                mGoogleSignInClient.signOut();

                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);
                NavInflater navInflater = navController.getNavInflater();
                NavGraph graph = navInflater.inflate(R.navigation.mobile_navigation);
                graph.setStartDestination(R.id.navigation_login);
                navController.setGraph(graph);
                //getActivity().finish();
                /*
                Fragment frag = new LoginFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.nav_host_fragment_activity_main, frag);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack(null);
                ft.commit();
                */
                //Toast.makeText(getActivity().getApplicationContext(), "Signed Out", Toast.LENGTH_SHORT).show();
                return false;
            default:
                break;
        }
        return false;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}