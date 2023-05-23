package com.example.todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {

    //선언
    ArrayList<ToDoModel> taskList;
    ToDoAdapter adapter;
    RecyclerView recyclerView;
    FloatingActionButton fab;
    EditText todoText;
    Button addBtn;

    //DB
    ToDoDB db;

    //입력 레이아웃
    LinearLayout bottomLayout;

    //할일 ID
    int gId;

    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DB연결
        db = new ToDoDB(this);

        //초기화
        taskList = new ArrayList<>();
        bottomLayout = findViewById(R.id.bottom_section);
        todoText = findViewById(R.id.todo_text);
        addBtn = findViewById(R.id.add_btn);
        fab = findViewById(R.id.fab);

        //recyclerView 설정
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //adapter 설정
        adapter = new ToDoAdapter(db);
        adapter.setTasks(taskList);

        //adapter 적용
        recyclerView.setAdapter(adapter);

        //조회
        selectData();

        //등록 모드
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewMode("ADD");
            }
        });

        //추가 버튼
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewMode("FAB");

                //입력 데이터
                String text = todoText.getText().toString();

                //ADD면 등록 아니면 수정
                if(addBtn.getText().toString().equals("ADD")){

                    //데이터 담기
                    ToDoModel task = new ToDoModel();
                    task.setTask(text);
                    task.setStatus(0);

                    //할일 추가
                    db.addTask(task);

                    //조회 및 리셋
                    selectReset("ADD");
                }else {

                    //할일 수정
                    db.updateTask(gId, text);

                    //조회 및 리셋
                    selectReset("UPDATE");
                }

                //키보드 내리기
                hideKeyboard(todoText);
            }
        });

        //할일 입력 체크
        todoText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.toString().equals("")) {

                    //버튼 비활성화
                    addBtn.setEnabled(false);

                    //버튼 텍스트 색 : 회색
                    addBtn.setTextColor(Color.GRAY);
                }else {

                    //버튼 활성화
                    addBtn.setEnabled(true);

                    //버튼 텍스트 색 : 검정색
                    addBtn.setTextColor(Color.BLACK);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {  }
        });

        //스와이프 기능(수정, 삭제)
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();

                switch (direction){
                    //삭제
                    case ItemTouchHelper.LEFT:

                        //할일 ID 변수에 담기
                        int id = taskList.get(position).getId();

                        //삭제 확인 다이얼로그
                        AlertDialog.Builder builder = new AlertDialog.Builder(recyclerView.getContext());
                        builder.setTitle("삭제할까요?");
                        builder.setMessage("영구적으로 삭제됩니다.");
                        //다이얼로그 외부 클릭 시 작동 안하도록 설정
                        builder.setCancelable(false);

                        //삭제에 빨간색 적용
                        String positiveButtonText = "삭제";
                        SpannableString spannableString = new SpannableString(positiveButtonText);
                        spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, positiveButtonText.length(), 0);

                        //삭제 버튼에 spannableString 적용
                        builder.setPositiveButton(spannableString, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //아이템 삭제
                                adapter.removeItem(position);

                                //DB에서 삭제
                                db.deleteTask(id);
                            }
                        });
                        //취소버튼
                        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //삭제 취소하기
                                adapter.notifyDataSetChanged();
                            }
                        });
                        builder.show();

                        break;

                    //수정
                    case ItemTouchHelper.RIGHT:
                        viewMode("UPDATE");

                        //선택한 할일 변수에 담기
                        String task = taskList.get(position).getTask();

                        //할일 ID 전역 변수에 담기
                        gId = taskList.get(position).getId();

                        //입력창에 수정할 할일 넣기
                        todoText.setText(task);

                        //버튼명 변경
                        addBtn.setText("UPDATE");

                        break;
                }
            }

            //스와이프 아이콘 바탕 설정
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder
                    , float dX, float dY, int actionState, boolean isCurrentlyActive) {

                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        //왼쪽 스와이프
                        .addSwipeLeftBackgroundColor(Color.BLACK)
                        .addSwipeLeftLabel("삭제")
                        .setSwipeLeftLabelColor(Color.RED)
                        //오른쪽 스와이프
                        .addSwipeRightBackgroundColor(Color.BLACK)
                        .addSwipeRightLabel("수정")
                        .setSwipeRightLabelColor(Color.WHITE)

                        .create()
                        .decorate();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        // ItemTouchHelper.Callback 객체 생성
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // 드래그 및 스와이프 방향 설정
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                int swipeFlags = 0; // 여기서는 스와이프 기능을 사용하지 않으므로 0으로 설정
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
                // 아이템의 순서 변경 처리
                adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 스와이프 기능을 사용하지 않으므로 구현하지 않음
            }
        };

        // ItemTouchHelper 객체 생성 및 RecyclerView에 연결
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }//onCreate

    //데이터 조회
    public void selectData(){
        //전체 할일 가져오기
        taskList = db.getAllTasks();

        //정렬
        Collections.reverse(taskList);

        //담기
        adapter.setTasks(taskList);

        //목록 체인지
        adapter.notifyDataSetChanged();
    }

    //조회 및 리셋
    public void selectReset(String type) {

        //조회
        selectData();

        //초기화
        todoText.setText("");

        addBtn.setText("ADD");

    }

    //화면 상태
    public void viewMode(String type){

        //입력하고 나면 입력창이 사라지고 FAB 보여줌
        if(type.equals("FAB")){

            //입력창 숨김
            bottomLayout.setVisibility(View.GONE);

            //FAB 보여중
            fab.setVisibility(View.VISIBLE);
        }else{      //FAB 누르면 입력창 보여주고 FAB 사라짐
            //입력창 보여줌
            bottomLayout.setVisibility(View.VISIBLE);

            // 키보드 자동으로 나타내기
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            //FAB 숨김
            fab.setVisibility(View.INVISIBLE);
        }
    }

    //키보드 숨김 메소드
    private void hideKeyboard(EditText editText){

        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //키보드 숨김
        manager.hideSoftInputFromWindow(editText.getApplicationWindowToken(), 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //메뉴 리소스 파일을 인플레이트해 액션바에 표시
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //선택된 메뉴 항목 처리
        int id = item.getItemId();
        if (id == R.id.action_help){
            //도움말 로고 클릭 시 처리 로직
            showPopup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("※도움말※");

        builder.setMessage("\n▶ 수정         : 왼쪽으로 밀기\n▶ 삭제         : 오른쪽으로 밀기\n▶ 순서 변경 : 꾹 누르고 위아래로 드래그\n");

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //팝업창 닫기
                dialog.dismiss();
            }
        });

        //다이얼로그 표시
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}//MainActivity