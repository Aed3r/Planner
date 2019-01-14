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
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.threeten.bp.LocalDate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


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
    private ObjectAnimator timePickerAnimator;
    private ValueAnimator recyclerViewAnimator;
    private int maxCalendarHeight;
    private int minCalendarHeight = 300;
    private int scrollState = 1; //1: calendar expanded, 0: recycler view expanded
    private int FABState = 0; //1: expanded, 0: not expanded
    private int distance;

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
        final TimePicker timePicker = view.findViewById(R.id.timePicker);

        timePicker.setIs24HourView(true);

        calendarView.setTopbarVisible(false);
        calendarView.setSelectedDate(LocalDate.now());
        setHeader(textView, calendarView);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull CalendarDay calendarDay, boolean b) {
                setHeader(textView, calendarView);
            }
        });

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
                        break;
                    case MotionEvent.ACTION_UP:
                        calendarLayoutParams = calendarView.getLayoutParams();

                        if(scrollState == 1){ //Calendar expanded
                            distance = minCalendarHeight;
                            scrollState = 0;
                        } else if(scrollState == 0){ //Recyclerview expanded
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
        mRecyclerView.setHasFixedSize(true);

        //mLayoutManager = new CustomLayoutManager(this.getActivity(), LinearLayoutManager.VERTICAL, false, mRecyclerView.getWidth(), -200);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        List<schedule> itemList = new ArrayList<>();
        itemList.add(new schedule(0, "Sample", "this is an example text"));
        itemList.add(new schedule(1, 6, "Sample"));
        itemList.add(new schedule(2, "Sample"));
        itemList.add(new schedule(3, 23, "Sample", "this is an example text"));
        itemList.add(new schedule(4, "Sample"));
        itemList.add(new schedule(5, 12, "Sample"));
        itemList.add(new schedule(6, "Sample", "this is an example text"));
        itemList.add(new schedule(7, 22, "Sample"));
        itemList.add(new schedule(8, "Sample"));
        itemList.add(new schedule(9, 45, "Sample", "this is an example text"));
        itemList.add(new schedule(10, "Sample"));
        itemList.add(new schedule(11, 55, "Sample"));
        itemList.add(new schedule(12, "Sample", "this is an example text"));

        mAdapter = new RecyclerViewAdapter(itemList, this.getActivity());
        mRecyclerView.setAdapter(mAdapter);
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

    public void setHeader(TextView textView, MaterialCalendarView calendarView){
        textView.setText(new SimpleDateFormat("MMMM").format(new Date(0, calendarView.getSelectedDate().getMonth(), 0)) + " " + String.valueOf(calendarView.getSelectedDate().getDay()) + " " + String.valueOf(calendarView.getSelectedDate().getYear()));
    }

    public void loadTimePicker(final MaterialCalendarView calendarView, final TextView textView, final TimePicker timePicker, final RecyclerView recyclerView){
        scrollAnimator = ValueAnimator.ofInt(calendarView.getMeasuredHeight(), 0);
        scrollAnimator.setInterpolator(new FastOutLinearInInterpolator());
        scrollAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) scrollAnimator.getAnimatedValue();
                ViewGroup.LayoutParams calendarLayoutParams = calendarView.getLayoutParams();
                calendarLayoutParams.height = val;
                calendarView.setLayoutParams(calendarLayoutParams);
            }
        });
        scrollAnimator.setDuration(200);
        scrollAnimator.start();


        recyclerViewAnimator = ValueAnimator.ofInt(0, pxToDp(recyclerView.getMeasuredHeight()));
        recyclerViewAnimator.setInterpolator(new FastOutLinearInInterpolator());
        recyclerViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) scrollAnimator.getAnimatedValue();
                LinearLayout.LayoutParams recyclerParams = (LinearLayout) recyclerView.getLayoutParams();
                recyclerParams.
            }
        });



        timePicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                timePicker.setY(textView.getY() - timePicker.getMeasuredHeight());
            }
        }, 200);
        timePicker.postDelayed(new Runnable() {
            @Override
            public void run() {
                timePickerAnimator = ObjectAnimator.ofFloat(timePicker, "translationY", textView.getMeasuredHeight());
                timePickerAnimator.setInterpolator(new LinearOutSlowInInterpolator());
                timePickerAnimator.setDuration(250);
                timePickerAnimator.start();
            }
        }, 201);
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder> {
        private List<schedule> itemList;
        private Context context;

        public RecyclerViewAdapter(List<schedule> itemList, Context context) {
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
            schedule item = itemList.get(i);
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
    public class calendarDay {
        private int year;
        private int month;
        private int day;
        private List<schedule> schedules = new ArrayList<>();

        public calendarDay(int year, int month, int day, schedule schedule) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.schedules.add(schedule);
        }

        public void addSchedule(schedule schedule) {
            this.schedules.add(schedule);
        }

        public List<schedule> getSchedules(){ return schedules; }
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public int getDay() { return day; }

    }
    public class schedule{
        private int hour;
        private int minute;
        private String title;
        private String text;

        public schedule(int hour, int minute, String title, String text){
            this.hour = hour;
            this.minute = minute;
            this.title = title;
            this.text = text;
        }

        public schedule(int hour, String title, String text){
            this.hour = hour;
            this.minute = 0;
            this.title = title;
            this.text = text;
        }

        public schedule(int hour, int minute, String title){
            this.hour = hour;
            this.minute = minute;
            this.title = title;
            this.text = "";
        }

        public schedule(int hour, String title){
            this.hour = hour;
            this.minute = 0;
            this.title = title;
            this.text = "";
        }

        public String getTitle() { return title; }
        public String getText() { return text; }
        public int getMinute() { return minute; }
        public int getHour() { return hour; }
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
