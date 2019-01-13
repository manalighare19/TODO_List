package com.example.manalighare.inclass08;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ocpsoft.prettytime.PrettyTime;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    EditText task;
    Spinner spinner;
    Button add;


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    ArrayList<Task> tasks=new ArrayList<>();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("ToDoTasks/");


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.showall:
                showTasks(1);
                break;

            case R.id.showcompleted:
                showTasks(2);
                break;


            case R.id.showpending:
                showTasks(3);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    void showTasks(int i){

        ArrayList<Task> pendingTask=new ArrayList<>();
        ArrayList<Task> CompletedTask=new ArrayList<>();

        if(i==1){

            mAdapter = new MyAdapter(tasks);
            mRecyclerView.setAdapter(mAdapter);

        }else if(i==2){

            for(int j=0;j<tasks.size();j++){
                if(tasks.get(j).status.equals("completed")){
                    CompletedTask.add(tasks.get(j));
                }
            }

            mAdapter = new MyAdapter(CompletedTask);
            mRecyclerView.setAdapter(mAdapter);


        }else if(i==3){

            for(int j=0;j<tasks.size();j++){
                if(tasks.get(j).status.equals("pending")){
                    pendingTask.add(tasks.get(j));
                }
            }

            mAdapter = new MyAdapter(pendingTask);
            mRecyclerView.setAdapter(mAdapter);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("TODO List");

        task=(EditText)findViewById(R.id.InputText);
        spinner=(Spinner)findViewById(R.id.spinner);
        add=(Button)findViewById(R.id.add_button);
        mRecyclerView=(RecyclerView)findViewById(R.id.recycler_view);

        mRecyclerView.setHasFixedSize(true);


        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mAdapter = new MyAdapter(tasks);
        mRecyclerView.setAdapter(mAdapter);



       myRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               tasks.clear();

               ArrayList<Task> pendingTask=new ArrayList<>();
               ArrayList<Task> CompletedTask=new ArrayList<>();


               for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    Task obj=dataSnapshot1.getValue(Task.class);

                  if(obj.status.equals("completed")){
                       CompletedTask.add(obj);
                   }else{

                       pendingTask.add(obj);
                   }

               }


               final Map<String,Integer> hashMap=new HashMap<>();
               hashMap.put("High",3);
               hashMap.put("Medium",2);
               hashMap.put("Low",1);

               Collections.sort(CompletedTask, new Comparator<Task>() {
                   @Override
                   public int compare(Task o1, Task o2) {
                       int i=hashMap.get(o1.priority)<hashMap.get(o2.priority)?1:-1;
                       return i;
                   }
               });


               Collections.sort(pendingTask, new Comparator<Task>() {
                   @Override
                   public int compare(Task o1, Task o2) {
                       int i=hashMap.get(o1.priority)<hashMap.get(o2.priority)?1:-1;
                       return i;
                   }
               });


               tasks.addAll(pendingTask);
               tasks.addAll(CompletedTask);


              mAdapter.notifyDataSetChanged();
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });




        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String note=task.getText().toString();

                if(!note.equals("")){

                    String priority=spinner.getSelectedItem().toString();
                    String time= String.valueOf(Calendar.getInstance().getTime());
                    String status="pending";
                    task.setText("");

                    String key=myRef.push().getKey();

                    Task task=new Task(key,note,priority,time,status);

                    myRef.child(key).setValue(task);

                    mAdapter.notifyDataSetChanged();

                    Toast.makeText(v.getContext(),"Task is added",Toast.LENGTH_SHORT);

                }else{
                    task.setError("Please enter note");
                }



            }
        });




    }


    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
        ArrayList<Task> tasks;


        public MyAdapter(ArrayList<Task> tasks) {
            this.tasks = tasks;

        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_layout, parent, false);
            MyViewHolder vh = new MyViewHolder(v);

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            Task obj = tasks.get(position);
            holder.Note.setText(obj.note);
            holder.display_priority.setText(obj.priority);
            holder.id=obj.id;

            if(obj.status.equals("completed")){
                holder.checkBox1.setChecked(true);
            }else {
                holder.checkBox1.setChecked(false);
            }





            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            try{
                Date date=simpleDateFormat.parse(obj.time);
                PrettyTime prettyTime=new PrettyTime();
                Log.d("prettyTime time is : ",""+prettyTime.format(date));
                holder.display_time.setText(prettyTime.format(date));

            }catch (ParseException e){
                e.printStackTrace();
            }




        }

        @Override
        public int getItemCount() {
            return tasks.size();
        }



        public static class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView Note;
            public TextView display_priority;
            public TextView display_time;
            public CheckBox checkBox1;
            public String id;

            public String status="";

            public MyViewHolder(View v) {
                super(v);
                this.Note = v.findViewById(R.id.TaskNote);
                this.display_priority = v.findViewById(R.id.Priority);
                this.display_time=v.findViewById(R.id.display_time);
                this.checkBox1=v.findViewById(R.id.checkBox);


                v.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        Log.d("Long click pressed on ","long");

                        DialogInterface.OnClickListener dialogClickListener=new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("ToDoTasks/");

                                switch (which){
                                    case DialogInterface.BUTTON_NEGATIVE:

                                        break;

                                    case  DialogInterface.BUTTON_POSITIVE:

                                        myRef.child(id).removeValue();
                                        Toast.makeText(v.getContext(), "Task is deleted", Toast.LENGTH_SHORT).show();

                                        break;

                                }
                            }
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
                        builder.setMessage("Are you sure you want to delete this task?").setPositiveButton("Yes",dialogClickListener)
                                .setNegativeButton("No",dialogClickListener);

                        AlertDialog alertDialog=builder.create();
                        alertDialog.show();

                        return false;
                    }
                });

                this.checkBox1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        Log.d("demo", "Clicked on ID "+id);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference myRef = database.getReference("ToDoTasks/");
                        final String time= String.valueOf(Calendar.getInstance().getTime());


                        DatabaseReference dref2=myRef.child(id);
                       dref2.addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               status=dataSnapshot.child("status").getValue(String.class);

                               if(status.equals("pending")){
                                   myRef.child(id).child("status").setValue("completed");
                                   myRef.child(id).child("time").setValue(time);
                                   Toast.makeText(view.getContext(), "Task is completed", Toast.LENGTH_SHORT).show();
                               }else if(status.equals("completed")){
                                   myRef.child(id).child("status").setValue("pending");
                                   myRef.child(id).child("time").setValue(time);
                                   Toast.makeText(view.getContext(), "Task is pending", Toast.LENGTH_SHORT).show();

                               }
                           }

                           @Override
                           public void onCancelled(DatabaseError databaseError) {

                           }
                       });


                    }
                });
            }
        }




    }
}
