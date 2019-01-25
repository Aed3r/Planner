package com.planner.aeder.planner;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.gson.Gson;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.LocalDate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.planner.aeder.planner.schedulesClasses.Schedule;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Calendar.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Calendar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Calendar extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private float dY;
    private ValueAnimator scrollAnimator;
    private ObjectAnimator oteFABAnimator;
    private ObjectAnimator tdFABAnimator;
    private ObjectAnimator reFABAnimator;
    private ObjectAnimator OTEtextAnimator;
    private ObjectAnimator TDtextAnimator;
    private ObjectAnimator REtextAnimator;
    private ValueAnimator recyclerViewAnimator;
    private int maxCalendarHeight;
    private int minCalendarHeight = 300;
    private int scrollState = 1; //1: calendar expanded, 0: recycler view expanded
    private int FABState = 0; //1: expanded, 0: not expanded
    private int distance;
    private Fragment fragment;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Calendar() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Calendar.
     */
    // TODO: Rename and change types and number of parameters
    public static Calendar newInstance(String param1, String param2) {
        Calendar fragment = new Calendar();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("CutPasteId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        //region scrolling and some calenderView set up
        final MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        final TextView textView = view.findViewById(R.id.textView4);
        final TextView noSchedulesText =view.findViewById(R.id.noSchedulesTXT);

        calendarView.setTopbarVisible(false);
        calendarView.setSelectedDate(LocalDate.now());
        setHeader(textView, calendarView);

        View.OnTouchListener textTouchHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dY = v.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        ViewGroup.LayoutParams calendarLayoutParams = calendarView.getLayoutParams();
                        if(calendarLayoutParams.height <= maxCalendarHeight && calendarLayoutParams.height >= minCalendarHeight) {
                            calendarLayoutParams.height = (int) (event.getRawY() + dY);
                            calendarView.setLayoutParams(calendarLayoutParams);
                        } else if (calendarLayoutParams.height == -2) { //On first run
                            calendarLayoutParams.height = (int) (event.getRawY() + dY);
                            maxCalendarHeight = calendarLayoutParams.height;
                            calendarView.setLayoutParams(calendarLayoutParams);
                        }
                        noSchedulesText.setX(mRecyclerView.getMeasuredWidth()/2-noSchedulesText.getMeasuredWidth()/2);
                        noSchedulesText.setY(mRecyclerView.getY()+30);
                        break;
                    case MotionEvent.ACTION_UP:
                        calendarLayoutParams = calendarView.getLayoutParams();

                        if(scrollState == 1){ //Calendar expanded
                            distance = minCalendarHeight;
                            scrollState = 0;
                        } else if(scrollState == 0){ //Recycler view expanded
                            distance = maxCalendarHeight;
                            scrollState = 1;
                        }
                        scrollAnimator = ValueAnimator.ofInt(calendarView.getMeasuredHeight(), distance);
                        scrollAnimator.setInterpolator(new FastOutSlowInInterpolator());
                        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int val = (Integer) scrollAnimator.getAnimatedValue();
                                ViewGroup.LayoutParams calendarLayoutParams = calendarView.getLayoutParams();
                                calendarLayoutParams.height = val;
                                calendarView.setLayoutParams(calendarLayoutParams);
                                noSchedulesText.setX(mRecyclerView.getMeasuredWidth()/2-noSchedulesText.getMeasuredWidth()/2);
                                noSchedulesText.setY(mRecyclerView.getY()+30);
                            }
                        });
                        scrollAnimator.setDuration(250);
                        scrollAnimator.start();
                        break;
                }
                return true;
            }
        };

        textView.setOnTouchListener(textTouchHandler);
        //endregion

        //region recyclerView set up
        mRecyclerView = view.findViewById(R.id.recyclerView);

        mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        //mLayoutManager = new CustomLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false, mRecyclerView.getWidth(), -200);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        final List<Schedule> itemList = getItems(calendarView.getSelectedDate().getYear(), calendarView.getSelectedDate().getMonth(), calendarView.getSelectedDate().getDay(), noSchedulesText);

        final RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(itemList, this.getActivity());
        mRecyclerView.setAdapter(mAdapter);

        view.getViewTreeObserver().addOnGlobalLayoutListener( //Waits for the LinearLayout to complete a layout pass to get the Y pos of the Recycler View
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        noSchedulesText.setX(mRecyclerView.getMeasuredWidth()/2-noSchedulesText.getMeasuredWidth()/2);
                        noSchedulesText.setY(mRecyclerView.getY()+50);

                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                setHeader(textView, calendarView);
                itemList.clear();
                itemList.addAll(0, getItems(calendarView.getSelectedDate().getYear(), calendarView.getSelectedDate().getMonth(), calendarView.getSelectedDate().getDay(), noSchedulesText));
                mAdapter.notifyDataSetChanged();
            }
        });
        //endregion

        //region FAB action
        final FloatingActionButton mainFAB = view.findViewById(R.id.floatingActionButton);
        final FloatingActionButton oteFAB = view.findViewById(R.id.oteFAB); //One Time Event button
        final FloatingActionButton tdFAB = view.findViewById(R.id.tdFAB); //To-Do button
        final FloatingActionButton reFAB = view.findViewById(R.id.reFAB); //Recurring Event button
        final TextView oteText = view.findViewById(R.id.oteText);
        final TextView tdText = view.findViewById(R.id.tdText);
        final TextView reText = view.findViewById(R.id.reText);

        View.OnTouchListener FABTouchHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(FABState == 0) {
                        oteFAB.setX(mainFAB.getMeasuredWidth() / 2 - oteFAB.getMeasuredWidth() /2 + mainFAB.getX());
                        tdFAB.setX(mainFAB.getMeasuredWidth() / 2 - tdFAB.getMeasuredWidth() /2 + mainFAB.getX());
                        reFAB.setX(mainFAB.getMeasuredWidth() / 2 - reFAB.getMeasuredWidth() /2 + mainFAB.getX());

                        oteFAB.show();
                        tdFAB.show();
                        reFAB.show();

                        oteFABAnimator = ObjectAnimator.ofFloat(oteFAB, "translationY", -(oteFAB.getMeasuredHeight() + 50));
                        tdFABAnimator = ObjectAnimator.ofFloat(tdFAB, "translationY", -(tdFAB.getMeasuredHeight() + 50) * 3);
                        reFABAnimator = ObjectAnimator.ofFloat(reFAB, "translationY", -(reFAB.getMeasuredHeight() + 50) * 2);

                        oteFABAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                        tdFABAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                        reFABAnimator.setInterpolator(new LinearOutSlowInInterpolator());

                        oteFABAnimator.setDuration(200);
                        tdFABAnimator.setDuration(200);
                        reFABAnimator.setDuration(200);

                        oteFABAnimator.start();
                        tdFABAnimator.start();
                        reFABAnimator.start();

                        oteText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                oteText.setX(oteFAB.getX());
                                oteText.setY((oteFAB.getY() + oteFAB.getMeasuredHeight() / 2) - oteText.getMeasuredHeight() / 2);
                            }
                        }, 200);
                        tdText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tdText.setX(tdFAB.getX());
                                tdText.setY((tdFAB.getY() + tdFAB.getMeasuredHeight() / 2) - tdText.getMeasuredHeight() / 2);
                            }
                        }, 200);
                        reText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                reText.setX(reFAB.getX());
                                reText.setY((reFAB.getY() + reFAB.getMeasuredHeight() / 2) - reText.getMeasuredHeight() / 2);
                            }
                        }, 200);

                        oteText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                oteText.setVisibility(View.VISIBLE);
                                OTEtextAnimator = ObjectAnimator.ofFloat(oteText, "translationX", oteText.getX()-oteText.getMeasuredWidth() - 10);
                                OTEtextAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                                OTEtextAnimator.setDuration(250);
                                OTEtextAnimator.start();
                            }
                        }, 201);
                        tdText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tdText.setVisibility(View.VISIBLE);
                                TDtextAnimator = ObjectAnimator.ofFloat(tdText, "translationX", tdText.getX()-tdText.getMeasuredWidth() - 10);
                                TDtextAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                                TDtextAnimator.setDuration(250);
                                TDtextAnimator.start();
                            }
                        }, 201);
                        reText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                reText.setVisibility(View.VISIBLE);
                                REtextAnimator = ObjectAnimator.ofFloat(reText, "translationX", reText.getX()-reText.getMeasuredWidth() - 10);
                                REtextAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                                TDtextAnimator.setDuration(250);
                                REtextAnimator.start();
                            }
                        }, 201);

                        FABState = 1;
                    }else if(FABState == 1){
                        OTEtextAnimator = ObjectAnimator.ofFloat(oteText, "translationX", oteText.getX()+ oteText.getMeasuredWidth() +  10);
                        OTEtextAnimator.setInterpolator(new FastOutLinearInInterpolator());
                        OTEtextAnimator.setDuration(200);
                        OTEtextAnimator.start();

                        TDtextAnimator = ObjectAnimator.ofFloat(tdText, "translationX", tdText.getX()+ tdText.getMeasuredWidth() +  10);
                        TDtextAnimator.setInterpolator(new FastOutLinearInInterpolator());
                        TDtextAnimator.setDuration(200);
                        TDtextAnimator.start();

                        REtextAnimator = ObjectAnimator.ofFloat(reText, "translationX", reText.getX()+reText.getMeasuredWidth() + 10);
                        REtextAnimator.setInterpolator(new FastOutLinearInInterpolator());
                        REtextAnimator.setDuration(200);
                        REtextAnimator.start();

                        oteText.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                oteText.setVisibility(View.INVISIBLE);
                                tdText.setVisibility(View.INVISIBLE);
                                reText.setVisibility(View.INVISIBLE);
                            }
                        }, 100);

                        oteFABAnimator = ObjectAnimator.ofFloat(oteFAB, "translationY", (mainFAB.getY() - oteFAB.getY()) / 3);
                        tdFABAnimator = ObjectAnimator.ofFloat(tdFAB, "translationY", (mainFAB.getY() - tdFAB.getY()) / 3);
                        reFABAnimator = ObjectAnimator.ofFloat(reFAB, "translationY", (mainFAB.getY() - reFAB.getY()) / 3);

                        oteFABAnimator.setInterpolator(new FastOutLinearInInterpolator());
                        tdFABAnimator.setInterpolator(new FastOutLinearInInterpolator());
                        reFABAnimator.setInterpolator(new FastOutLinearInInterpolator());

                        oteFABAnimator.setDuration(200);
                        tdFABAnimator.setDuration(200);
                        reFABAnimator.setDuration(200);

                        oteFAB.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                oteFABAnimator.start();
                                tdFABAnimator.start();
                                reFABAnimator.start();

                                oteFAB.hide();
                                tdFAB.hide();
                                reFAB.hide();
                            }
                        }, 150);


                        FABState = 0;
                    }
                }
                return true;
            }
        };

        ViewGroup.OnTouchListener oteTouchHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    fragment = new oteSetupFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt("year", calendarView.getSelectedDate().getYear());
                    bundle.putInt("month", calendarView.getSelectedDate().getMonth());
                    bundle.putInt("day", calendarView.getSelectedDate().getDay());
                    fragment.setArguments(bundle);
                    loadFragment(fragment);
                }
                return false;
            }
        };

        ViewGroup.OnTouchListener tdTouchHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        };

        ViewGroup.OnTouchListener reTouchHandler = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){

                }
                return false;
            }
        };

        mainFAB.setOnTouchListener(FABTouchHandler);
        oteFAB.setOnTouchListener(oteTouchHandler);
        tdFAB.setOnTouchListener(tdTouchHandler);
        reFAB.setOnTouchListener(reTouchHandler);
        //endregion

        return view;
    }

    private List<Schedule> getItems(int year, int month, int day, TextView noSchedulesText){
        try{
            noSchedulesText.setVisibility(View.INVISIBLE);
            return(new Gson().fromJson(readFile(this.getActivity().getFilesDir().toString() + "/" + String.valueOf(year) + "." + String.valueOf(month) + "." + String.valueOf(day) + ".txt", StandardCharsets.UTF_8), schedulesClasses.CalendarDay.class).getSchedules());
        }catch(Exception e){
            noSchedulesText.setVisibility(View.VISIBLE);
            return new ArrayList<Schedule>();
        }
    }

    @NonNull
    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private void loadFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.floatingFrameContainer, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    public void setHeader(TextView textView, MaterialCalendarView calendarView){
        textView.setText(new SimpleDateFormat("MMMM").format(new Date(0, calendarView.getSelectedDate().getMonth(), 0)) + " " + String.valueOf(calendarView.getSelectedDate().getDay()) + " " + String.valueOf(calendarView.getSelectedDate().getYear()));
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder> {
        private List<Schedule> itemList;
        private Context context;

        public RecyclerViewAdapter(List<Schedule> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
            Schedule item = itemList.get(i);
            String title;
            if(String.valueOf(item.getHour()).length() == 1){
                title = "0" + String.valueOf(item.getHour());
            } else {
                title = String.valueOf(item.getHour());
            }
            if(String.valueOf(item.getMinute()).length() == 1){
                title += ":0" + String.valueOf(item.getMinute());
            } else {
                title += ":" + String.valueOf(item.getMinute());
            }
            title += " " + item.getTitle();
            customViewHolder.title.setText(title);
            customViewHolder.text.setText(item.getText());
        }

        @Override
        public int getItemCount() {
            return (null != itemList ? itemList.size() : 0);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            protected TextView title;
            protected TextView text;


            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                this.title = (TextView) itemView.findViewById(R.id.cardViewTitle);
                this.text = (TextView) itemView.findViewById(R.id.cardViewText);
            }
        }
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name (used for interaction with activity)
        void onFragmentInteraction(Uri uri);
    }
}
