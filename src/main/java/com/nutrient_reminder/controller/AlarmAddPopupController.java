package com.nutrient_reminder.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent; // 추가
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AlarmAddPopupController {

    // 💡 새로운 알람 정보를 메인 컨트롤러로 전달하기 위한 인터페이스 정의
    public interface AlarmSaveListener {
        // name: 약 이름, days: 선택된 요일 목록, time: "오전 09 : 30" 형식의 시간
        void onAlarmSaved(String name, List<String> days, String time);
    }

    private AlarmSaveListener listener;

    // 외부에서 MainController를 리스너로 설정할 수 있는 Setter 메서드
    public void setAlarmSaveListener(AlarmSaveListener listener) {
        this.listener = listener;
    }

    // 💡 FXML 필드 수정: timeLabel 제거, ampmLabel, hourField, minuteField 추가
    @FXML private Label ampmLabel;
    @FXML private TextField hourField;
    @FXML private TextField minuteField;

    @FXML private TextField nameField;
    @FXML private Button saveButton;

    // 요일 선택 버튼들
    @FXML private ToggleButton sunToggle;
    @FXML private ToggleButton monToggle;
    @FXML private ToggleButton tueToggle;
    @FXML private ToggleButton wedToggle;
    @FXML private ToggleButton thuToggle;
    @FXML private ToggleButton friToggle;
    @FXML private ToggleButton satToggle;

    @FXML
    public void initialize() {
        LocalTime now = LocalTime.now();

        // AM/PM 초기 설정 및 핸들러 연결
        ampmLabel.setText(now.getHour() < 12 ? "오전" : "오후");
        // onMouseClicked 대신 FXML에 onMouseClicked="#toggleAmPm"을 직접 연결했습니다.

        // 시간 필드 초기 설정
        int hour = now.getHour() % 12;
        if (hour == 0) hour = 12; // 0시(자정) 또는 12시(정오) 처리

        hourField.setText(String.format("%02d", hour));
        minuteField.setText(String.format("%02d", now.getMinute()));

        // 시간 필드에 숫자만 입력되도록 제한 및 범위 제한 로직 추가
        restrictToNumbers(hourField, 1, 12); // 12시간제 (1-12)
        restrictToNumbers(minuteField, 0, 59);
    }

    // AM/PM 토글 핸들러 (FXML에서 직접 연결)
    @FXML
    private void toggleAmPm(MouseEvent event) {
        if ("오전".equals(ampmLabel.getText())) {
            ampmLabel.setText("오후");
        } else {
            ampmLabel.setText("오전");
        }
    }

    // 숫자 및 범위 제한
    private void restrictToNumbers(TextField field, int minVal, int maxVal) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                field.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }
            if (!field.getText().isEmpty()) {
                try {
                    int value = Integer.parseInt(field.getText());
                    // 입력 값이 범위를 벗어날 경우 수정
                    if (value < minVal) {
                        // 사용자 입력 경험을 위해 00은 허용하지 않고 minVal로 설정
                        // (단, 00:00 분은 0으로 시작하는 것이 일반적이므로 minuteField에만 0을 허용)
                        if (minVal > 0 && value == 0) field.setText(String.format("%02d", minVal));
                    } else if (value > maxVal) {
                        field.setText(String.valueOf(maxVal));
                    }
                } catch (NumberFormatException e) {
                    // Do nothing
                }
            }
        });
    }


    @FXML
    private void handleSave() {
        // 입력값 가져오기
        String name = nameField.getText();

        // 시간 값 가져오기 및 포맷
        String ampm = ampmLabel.getText();
        String hourStr = hourField.getText();
        String minuteStr = minuteField.getText();

        // Time 형식 ex) "오전 09 : 30"
        String time = String.format("%s %s : %s", ampm, hourStr, minuteStr);

        // 선택된 요일 리스트 만들기
        List<String> days = new ArrayList<>();
        if (sunToggle.isSelected()) days.add("일");
        if (monToggle.isSelected()) days.add("월");
        if (tueToggle.isSelected()) days.add("화");
        if (wedToggle.isSelected()) days.add("수");
        if (thuToggle.isSelected()) days.add("목");
        if (friToggle.isSelected()) days.add("금");
        if (satToggle.isSelected()) days.add("토");

        // 리스너를 통해 메인 컨트롤러에 정보를 전달
        if (listener != null) {
            listener.onAlarmSaved(name, days, time);
        }

        // 팝업 닫기
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}