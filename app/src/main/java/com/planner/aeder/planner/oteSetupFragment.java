package com.planner.aeder.planner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.storage.StorageReference;
import com.google.gson.stream.JsonReader;
import com.planner.aeder.planner.schedulesClasses.Schedule;

import com.google.gson.*;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link oteSetupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link oteSetupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class oteSetupFragment extends Fragment {
    private StorageReference mStorageRef;
    private Schedule schedule;
    private int year;
    private int month;
    private int day;
    private schedulesClasses.CalendarDay calendarDay;
    private String fileName;

    private OnFragmentInteractionListener mListener;

    public oteSetupFragment() {
        // Required empty public constructor
    }

    public static oteSetupFragment newInstance(int year, int month, int day) {
        oteSetupFragment fragment = new oteSetupFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            year = getArguments().getInt("year");
            month = getArguments().getInt("month");
            day = getArguments().getInt("day");
        }

        fileName = String.valueOf(year) + "." + String.valueOf(month) + "." + String.valueOf(day) + ".txt";
        try{
            calendarDay = new Gson().fromJson(readFile("/data/data/com.planner.aeder.planner/files/" + fileName, StandardCharsets.UTF_8), schedulesClasses.CalendarDay.class);
        }catch(Exception e){
            calendarDay = new schedulesClasses.CalendarDay(year, month, day);
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ote_setup, container, false);
        Button saveBTN = view.findViewById(R.id.saveBTN);
        Button cancelBTN = view.findViewById(R.id.cancelBTN);
        final TextView titleTXT = view.findViewById(R.id.titleInput);
        final TextView textTXT = view.findViewById(R.id.textInput);
        final TimePicker timePicker = view.findViewById(R.id.timeInput);
        timePicker.setIs24HourView(true);
        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(titleTXT.getText() == ""){
                    titleTXT.setShadowLayer(10f, 0, 0, Color.RED);
                }else{
                    schedule = new Schedule(timePicker.getHour(), timePicker.getMinute(), titleTXT.getText().toString(), textTXT.getText().toString());
                    calendarDay.addSchedule(schedule);
                    try {
                        writeToFile();
                    } catch (IOException e) {
                        throwWriteError(e);
                    }

                }
            }
        });
        return view;
    }

    public void writeToFile() throws IOException{
        try {
            FileOutputStream fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE); //output to file
            fos.write(new Gson().toJson(calendarDay, schedulesClasses.CalendarDay.class).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            throwWriteError(e);
        }
    }

    public void throwWriteError(Exception e){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Write Error")
                .setMessage(e.toString())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
