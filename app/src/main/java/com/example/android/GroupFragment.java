package com.example.android;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


/**
 * Todo: Create a temp group and check for duplicates
 */
public class GroupFragment extends Fragment {
    protected static final String FRAGMENT_NAME="GroupFragment";
    protected static int colorIcon[] = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};

    private static final int[] memberchip={R.id.Chip1,R.id.Chip2,R.id.Chip3, R.id.Chip4};
    private final Chip[] switch_buttons=new Chip[memberchip.length];
    private  Button EmailInstrucorBtn;
    private ViewGroupsAdapter adapter;
    private ArrayList<GroupsInformation> groups = new ArrayList<GroupsInformation>();
    private ListView GroupLists;
    private TextView GroupName;
    private View result;
    private TextView message;
    private ImageView GroupIcon;
    private  TextView GroupDescription;
    private   GroupsInformation info;
    private CardView CardGroup;
    private LayoutInflater inflater;
    private  TextView nogroupinfo;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private  ImageView refresh;


    private static class GroupsInformation
    {
        private final String  id;  //Primary Key
        private String NameofGroup;
        private String GroupDescription;
        private  String coursename ;

        public  ArrayList<String> members;

        private GroupsInformation(String id , String NameofGroup, String GroupDescription,String coursename, ArrayList<String> members) {
            this.id = id;
            this.NameofGroup = NameofGroup ;
            this.GroupDescription =GroupDescription;
            this.coursename = coursename;
            this.members = members;


        }

        public String getCoursename() {return  this.coursename;}
        public String  getid()
        {
            return this.id;
        }
        public String getNameofGroup() {return this.NameofGroup;}
        public String getGroupDescription() {return this.GroupDescription;}

        public void setMembers(ArrayList<String> newmembers){this.members = newmembers;}
        public void setGroupDescription(String newGroupDesc){this.GroupDescription = newGroupDesc; }
        public void setGroupName(String newName){this.NameofGroup = newName; }

        public ArrayList<String> getMembers() {return this.members;}

    }

    private class ViewGroupsAdapter extends ArrayAdapter<String> {
        public ViewGroupsAdapter(@NonNull Context context, int resource) {
            super(context, resource);
        }
        public int getCount() {
            return groups.size();
        }

        public GroupsInformation fetchitem(int position) {
            return groups.get(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            inflater = GroupFragment.this.getLayoutInflater();
            result = inflater.inflate(R.layout.group_list_view, null);
            message =  result.findViewById(R.id.GroupNameLabel);
            ImageView deleteButton = result.findViewById(R.id.RemoveGroup);
            ImageView Editbutton = result.findViewById(R.id.EditIcon);
            GroupIcon =  result.findViewById(R.id.GroupIconDisplay);
            GroupDescription = result.findViewById(R.id.GroupDescriptionLabel);
            CardGroup = result.findViewById(R.id.CardGroup);



            info  = fetchitem(position);
            deleteButton.setTag(position);
            GroupIcon.setTag(position);
            CardGroup.setTag(position);
            Editbutton.setTag(position);

            message.setText(info.getNameofGroup());
            GroupDescription.setText(info.getGroupDescription());
            GroupIcon.setColorFilter((colorIcon[ (int)Math.floor(Math.random()*(colorIcon.length))]));

            CardView cardGroupCV=result.findViewById(R.id.CardGroup);
            cardGroupCV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ShowGroupInfo(view);
                }
            });

            ImageView removeGroupIV=result.findViewById(R.id.RemoveGroup);
            removeGroupIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DeleteGroupEntry(view);
                }
            });

            ImageView editIconIV=result.findViewById(R.id.EditIcon);
            editIconIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditGroups(view);
                }
            });



            return result;
        }

    }

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String UserID  = "userID";
    private static final String username = "username";

    private String mParam1;
    private String mParam2;

    public GroupFragment() {
        // Required empty public constructor
    }

    public static GroupFragment newInstance(String param1, String param2) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putString(UserID, param1);
        args.putString(username, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View test=inflater.inflate(R.layout.fragment_group, container, false);
        GroupLists = test.findViewById(R.id.GroupinformationList);
        nogroupinfo = test.findViewById(R.id.NoGroupinfo);
        refresh = test.findViewById(R.id.refresh);

        adapter = new ViewGroupsAdapter(this.getContext(), 0 );
        GroupLists.setAdapter(adapter);
        nogroupinfo.setText(R.string.NoGroupsPage);
        nogroupinfo.setVisibility(View.VISIBLE);
        GroupLists.setVisibility(View.INVISIBLE);

        CardView btn=test.findViewById(R.id.Banner);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView,CreateGroupFragment.class,getArguments())
                        .setReorderingAllowed(true)
                        .addToBackStack("tempBackStack")
                        .commit();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RefreshPage(view);
            }
        });
        Log.i(FRAGMENT_NAME, "This is the user ID: " + mParam1 );
        db.collection("user").whereEqualTo(FieldPath.documentId(), mParam1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                mParam2 = document.getString("first_name") + " " + document.getString("last_name");
                                DisplayGroupInfo(mParam2);
                            }
                        } else {
                            Log.w(FRAGMENT_NAME, "Error getting documents.", task.getException());
                        }
                    }
                });


        return test;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(UserID);
            mParam2 = getArguments().getString(username);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(FRAGMENT_NAME, "In OnStop");
        groups.clear();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(FRAGMENT_NAME, "In onPause");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onResume() {
        super.onResume();
        groups.clear();
        DisplayGroupInfo(mParam2);

        // Toast.makeText(getActivity(), "New Change", Toast.LENGTH_SHORT).show();

    }

    /****
     * queries the database, filtering information based on user registered and adding it to the UI
     * Refreshes page every 3 seconds for updates from the firebase
     * @param name of the user that queries the data
     */
    public void DisplayGroupInfo(String name)
    {

                db.collection("groups").whereArrayContains("members", name).orderBy("name")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && (groups.size() -  task.getResult().size() !=0)) {
                                    groups.clear();

                                    for (DocumentChange document : task.getResult().getDocumentChanges()) {
                                        String groupName = document.getDocument().getString("name");
                                        String groupDesc = document.getDocument().getString("description");
                                        String courseName = document.getDocument().getString("course");
                                        Log.i( " Array information for members : ", "  " +  document.getDocument().get("members") ) ;
                                        groups.add(new GroupsInformation(document.getDocument().getId(), groupName, groupDesc, courseName, (ArrayList<String>) document.getDocument().get("members")));
                                        adapter.notifyDataSetChanged();
                                        if (groups.size()>0 )
                                        {
                                            GroupLists.setVisibility(View.VISIBLE);
                                            nogroupinfo.setVisibility(View.INVISIBLE);

                                        }
                                    }

                                    //Toast.makeText(getActivity(), "Page Refreshed ", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.w(FRAGMENT_NAME, "Error getting documents or no changes yet.", task.getException());
                                }
                            }

                        });
               // Log.i(FRAGMENT_NAME, "This is the second param: " + name );

            }










    /**
     * On click handler to display more information about the group information when clicked
     * @param view
     */

    public void ShowGroupInfo(View view)
    {

        LayoutInflater inflater = this.getLayoutInflater();
        final View views = inflater.inflate(R.layout.groups_custom_dialog_box, null);
        EmailInstrucorBtn = views.findViewById(R.id.EmailInstructorBtn);
        EmailInstrucorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EmailInstructor(view);
            }
        });


        Log.i(FRAGMENT_NAME, "List Item Clicked");
        int positionitem= (int) view.getTag();
        GroupsInformation group = groups.get(positionitem);
        GroupName = views.findViewById(R.id.ViewGroupName);
        TextView GroupDesc = views.findViewById(R.id.ViewGroupDesc);
        TextView CourseName  = views.findViewById(R.id.ViewGroupInstructor);

        for (int i =0; i < (group.getMembers().size()); i++)
        {
            switch_buttons[i]= views.findViewById(memberchip[i]);
            switch_buttons[i].setVisibility(View.VISIBLE);
            switch_buttons[i].setText(group.getMembers().get(i));

        }


        GroupName.setText(group.getNameofGroup());
        GroupDesc.setText(group.getGroupDescription()) ;

        CourseName.setText(group.getCoursename());

        AlertDialog.Builder customDialog =  new AlertDialog.Builder(this.getContext());
        customDialog.setView(views)
                .setPositiveButton(R.string.Close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        Dialog dialog = customDialog.create();
        dialog.show();

    }


    /****
     * Starts the Email Intent to send an email to the instructor directly
     * TODO: Start Email Intent to email instructor directly, would need instructor email from firebase
     * @param view
     */
    public void EmailInstructor(View view)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ibra5690@mylaurier.ca"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Email from "+ GroupName.getText());
        intent.setData(Uri.parse("mailto:"));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.EmailErr,
                    Toast.LENGTH_SHORT).show();
        }



    }


    /**
     * Enables the user to edit their information, either about the group or description of the group
     * OnClick Handler of the green edit pencil icon gets called by this
     * @param view
     */

    public void EditGroups(View view )
    {

        LayoutInflater inflater = this.getLayoutInflater();
        final View views = inflater.inflate(R.layout.custom_dialog_edit_groupinfo, null);

        Log.i(FRAGMENT_NAME, "List Item Clicked");
        int positionitem= (int) view.getTag();
        GroupsInformation group = groups.get(positionitem);

        AlertDialog.Builder customDialog =  new AlertDialog.Builder(this.getContext());
        customDialog.setView(views)
                .setPositiveButton(R.string.SaveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        boolean changed = false;
                        EditText NewGroupName = views.findViewById(R.id.GroupNameEdit);
                        EditText NewGroupDesc = views.findViewById(R.id.GroupDescEdit);

                        if (NewGroupName.getText().toString().compareTo("") != 0 ){
                            changed = true;
                            group.setGroupName(NewGroupName.getText().toString());
                        }
                        if (NewGroupDesc.getText().toString().compareTo("") != 0 ) {
                            changed = true;
                            group.setGroupDescription( NewGroupDesc.getText().toString());
                        }

                        if(changed) {
                            adapter.notifyDataSetChanged();
                            updateDatabase(group.getid(), group.getNameofGroup(), group.getGroupDescription());
                        }

                    }

                });
        Dialog dialog = customDialog.create();
        dialog.show();


    }


    /**
     * Gets called to update the database information once user changes either the group name or the group Description
     * @param id : The group id key of the new group instance
     * @param groupname : Name of the new group
     * @param groupdescription : Description of the group
     * @return true: If data has been succefully updated
     *
     */
    public boolean updateDatabase(String id, String groupname, String groupdescription)
    {
        Log.i(FRAGMENT_NAME, "Message id: " + id);
        DocumentReference groupRef = db.collection("groups").document(id);
        groupRef
                .update("name", groupname)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(FRAGMENT_NAME, "Group name successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FRAGMENT_NAME, "Error updating document id:" + id, e);

                    }
                });

        groupRef
                .update("description", groupdescription)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(FRAGMENT_NAME, "Group Desc successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(FRAGMENT_NAME, "Error updating document", e);
                    }
                });
        return true;
    }
    /***
     * On Click Handler for delete button icon, gets called when user wants to remove themselves from the group
     * Updates the arrayList and notifys data has been changed, calls the delete database fuction to notify
     * of item being deleted to the firebase
     * @param view
     */
    public void DeleteGroupEntry(View view){

        int positionitemToDelete = (int) view.getTag();
        GroupsInformation groupdel = groups.get(positionitemToDelete);
        String GroupName = groups.get(positionitemToDelete).getNameofGroup();


        AlertDialog.Builder builder =
                new AlertDialog.Builder(this.getContext());

        builder.setTitle("Are you sure you want to be removed from:" +  GroupName);
        builder.setPositiveButton(R.string.Yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        groups.remove(positionitemToDelete);
                        adapter.notifyDataSetChanged();
                        if (groups.size() == 0 )
                        {
                            nogroupinfo.setVisibility(View.VISIBLE);
                            GroupLists.setVisibility(View.INVISIBLE);
                        }
                        Log.i(FRAGMENT_NAME, "This is the user: " + mParam2);
                        deletefromdatabase(groupdel.getid(), mParam2 ); //TODO: change mparam2 to username
                    } });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    } });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    /***
     * Removes the user from the group.
     * @param groupid : Instance id of the group
     * @param username : Name of the user who wants to be removed
     */
    public void deletefromdatabase(String groupid, String username)
    {
        DocumentReference groupsRef = db.collection("groups").document(groupid);
        Log.i(FRAGMENT_NAME, "Group id " + groupid + " username " + username);
        groupsRef.update("members", FieldValue.arrayRemove(username));


        /***
         * TODO before final submission: Check if the group has only one member, delete the document if so
         */
    }



    /**
     * Refreshes the page when clicked
     */

    public void RefreshPage(View view)
    {
        Toast.makeText(getActivity(), "Page refreshed ", Toast.LENGTH_SHORT).show();
        DisplayGroupInfo(mParam2);
    }

    /**
     * Debugging functionality for Snackbar
     * @param message message to display
     * @param color   color of the snackbar
     * @param duration duration of the time it takes to display
     */
    private void PrintSnackbar(String message, int color, int duration)
    {
        Snackbar snackbar = Snackbar.make(this.getActivity().findViewById(R.id.linearLayout), message, duration)
                .setAction("Action", null);
        View snackview = snackbar.getView();
        snackview.setBackgroundColor(color);
        snackbar.show();
    }

}